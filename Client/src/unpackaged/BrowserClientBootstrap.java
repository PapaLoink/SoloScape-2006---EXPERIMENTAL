package unpackaged;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Experimental phone-local rendering entrypoint for the CheerpJ browser runtime.
 *
 * CheerpJ exposes /app as a read-only web-server mount. The 443 client opens its
 * cache using RandomAccessFile("rw"), so a first-run copy into the browser's
 * persistent /files mount is required before launching the unchanged client.
 *
 * This is NOT used by the existing Java desktop or streamed WebClient launchers.
 */
public final class BrowserClientBootstrap {
    private static volatile SceneHover completedSceneHover;

    private static final class SceneHover {
        final int frame, x, y;
        SceneHover(int frame, int x, int y) {
            this.frame = frame;
            this.x = x;
            this.y = y;
        }
    }

    private static final File SOURCE_CACHE = new File("/app/cache");
    private static final File LOCAL_CACHE = new File("/files/soloscape-cache");

    private BrowserClientBootstrap() {
    }

    public static void main(String[] args) {
        try {
            if (!LOCAL_CACHE.isDirectory() && !LOCAL_CACHE.mkdirs()) {
                throw new IOException("Cannot create browser-local cache at " + LOCAL_CACHE);
            }

            copyCacheFile("main_file_cache.dat2");
            copyCacheFile("main_file_cache.idx255");
            for (int index = 0; index < 14; index++) {
                copyCacheFile("main_file_cache.idx" + index);
            }

            copyMapFallbacks(SOURCE_CACHE, LOCAL_CACHE);

            // Do not copy uid.dat: every browser session generates its own local UID.
            System.setProperty("client.cache", LOCAL_CACHE.getAbsolutePath());
            System.out.println("[local-render] Cache ready in the browser. Starting Java renderer.");
            Client.main(args);
        } catch (Throwable failure) {
            System.err.println("[local-render] Browser-local client startup failed: " + failure);
            failure.printStackTrace();
            throw new RuntimeException("Browser-local client startup failed", failure);
        }
    }


    /** Start through library mode so keyboard calls share the game's class loader. */
    public static void start() {
        main(new String[] {"1", "local", "live", "highmem", "members", "english"});
    }

    /** Forward explicit phone-keyboard input without focusing CheerpJ's text overlay. */
    public static void sendKey(final int code, final int character) {
        java.awt.EventQueue.invokeLater(() -> {
            java.awt.Canvas canvas = Class41.aCanvas778;
            if (canvas == null) return;
            char value = character <= 0 ? java.awt.event.KeyEvent.CHAR_UNDEFINED : (char) character;
            long when = System.currentTimeMillis();
            java.awt.event.KeyEvent pressed = new java.awt.event.KeyEvent(canvas,
                    java.awt.event.KeyEvent.KEY_PRESSED, when, 0, code, value);
            java.awt.event.KeyEvent released = new java.awt.event.KeyEvent(canvas,
                    java.awt.event.KeyEvent.KEY_RELEASED, when, 0, code, value);
            for (java.awt.event.KeyListener listener : canvas.getKeyListeners()) {
                listener.keyPressed(pressed);
                listener.keyReleased(released);
            }
        });
    }


    /** Control keys must not carry printable characters into the chat queue. */
    public static void setKeyDown(int code, boolean down) {
        WebClientBridge.key(down ? java.awt.event.KeyEvent.KEY_PRESSED
                : java.awt.event.KeyEvent.KEY_RELEASED, code, 0);
    }

    /** Mouse coordinates use the original game resolution, including on resized canvases. */
    public static int sendMouse(int id, int button, int x, int y) throws Exception {
        WebClientBridge.mouse(id, button, x, y);
        if (!java.awt.EventQueue.isDispatchThread()) {
            java.awt.EventQueue.invokeAndWait(() -> { });
        }
        return sceneFrame();
    }

    public static int sceneFrame() {
        return Class31.state == 30 ? Canvas_Sub1.anInt15 : -1;
    }

    /** Published only after scene picking and the context menu have finished rendering. */
    static void sceneRendered() {
        if ("lan-websocket".equals(System.getProperty("client.transport"))) {
            completedSceneHover = new SceneHover(Canvas_Sub1.anInt15,
                    jagex.utils.IsaacPrng.anInt1091, Class33.anInt599);
        }
    }

    public static boolean sceneHoverReady(int x, int y, int afterFrame) {
        if (Class31.state != 30) return true;
        SceneHover hover = completedSceneHover;
        return hover != null && hover.frame > afterFrame && hover.x == x && hover.y == y;
    }

    /** The game snapshots and clears this press under the same mouse-listener lock. */
    public static boolean clickConsumed() {
        synchronized (Class39_Sub13.aClass42_1502) {
            return Class23.anInt430 == 0;
        }
    }

    public static void zoom(int steps) {
        WebClientBridge.adjustCameraZoom(steps);
    }

    static void copyMapFallbacks(File sourceCache, File localCache) throws IOException {
        File folder = new File(localCache, "map-fallback");
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Cannot create browser map fallback directory: " + folder);
        }
        File manifest = new File(sourceCache, "map-fallback/manifest.txt");
        int count = 0;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                new FileInputStream(manifest), java.nio.charset.StandardCharsets.UTF_8))) {
            String name;
            while ((name = reader.readLine()) != null) {
                if (!name.matches("l[0-9]+_[0-9]+[.]dat")) {
                    throw new IOException("Invalid map fallback filename: " + name);
                }
                // Always refresh these small files, including repairs of identical length.
                copyCacheFile(sourceCache, localCache, "map-fallback/" + name, true);
                count++;
            }
        }
        if (count == 0) throw new IOException("No browser map fallbacks in " + manifest);
        System.out.println("[local-render] Imported " + count + " map fallbacks.");
    }

    private static void copyCacheFile(String fileName) throws IOException {
        copyCacheFile(SOURCE_CACHE, LOCAL_CACHE, fileName, false);
    }

    private static void copyCacheFile(File sourceCache, File localCache,
                                      String fileName, boolean refresh) throws IOException {
        File source = new File(sourceCache, fileName);
        File target = new File(localCache, fileName);
        if (!source.isFile()) {
            throw new IOException("Missing staged cache file: " + source);
        }
        // This prototype uses file lengths to avoid recopying the cache on reload.
        // Delete the site's browser storage when testing changes of identical length.
        if (!refresh && target.isFile() && target.length() == source.length()) {
            return;
        }

        System.out.println("[local-render] Importing " + fileName + " (" + source.length() + " bytes)");
        byte[] buffer = new byte[32768];
        try (InputStream input = new BufferedInputStream(new FileInputStream(source));
             OutputStream output = new BufferedOutputStream(new FileOutputStream(target))) {
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }
    }
}
