package unpackaged;

import java.awt.Canvas;
import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Streams the real Java client's 765x503 software-rendered framebuffer to a
 * browser and feeds browser input back through the client's normal AWT event
 * handlers. The game protocol/cache/rendering stay entirely inside 06-Client.
 */
public final class WebClientBridge {
    private static final int WIDTH = 765;
    private static final int HEIGHT = 503;
    private static final String WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final Object FRAME_LOCK = new Object();
    private static final int[] FRAME = new int[WIDTH * HEIGHT];
    private static final CopyOnWriteArrayList<ClientSession> CLIENTS = new CopyOnWriteArrayList<ClientSession>();
    private static final ExecutorService ENCODER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "webclient-frame-encoder");
        thread.setDaemon(true);
        return thread;
    });
    private static final AtomicBoolean ENCODE_PENDING = new AtomicBoolean(false);
    private static final ExecutorService AUDIO_SENDER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "webclient-audio-sender");
        thread.setDaemon(true);
        return thread;
    });
    private static final AtomicInteger AUDIO_PENDING = new AtomicInteger();
    // Extra 3D camera distance for browser pinch zoom; never affects normal Java client.
    private static final AtomicInteger CAMERA_DISTANCE_OFFSET = new AtomicInteger();

    private static volatile boolean enabled;
    private static volatile Path webRoot;
    private static volatile long lastFrameNanos;
    private static volatile int actualPort = -1;
    private static volatile boolean frameAvailable;
    // Only the original runescape.exe wrapper opts into shutdown after browser exit.
    private static final boolean EXIT_ON_LAST_CLIENT =
            Boolean.getBoolean("web.bridge.exitOnLastClient");
    private static final AtomicBoolean EXIT_CHECK_PENDING = new AtomicBoolean(false);

    private WebClientBridge() {
    }

    static boolean isEnabled() {
        return enabled;
    }

    static int cameraDistanceOffset() {
        return (enabled || "lan-websocket".equals(System.getProperty("client.transport")))
                ? CAMERA_DISTANCE_OFFSET.get() : 0;
    }

    static int cameraDistance(int normalDistance) {
        return clamp(normalDistance + cameraDistanceOffset(), 450, 2600);
    }

    // Positive steps zoom out, negative steps zoom in. Bounds keep the scene usable.
    static void adjustCameraZoom(int steps) {
        if (!enabled && !"lan-websocket".equals(System.getProperty("client.transport"))) return;
        final int amount = clamp(steps, -10, 10) * 70;
        CAMERA_DISTANCE_OFFSET.updateAndGet(
                old -> clamp(old + amount, -550, 850));
    }

    private static boolean hasAudioListeners() {
        for (ClientSession client : CLIENTS)
            if (client.audioEnabled) return true;
        return false;
    }

    /**
     * Audio wire packet: ASCII 'A', stream (1=effects, 2=music),
     * channel count, unsigned big-endian sample rate, and little-endian signed
     * 16-bit interleaved PCM. Video packets continue to be plain PNGs.
     */
    public static void publishAudio(int stream, int sampleRate, int channels,
                                    byte[] pcm, int length) {
        if (!enabled || !hasAudioListeners() || length <= 0 || pcm == null
                || (stream != 1 && stream != 2)
                || channels < 1 || channels > 2 || sampleRate <= 0 || sampleRate > 65535
                || length > pcm.length || length % (channels * 2) != 0) return;
        if (AUDIO_PENDING.incrementAndGet() > 8) {
            AUDIO_PENDING.decrementAndGet(); // Drop late audio instead of building a lag queue.
            return;
        }
        final byte[] packet = new byte[5 + length];
        packet[0] = 'A';
        packet[1] = (byte) stream;
        packet[2] = (byte) channels;
        packet[3] = (byte) (sampleRate >>> 8);
        packet[4] = (byte) sampleRate;
        System.arraycopy(pcm, 0, packet, 5, length);
        try {
            AUDIO_SENDER.execute(() -> {
                try {
                    for (ClientSession client : CLIENTS) {
                        if (client.audioEnabled) client.sendBinary(packet);
                    }
                } finally {
                    AUDIO_PENDING.decrementAndGet();
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
            AUDIO_PENDING.decrementAndGet();
        }
    }

    public static void startFromProperties() {
        int port = Integer.getInteger("web.bridge.port", -1);
        if (port <= 0 || enabled) {
            return;
        }
        String root = System.getProperty("web.bridge.root", "../06-WebClient/web");
        Path candidate = Paths.get(root).toAbsolutePath().normalize();
        if (!Files.isDirectory(candidate)) {
            System.err.println("[webclient] Web root not found: " + candidate);
            return;
        }
        webRoot = candidate;
        try {
            final ServerSocket listener = bind(port);
            actualPort = listener.getLocalPort();
            enabled = true;
            Thread server = new Thread(() -> acceptLoop(listener), "webclient-http");
            server.setDaemon(true);
            server.start();
            if (Boolean.getBoolean("web.bridge.openBrowser")) {
                Thread browser = new Thread(() -> {
                    try {
                        java.awt.Desktop.getDesktop().browse(java.net.URI.create(
                                "http://127.0.0.1:" + actualPort + "/"));
                    } catch (Exception exception) {
                        System.err.println("[webclient] Could not open browser automatically: "
                                + exception.getMessage());
                    }
                }, "webclient-open-browser");
                browser.setDaemon(true);
                browser.start();
            }
            System.out.println("[webclient] Real Java client bridge enabled.");
            System.out.println("[webclient] Local: http://127.0.0.1:" + actualPort + "/");
            for (String address : lanAddresses()) {
                System.out.println("[webclient] Phone: http://" + address + ":" + actualPort + "/");
            }
        } catch (IOException exception) {
            System.err.println("[webclient] Unable to start bridge: " + exception.getMessage());
        }
    }

    private static ServerSocket bind(int requestedPort) throws IOException {
        BindException last = null;
        // Launchers use a fixed URL; don't silently start on a different port.
        int lastPort = Boolean.getBoolean("web.bridge.strictPort")
                ? requestedPort : requestedPort + 20;
        for (int port = requestedPort; port <= lastPort; port++) {
            ServerSocket socket = new ServerSocket();
            socket.setReuseAddress(true);
            try {
                socket.bind(new InetSocketAddress("0.0.0.0", port));
                if (port != requestedPort) {
                    System.out.println("[webclient] Port " + requestedPort + " unavailable; using " + port + ".");
                }
                return socket;
            } catch (BindException exception) {
                last = exception;
                try { socket.close(); } catch (IOException ignored) { }
            }
        }
        throw new BindException("No free web bridge port near " + requestedPort
                + (last == null ? "" : ": " + last.getMessage()));
    }

    private static List<String> lanAddresses() {
        List<String> values = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (!network.isUp() || network.isLoopback()) continue;
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
                        values.add(address.getHostAddress());
                    }
                }
            }
        } catch (Exception ignored) {
        }
        Collections.sort(values);
        return values;
    }

    private static void acceptLoop(ServerSocket listener) {
        while (enabled) {
            try {
                final Socket socket = listener.accept();
                Thread worker = new Thread(() -> handleHttp(socket), "webclient-http-connection");
                worker.setDaemon(true);
                worker.start();
            } catch (IOException exception) {
                if (enabled) {
                    System.err.println("[webclient] HTTP accept failed: " + exception.getMessage());
                }
            }
        }
    }

    private static void handleHttp(Socket socket) {
        boolean upgraded = false;
        try {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(10000);
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            HttpRequest request = readRequest(in);
            if (request == null) return;
            if ("websocket".equalsIgnoreCase(request.headers.get("upgrade"))
                    && "/client".equals(request.path)) {
                upgraded = true;
                upgrade(socket, in, out, request);
                return;
            }
            serveFile(out, request);
        } catch (Exception exception) {
            if (!upgraded) {
                System.err.println("[webclient] HTTP error: " + exception.getMessage());
            }
        } finally {
            if (!upgraded) {
                try { socket.close(); } catch (IOException ignored) { }
            }
        }
    }

    private static HttpRequest readRequest(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int state = 0;
        while (bytes.size() < 16384) {
            int value = in.read();
            if (value < 0) return null;
            bytes.write(value);
            if ((state == 0 || state == 2) && value == '\r') state++;
            else if ((state == 1 || state == 3) && value == '\n') state++;
            else state = value == '\r' ? 1 : 0;
            if (state == 4) break;
        }
        if (state != 4) throw new IOException("HTTP header too large");
        String text = new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1);
        String[] lines = text.split("\\r\\n");
        if (lines.length == 0) return null;
        String[] first = lines[0].split(" ");
        if (first.length < 2) return null;
        String path = first[1];
        int query = path.indexOf('?');
        if (query >= 0) path = path.substring(0, query);
        try { path = URLDecoder.decode(path, "UTF-8"); } catch (Exception ignored) { }
        Map<String, String> headers = new HashMap<String, String>();
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon > 0) {
                headers.put(lines[i].substring(0, colon).trim().toLowerCase(Locale.ROOT),
                        lines[i].substring(colon + 1).trim());
            }
        }
        return new HttpRequest(first[0], path, headers);
    }

    private static void serveFile(OutputStream out, HttpRequest request) throws IOException {
        if (!"GET".equals(request.method) && !"HEAD".equals(request.method)) {
            writeHttp(out, 405, "text/plain; charset=utf-8",
                    "Method Not Allowed".getBytes(StandardCharsets.UTF_8), true);
            return;
        }
        String relative = "/".equals(request.path) ? "index.html" : request.path.substring(1);
        Path file = webRoot.resolve(relative).normalize();
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            writeHttp(out, 404, "text/plain; charset=utf-8",
                    "Not Found".getBytes(StandardCharsets.UTF_8), true);
            return;
        }
        byte[] body = Files.readAllBytes(file);
        writeHttp(out, 200, contentType(file.getFileName().toString()), body, "GET".equals(request.method));
    }

    private static void writeHttp(OutputStream out, int status, String type, byte[] body,
                                  boolean includeBody) throws IOException {
        String reason = status == 200 ? "OK" : status == 404 ? "Not Found" : "Method Not Allowed";
        String headers = "HTTP/1.1 " + status + " " + reason + "\r\n"
                + "Content-Type: " + type + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Cache-Control: no-store\r\n"
                + "X-Content-Type-Options: nosniff\r\n"
                + "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
        if (includeBody) out.write(body);
        out.flush();
    }

    private static String contentType(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html")) return "text/html; charset=utf-8";
        if (lower.endsWith(".js")) return "text/javascript; charset=utf-8";
        if (lower.endsWith(".css")) return "text/css; charset=utf-8";
        if (lower.endsWith(".webmanifest")) return "application/manifest+json; charset=utf-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static void upgrade(Socket socket, InputStream in, OutputStream out,
                                HttpRequest request) throws Exception {
        String key = request.headers.get("sec-websocket-key");
        if (key == null) throw new IOException("Missing Sec-WebSocket-Key");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        String accept = Base64.getEncoder().encodeToString(
                sha1.digest((key + WS_GUID).getBytes(StandardCharsets.ISO_8859_1)));
        String response = "HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + accept + "\r\n\r\n";
        out.write(response.getBytes(StandardCharsets.ISO_8859_1));
        out.flush();
        socket.setSoTimeout(0);

        ClientSession session = new ClientSession(socket, in, out);
        CLIENTS.add(session);
        try {
            session.readLoop();
        } finally {
            CLIENTS.remove(session);
            session.close();
            maybeExitAfterLastClient();
        }
    }

    private static void maybeExitAfterLastClient() {
        if (!EXIT_ON_LAST_CLIENT || !CLIENTS.isEmpty()
                || !EXIT_CHECK_PENDING.compareAndSet(false, true)) return;
        Thread checker = new Thread(() -> {
            try {
                // A short grace period permits a transient IE WebSocket reconnect.
                Thread.sleep(1500L);
                if (CLIENTS.isEmpty()) {
                    System.out.println("[webclient] Original client disconnected; exiting Java renderer.");
                    System.exit(0);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                EXIT_CHECK_PENDING.set(false);
            }
        }, "webclient-original-close");
        checker.setDaemon(true);
        checker.start();
    }

    /** Called from JImage.draw for every native client blit. */
    public static void blit(int[] pixels, int width, int height, int x, int y) {
        if ((!enabled && !DeveloperToolsServer.isRunning()) || pixels == null || width <= 0 || height <= 0) return;
        int srcX = 0;
        int srcY = 0;
        int copyWidth = width;
        int copyHeight = height;
        if (x < 0) { srcX = -x; copyWidth -= srcX; x = 0; }
        if (y < 0) { srcY = -y; copyHeight -= srcY; y = 0; }
        if (x + copyWidth > WIDTH) copyWidth = WIDTH - x;
        if (y + copyHeight > HEIGHT) copyHeight = HEIGHT - y;
        if (copyWidth <= 0 || copyHeight <= 0) return;
        synchronized (FRAME_LOCK) {
            for (int row = 0; row < copyHeight; row++) {
                int src = (srcY + row) * width + srcX;
                int dst = (y + row) * WIDTH + x;
                System.arraycopy(pixels, src, FRAME, dst, copyWidth);
            }
            frameAvailable = true;
        }
    }

    /** Called once at the end of each normal Java-client render pass. */
    public static void publishFrame() {
        if (!enabled || CLIENTS.isEmpty()) return;
        long now = System.nanoTime();
        // Allow 2 ms of timer jitter without dropping alternate 50 Hz frames.
        if (now - lastFrameNanos < 18_000_000L) return;
        if (!ENCODE_PENDING.compareAndSet(false, true)) return;
        lastFrameNanos = Math.max(lastFrameNanos + 20_000_000L, now - 2_000_000L);
        final int[] snapshot = new int[FRAME.length];
        synchronized (FRAME_LOCK) {
            System.arraycopy(FRAME, 0, snapshot, 0, FRAME.length);
        }
        ENCODER.execute(() -> {
            try {
                byte[] frame = FramePngEncoder.encode(snapshot, WIDTH, HEIGHT);
                for (ClientSession client : CLIENTS) {
                    client.sendBinary(frame);
                }
            } catch (Exception exception) {
                System.err.println("[webclient] Frame encode failed: " + exception.getMessage());
            } finally {
                ENCODE_PENDING.set(false);
            }
        });
    }

    static BufferedImage snapshot() {
        if (!frameAvailable) throw new IllegalStateException("Client has not rendered a frame yet");
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        synchronized (FRAME_LOCK) {
            image.setRGB(0, 0, WIDTH, HEIGHT, FRAME, 0, WIDTH);
        }
        return image;
    }

    static void mouse(final int id, final int button, final int x, final int y) {
        EventQueue.invokeLater(() -> {
            Canvas canvas = Class41.aCanvas778;
            if (canvas == null) return;
            long when = System.currentTimeMillis();
            int modifiers = 0;
            if (button == MouseEvent.BUTTON1) modifiers = InputEvent.BUTTON1_DOWN_MASK;
            if (button == MouseEvent.BUTTON2) modifiers = InputEvent.BUTTON2_DOWN_MASK;
            if (button == MouseEvent.BUTTON3) modifiers = InputEvent.BUTTON3_DOWN_MASK | InputEvent.META_MASK;
            // Unlike a real pointer exit, clamping (-1,-1) to (0,0) makes
            // RuneScape think the cursor jumped to the corner of the game.
            boolean exited = id == MouseEvent.MOUSE_EXITED;
            int canvasX = exited ? -1 : clamp(x, 0, WIDTH - 1);
            int canvasY = exited ? -1 : clamp(y, 0, HEIGHT - 1);
            if (!exited && canvas instanceof Canvas_Sub1) {
                canvasX = ((Canvas_Sub1) canvas).toCanvasX(canvasX);
                canvasY = ((Canvas_Sub1) canvas).toCanvasY(canvasY);
            }
            // Drag/move/exit events have no changed button according to AWT;
            // the held button is carried in modifiersEx instead.
            int eventButton = (id == MouseEvent.MOUSE_MOVED ||
                    id == MouseEvent.MOUSE_DRAGGED || exited)
                    ? MouseEvent.NOBUTTON : button;
            MouseEvent event = new MouseEvent(canvas, id, when, modifiers,
                    canvasX, canvasY, 1,
                    button == MouseEvent.BUTTON3 && id == MouseEvent.MOUSE_RELEASED,
                    eventButton);
            canvas.dispatchEvent(event);
        });
    }

    static void key(final int id, final int code, final int character) {
        EventQueue.invokeLater(() -> {
            Canvas canvas = Class41.aCanvas778;
            if (canvas == null) return;
            char value = character <= 0 ? KeyEvent.CHAR_UNDEFINED : (char) character;
            KeyEvent event = new KeyEvent(canvas, id, System.currentTimeMillis(), 0,
                    code <= 0 ? KeyEvent.VK_UNDEFINED : code, value);
            for (KeyListener listener : canvas.getKeyListeners()) {
                if (id == KeyEvent.KEY_PRESSED) listener.keyPressed(event);
                else if (id == KeyEvent.KEY_RELEASED) listener.keyReleased(event);
                else if (id == KeyEvent.KEY_TYPED) listener.keyTyped(event);
            }
        });
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    private static int u16(byte[] payload, int offset) {
        return (payload[offset] & 0xFF) << 8 | payload[offset + 1] & 0xFF;
    }

    private static void handleInput(byte[] payload) {
        if (payload.length < 1) return;
        int type = payload[0] & 0xFF;
        if (type == 1 && payload.length >= 5) {
            mouse(MouseEvent.MOUSE_MOVED, MouseEvent.NOBUTTON, u16(payload, 1), u16(payload, 3));
        } else if (type == 6 && payload.length == 1) {
            mouse(MouseEvent.MOUSE_EXITED, MouseEvent.NOBUTTON, -1, -1);
        } else if (type == 9 && payload.length == 2) {
            // Browser pinch packet, signed wheel-like steps. Unlike AWT wheel
            // events this changes camera distance, not inventory scrollbar state.
            adjustCameraZoom(payload[1]);
        } else if ((type == 2 || type == 3 || type == 7) && payload.length >= 6) {
            int button = payload[1] & 0xFF;
            if (button < MouseEvent.BUTTON1 || button > MouseEvent.BUTTON3) return;
            int x = u16(payload, 2);
            int y = u16(payload, 4);
            int eventId = type == 2 ? MouseEvent.MOUSE_PRESSED
                    : type == 3 ? MouseEvent.MOUSE_RELEASED : MouseEvent.MOUSE_DRAGGED;
            mouse(eventId, button, x, y);
        } else if ((type == 4 || type == 5) && payload.length >= 5) {
            int code = u16(payload, 1);
            int character = u16(payload, 3);
            key(type == 4 ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED, code, character);
        }
    }

    private static WsFrame readWsFrame(InputStream in) throws IOException {
        int first = in.read();
        if (first < 0) return null;
        int second = in.read();
        if (second < 0) return null;
        int opcode = first & 0x0F;
        boolean masked = (second & 0x80) != 0;
        long length = second & 0x7F;
        if (length == 126) {
            length = ((long) required(in) << 8) | required(in);
        } else if (length == 127) {
            length = 0;
            for (int i = 0; i < 8; i++) length = length << 8 | required(in);
        }
        if (length > 1024 * 1024) throw new IOException("WebSocket input too large");
        byte[] mask = masked ? readFully(in, 4) : null;
        byte[] payload = readFully(in, (int) length);
        if (masked) {
            for (int i = 0; i < payload.length; i++) payload[i] ^= mask[i & 3];
        }
        return new WsFrame(opcode, payload);
    }

    private static int required(InputStream in) throws IOException {
        int value = in.read();
        if (value < 0) throw new EOFException();
        return value;
    }

    private static byte[] readFully(InputStream in, int length) throws IOException {
        byte[] data = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = in.read(data, offset, length - offset);
            if (count < 0) throw new EOFException();
            offset += count;
        }
        return data;
    }

    private static final class ClientSession {
        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;
        private final Object writeLock = new Object();
        private volatile boolean closed;
        private volatile boolean audioEnabled;

        ClientSession(Socket socket, InputStream in, OutputStream out) {
            this.socket = socket;
            this.in = in;
            this.out = out;
        }

        void readLoop() throws IOException {
            while (!closed) {
                WsFrame frame = readWsFrame(in);
                if (frame == null || frame.opcode == 8) return;
                if (frame.opcode == 9) {
                    sendFrame(10, frame.payload);
                } else if (frame.opcode == 2) {
                    if (frame.payload.length == 2 && (frame.payload[0] & 0xff) == 8) {
                        audioEnabled = frame.payload[1] == 1;
                    } else {
                        handleInput(frame.payload);
                    }
                }
            }
        }

        void sendBinary(byte[] payload) {
            if (closed) return;
            try {
                sendFrame(2, payload);
            } catch (IOException exception) {
                close();
                CLIENTS.remove(this);
            }
        }

        private void sendFrame(int opcode, byte[] payload) throws IOException {
            synchronized (writeLock) {
                if (closed) return;
                out.write(0x80 | opcode);
                int length = payload.length;
                if (length < 126) {
                    out.write(length);
                } else if (length <= 0xFFFF) {
                    out.write(126);
                    out.write(length >>> 8);
                    out.write(length);
                } else {
                    out.write(127);
                    long value = length;
                    for (int shift = 56; shift >= 0; shift -= 8) {
                        out.write((int) (value >>> shift));
                    }
                }
                out.write(payload);
                out.flush();
            }
        }

        void close() {
            closed = true;
            audioEnabled = false;
            try { socket.close(); } catch (IOException ignored) { }
        }
    }

    private static final class HttpRequest {
        final String method;
        final String path;
        final Map<String, String> headers;

        HttpRequest(String method, String path, Map<String, String> headers) {
            this.method = method;
            this.path = path;
            this.headers = headers;
        }
    }

    private static final class WsFrame {
        final int opcode;
        final byte[] payload;

        WsFrame(int opcode, byte[] payload) {
            this.opcode = opcode;
            this.payload = payload;
        }
    }
}
