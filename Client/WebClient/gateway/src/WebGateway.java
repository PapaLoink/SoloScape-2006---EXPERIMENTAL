import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tiny dependency-free HTTP + WebSocket-to-TCP bridge for the revision 443 web client.
 *
 * Browser WebSockets cannot connect directly to the RSPS TCP port. This process serves
 * the static mobile client and relays binary WebSocket frames to 127.0.0.1:43594.
 */
public final class WebGateway {
    private static final String WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static Path webRoot;
    private static String gameHost;
    private static int gamePort;

    public static void main(String[] args) throws Exception {
        String bindHost = arg(args, 0, "0.0.0.0");
        int requestedHttpPort = Integer.parseInt(arg(args, 1, "8080"));
        gameHost = arg(args, 2, "127.0.0.1");
        gamePort = Integer.parseInt(arg(args, 3, "43594"));
        webRoot = Paths.get(arg(args, 4, "web")).toAbsolutePath().normalize();

        if (!Files.isDirectory(webRoot)) {
            throw new IllegalStateException("Web root not found: " + webRoot);
        }

        ServerSocket listener = null;
        int httpPort = requestedHttpPort;
        BindException lastBindError = null;
        boolean strictPort = "strict".equalsIgnoreCase(arg(args, 5, ""));
        for (int candidate = requestedHttpPort;
             candidate <= requestedHttpPort + (strictPort ? 0 : 20); candidate++) {
            ServerSocket attempt = new ServerSocket();
            attempt.setReuseAddress(true);
            try {
                attempt.bind(new InetSocketAddress(bindHost, candidate));
                listener = attempt;
                httpPort = candidate;
                break;
            } catch (BindException bindException) {
                lastBindError = bindException;
                try { attempt.close(); } catch (IOException ignored) { }
            }
        }
        if (listener == null) {
            throw new BindException("No free HTTP port from " + requestedHttpPort + " to " + (requestedHttpPort + 20)
                    + (lastBindError == null ? "" : ": " + lastBindError.getMessage()));
        }
        if (httpPort != requestedHttpPort) {
            System.out.println("Port " + requestedHttpPort + " was unavailable; using " + httpPort + " instead.");
        }
        System.out.println("06 WebClient: http://" + displayHost(bindHost) + ":" + httpPort + "/");
        System.out.println("Game relay:  ws://<same-host>:" + httpPort + "/game -> " + gameHost + ":" + gamePort);
        System.out.println("Press Ctrl+C to stop.");

        while (true) {
            final Socket socket = listener.accept();
            Thread worker = new Thread(new Runnable() {
                @Override public void run() {
                    handleConnection(socket);
                }
            }, "webclient-" + socket.getRemoteSocketAddress());
            worker.setDaemon(true);
            worker.start();
        }
    }

    private static String arg(String[] args, int index, String fallback) {
        return args.length > index && args[index] != null && !args[index].trim().isEmpty()
                ? args[index].trim() : fallback;
    }

    private static String displayHost(String bindHost) {
        return "0.0.0.0".equals(bindHost) ? "localhost" : bindHost;
    }

    private static void handleConnection(Socket socket) {
        try {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(10000);
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            OutputStream out = new BufferedOutputStream(socket.getOutputStream());
            HttpRequest request = readRequest(in);
            if (request == null) return;

            if ("websocket".equalsIgnoreCase(request.headers.get("upgrade")) && "/game".equals(request.path)) {
                // This unauthenticated TCP relay must not be reachable from other web origins.
                if (!sameOrigin(request)) {
                    writeHttp(out, 403, "text/plain; charset=utf-8",
                            "WebSocket origin not allowed".getBytes(StandardCharsets.UTF_8), true);
                    return;
                }
                upgradeAndRelay(socket, in, out, request);
                return;
            }

            serveStatic(out, request);
        } catch (Exception exception) {
            System.err.println("Web client connection error: " + exception.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) { }
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

    private static boolean sameOrigin(HttpRequest request) {
        String origin = request.headers.get("origin");
        String host = request.headers.get("host");
        return origin != null && host != null && (
                origin.equalsIgnoreCase("http://" + host)
                || origin.equalsIgnoreCase("https://" + host));
    }

    private static void serveStatic(OutputStream out, HttpRequest request) throws IOException {
        if (!"GET".equals(request.method) && !"HEAD".equals(request.method)) {
            writeHttp(out, 405, "text/plain; charset=utf-8", "Method Not Allowed".getBytes(StandardCharsets.UTF_8), true);
            return;
        }
        String relative = "/".equals(request.path) ? "index.html" : request.path.substring(1);
        Path file = webRoot.resolve(relative).normalize();
        if (!file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            writeHttp(out, 404, "text/plain; charset=utf-8", "Not Found".getBytes(StandardCharsets.UTF_8), true);
            return;
        }
        // CheerpJ needs HTTP byte ranges for JARs. Serve all static resources
        // with 200/206 and stream the bytes (including large cache archives).
        long total = Files.size(file);
        long start = 0;
        long end = total - 1;
        boolean partial = false;
        String requestedRange = request.headers.get("range");
        if (requestedRange != null) {
            // Support one "bytes=start-end", "bytes=start-" or "bytes=-suffix"
            // range. Reject malformed, unsupported and multi-range requests.
            String spec = requestedRange.startsWith("bytes=")
                    ? requestedRange.substring("bytes=".length()).trim() : "";
            int dash = spec.indexOf('-');
            if (total == 0 || dash < 0 || dash != spec.lastIndexOf('-') || spec.indexOf(',') >= 0) {
                writeUnsatisfiableRange(out, total);
                return;
            }
            String first = spec.substring(0, dash).trim();
            String last = spec.substring(dash + 1).trim();
            try {
                if (first.isEmpty()) {
                    long suffix = Long.parseLong(last);
                    if (suffix <= 0) {
                        writeUnsatisfiableRange(out, total);
                        return;
                    }
                    start = suffix >= total ? 0 : total - suffix;
                } else {
                    start = Long.parseLong(first);
                    if (start < 0 || start >= total) {
                        writeUnsatisfiableRange(out, total);
                        return;
                    }
                    if (!last.isEmpty()) {
                        end = Math.min(end, Long.parseLong(last));
                    }
                    if (end < start) {
                        writeUnsatisfiableRange(out, total);
                        return;
                    }
                }
                partial = true;
            } catch (NumberFormatException invalidRange) {
                writeUnsatisfiableRange(out, total);
                return;
            }
        }

        long bodyLength = partial ? end - start + 1 : total;
        String headers = "HTTP/1.1 " + (partial ? "206 Partial Content" : "200 OK") + "\r\n"
                + "Content-Type: " + contentType(file.getFileName().toString()) + "\r\n"
                + "Content-Length: " + bodyLength + "\r\n"
                + "Accept-Ranges: bytes\r\n"
                + (partial ? "Content-Range: bytes " + start + "-" + end + "/" + total + "\r\n" : "")
                + "Cache-Control: no-cache\r\n"
                + "X-Content-Type-Options: nosniff\r\n"
                + "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
        if ("GET".equals(request.method)) {
            try (java.nio.channels.SeekableByteChannel channel = Files.newByteChannel(file)) {
                channel.position(start);
                byte[] chunk = new byte[65536];
                long remaining = bodyLength;
                while (remaining > 0) {
                    int count = channel.read(java.nio.ByteBuffer.wrap(chunk, 0,
                            (int) Math.min((long) chunk.length, remaining)));
                    if (count < 0) throw new EOFException("File changed while serving " + file);
                    if (count == 0) continue;
                    out.write(chunk, 0, count);
                    remaining -= count;
                }
            }
        }
        out.flush();
    }

    private static void writeUnsatisfiableRange(OutputStream out, long total) throws IOException {
        String headers = "HTTP/1.1 416 Range Not Satisfiable\r\n"
                + "Content-Range: bytes */" + total + "\r\n"
                + "Accept-Ranges: bytes\r\n"
                + "Content-Length: 0\r\n"
                + "Cache-Control: no-cache\r\n"
                + "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
        out.flush();
    }

    private static void writeHttp(OutputStream out, int status, String type, byte[] body, boolean includeBody) throws IOException {
        String reason = status == 200 ? "OK" : status == 404 ? "Not Found"
                : status == 403 ? "Forbidden" : "Method Not Allowed";
        String headers = "HTTP/1.1 " + status + " " + reason + "\r\n"
                + "Content-Type: " + type + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Cache-Control: no-cache\r\n"
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
        if (lower.endsWith(".json") || lower.endsWith(".webmanifest")) return "application/manifest+json; charset=utf-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private static void upgradeAndRelay(final Socket browser, final InputStream browserIn,
                                        final OutputStream browserOut, HttpRequest request) throws Exception {
        String key = request.headers.get("sec-websocket-key");
        if (key == null) throw new IOException("Missing Sec-WebSocket-Key");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        String accept = Base64.getEncoder().encodeToString(
                sha1.digest((key + WS_GUID).getBytes(StandardCharsets.ISO_8859_1)));
        String response = "HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + accept + "\r\n\r\n";
        browserOut.write(response.getBytes(StandardCharsets.ISO_8859_1));
        browserOut.flush();
        browser.setSoTimeout(0);

        final Socket game = new Socket();
        game.connect(new InetSocketAddress(gameHost, gamePort), 5000);
        game.setTcpNoDelay(true);
        final InputStream gameIn = new BufferedInputStream(game.getInputStream());
        final OutputStream gameOut = new BufferedOutputStream(game.getOutputStream());
        final AtomicBoolean closed = new AtomicBoolean(false);
        final Object browserWriteLock = new Object();

        Thread downstream = new Thread(new Runnable() {
            @Override public void run() {
                byte[] buffer = new byte[8192];
                try {
                    int count;
                    while (!closed.get() && (count = gameIn.read(buffer)) >= 0) {
                        if (count == 0) continue;
                        synchronized (browserWriteLock) {
                            writeWsFrame(browserOut, 2, buffer, 0, count);
                            browserOut.flush();
                        }
                    }
                } catch (IOException ignored) {
                } finally {
                    closed.set(true);
                    try { browser.close(); } catch (IOException ignored) { }
                    try { game.close(); } catch (IOException ignored) { }
                }
            }
        }, "game-to-web");
        downstream.setDaemon(true);
        downstream.start();

        try {
            while (!closed.get()) {
                WsFrame frame = readWsFrame(browserIn);
                if (frame == null) break;
                if (frame.opcode == 8) break;
                if (frame.opcode == 9) {
                    synchronized (browserWriteLock) {
                        writeWsFrame(browserOut, 10, frame.payload, 0, frame.payload.length);
                        browserOut.flush();
                    }
                    continue;
                }
                if (frame.opcode == 2 || frame.opcode == 0) {
                    gameOut.write(frame.payload);
                    gameOut.flush();
                }
            }
        } finally {
            closed.set(true);
            try { game.close(); } catch (IOException ignored) { }
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
            length = ((long) readRequired(in) << 8) | readRequired(in);
        } else if (length == 127) {
            length = 0;
            for (int i = 0; i < 8; i++) length = (length << 8) | readRequired(in);
        }
        if (length > 1024 * 1024) throw new IOException("WebSocket frame too large");
        byte[] mask = masked ? readFully(in, 4) : null;
        byte[] payload = readFully(in, (int) length);
        if (masked) {
            for (int i = 0; i < payload.length; i++) payload[i] ^= mask[i & 3];
        }
        return new WsFrame(opcode, payload);
    }

    private static int readRequired(InputStream in) throws IOException {
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

    private static void writeWsFrame(OutputStream out, int opcode, byte[] data, int offset, int length) throws IOException {
        out.write(0x80 | (opcode & 0x0F));
        if (length < 126) {
            out.write(length);
        } else if (length <= 0xFFFF) {
            out.write(126);
            out.write(length >>> 8);
            out.write(length);
        } else {
            out.write(127);
            for (int shift = 56; shift >= 0; shift -= 8) out.write((int) ((long) length >>> shift));
        }
        out.write(data, offset, length);
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
