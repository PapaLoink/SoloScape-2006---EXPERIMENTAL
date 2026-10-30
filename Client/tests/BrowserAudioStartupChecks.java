package unpackaged;

import jagex.world.actors.StillGraphic;

public final class BrowserAudioStartupChecks {
    public static void main(String[] args) throws Exception {
        System.setProperty("client.transport", "lan-websocket");
        // No Signlink, canvas, MIDI device or audio device is available in this check.
        Class39_Sub5_Sub9.signlink = null;
        Class41.aCanvas778 = null;
        Class26.anInt484 = 45;
        Class39_Sub12.method870((byte) 0);

        require(Class26.anInt484 == 50, "Browser startup did not advance to fonts");
        require(Class39_Sub7.anInt1387 == 35, "Loading progress did not advance");
        require(Class55.aClass62_1251 == null, "Browser startup opened a MIDI player");
        require(Class20.aClass13_395 != null
                && Class20.aClass13_395.getClass() == Class13.class,
                "Browser startup did not select silent audio output");
        require(Class15.anInt274 == 22050, "Silent mixer has an invalid sample rate");
        require(!StillGraphic.aBoolean2342, "Silent mixer should use mono samples");
        require(Class1.aClass39_Sub1_Sub1_32 != null, "Effect mixer was not initialized");
        require(ScriptState.aClass39_Sub1_439 == Class1.aClass39_Sub1_Sub1_32,
                "Effect mixer was not registered");
        require(ClientScript.aClass43_1693 != null, "Audio resampler was not initialized");
        // Later ticks and shutdown must still be safe with the silent output.
        Class20.aClass13_395.method193(0L);
        Class20.aClass13_395.method187();
        // Exercise the real game key listener, including soft-keyboard-only text.
        Class41.aCanvas778 = new java.awt.Canvas();
        jagex.world.actors.Npc.aClass35_2499 = new JKeyListener();
        Class41.aCanvas778.addKeyListener(jagex.world.actors.Npc.aClass35_2499);
        Class39_Sub5_Sub4_Sub4.anInt2299 = 0;
        jagex.io.Buffer.anInt1368 = 0;
        jagex.io.BufferedFile.anInt341 = 0;
        BrowserClientBootstrap.sendKey(0, 'a');
        BrowserClientBootstrap.sendKey(8, 8);
        BrowserClientBootstrap.sendKey(10, 10);
        java.awt.EventQueue.invokeAndWait(() -> { });
        require(Class39_Sub5_Sub4_Sub4.anInt2299 == 3,
                "Phone keyboard did not enqueue exactly three game inputs");
        require(Class46_Sub1.anIntArray1558[0] == 'a',
                "Phone typing did not reach the game character queue");
        require(Class46.anIntArray883[1] == 85,
                "Phone Backspace did not reach the game's delete key");
        require(Class46.anIntArray883[2] == 84,
                "Phone Enter did not reach the game's submit key");
        Class41.aCanvas778 = null;
        BrowserClientBootstrap.sendKey(0, 'b');
        java.awt.EventQueue.invokeAndWait(() -> { });
        require(Class39_Sub5_Sub4_Sub4.anInt2299 == 3,
                "Keyboard input should be ignored when no game canvas exists");

        Class41.aCanvas778 = new java.awt.Canvas();
        Class41.aCanvas778.addKeyListener(jagex.world.actors.Npc.aClass35_2499);
        int arrowStart = Class39_Sub5_Sub4_Sub4.anInt2299;
        for (int code = 37; code <= 40; code++) {
            BrowserClientBootstrap.setKeyDown(code, true);
            BrowserClientBootstrap.setKeyDown(code, false);
        }
        java.awt.EventQueue.invokeAndWait(() -> { });
        require(Class39_Sub5_Sub4_Sub4.anInt2299 == arrowStart + 4,
                "Desktop arrows did not reach the control-key queue");
        for (int slot = arrowStart; slot < arrowStart + 4; slot++) {
            require(Class46_Sub1.anIntArray1558[slot] == -1,
                    "A desktop arrow generated a printable chat character");
        }

        jagex.utils.IsaacPrng.anInt1087 = 765;
        jagex.utils.Deque.anInt919 = 503;
        Canvas_Sub1 touchCanvas = new Canvas_Sub1(new java.awt.Canvas());
        touchCanvas.setSize(1530, 1006);
        JMouseListener mouseListener = Class39_Sub13.aClass42_1502;
        touchCanvas.addMouseListener(mouseListener);
        touchCanvas.addMouseMotionListener(mouseListener);
        Class41.aCanvas778 = touchCanvas;
        Class31.state = 30;
        Canvas_Sub1.anInt15 = 7;
        require(BrowserClientBootstrap.sendMouse(503, 0, 100, 110) == 7,
                "Touch hover did not return scene render progress");
        require(jagex.world.actors.Projectile.anInt2208 == 100 && Class10.anInt172 == 110,
                "Touch hover coordinates were not mapped through the resized game canvas");
        jagex.utils.IsaacPrng.anInt1091 = 100;
        Class33.anInt599 = 110;
        BrowserClientBootstrap.sceneRendered();
        require(!BrowserClientBootstrap.sceneHoverReady(100, 110, 7),
                "A frame already in progress incorrectly acknowledged the new hover");
        Canvas_Sub1.anInt15 = 8;
        BrowserClientBootstrap.sceneRendered();
        require(BrowserClientBootstrap.sceneHoverReady(100, 110, 7),
                "Completed scene hover was not acknowledged");
        require(!BrowserClientBootstrap.sceneHoverReady(105, 113, 7),
                "A scene rendered at an old position incorrectly acknowledged finger drift");
        BrowserClientBootstrap.sendMouse(501, 1, 120, 130);
        require(JMouseListener.anInt791 == 120 && Class41.anInt784 == 130 && Class23.anInt430 == 1,
                "Touch tap did not reach the game's click queue");
        require(!BrowserClientBootstrap.clickConsumed(),
                "Touch press was acknowledged before the logic tick sampled it");
        Class39_Sub5_Sub18.method776((byte) -99);
        require(BrowserClientBootstrap.clickConsumed() && Class46.anInt887 == 1,
                "Logic tick did not acknowledge and preserve the pending touch click");
        BrowserClientBootstrap.sendMouse(502, 1, 120, 130);
        Class23.anInt430 = 0;
        Class34.anInt605 = 500;
        jagex.io.JSocket.anInt301 = 200;
        BrowserClientBootstrap.sendMouse(501, 2, 100, 100);
        BrowserClientBootstrap.sendMouse(506, 2, 140, 120);
        BrowserClientBootstrap.sendMouse(502, 2, 140, 120);
        require(Class34.anInt605 == 420 && jagex.io.JSocket.anInt301 == 220,
                "Finger drag did not rotate the camera through its existing handler");
        require(Class23.anInt430 == 0, "Camera drag generated an unintended walking click");
        BrowserClientBootstrap.zoom(-2);
        require(WebClientBridge.cameraDistanceOffset() == -140,
                "Browser-local pinch did not zoom in");
        BrowserClientBootstrap.zoom(1);
        require(WebClientBridge.cameraDistanceOffset() == -70,
                "Browser-local pinch did not zoom out");
        for (int n = 0; n < 10; n++) BrowserClientBootstrap.zoom(10);
        require(WebClientBridge.cameraDistanceOffset() == 850, "Zoom-out limit was not enforced");
        for (int n = 0; n < 10; n++) BrowserClientBootstrap.zoom(-10);
        require(WebClientBridge.cameraDistanceOffset() == -550, "Zoom-in limit was not enforced");
        System.clearProperty("client.transport");
        BrowserClientBootstrap.zoom(10);
        require(WebClientBridge.cameraDistanceOffset() == 0, "Phone zoom leaked into desktop mode");
        System.setProperty("client.transport", "lan-websocket");
        require(WebClientBridge.cameraDistanceOffset() == -550,
                "Desktop mode unexpectedly changed the stored browser zoom");
        java.io.File stagedCache = new java.io.File("Client/WebClient/build/local-render/cache");
        java.nio.file.Path browserCache = java.nio.file.Files.createTempDirectory("browser-map-cache");
        BrowserClientBootstrap.copyMapFallbacks(stagedCache, browserCache.toFile());
        java.io.File[] fallbackFiles = new java.io.File("Server/cache/map-fallback")
                .listFiles((dir, name) -> name.matches("l[0-9]+_[0-9]+[.]dat"));
        require(fallbackFiles != null && fallbackFiles.length > 0, "Bundled map fallbacks are missing");
        for (java.io.File fallback : fallbackFiles) {
            java.nio.file.Path imported = browserCache.resolve("map-fallback").resolve(fallback.getName());
            require(java.util.Arrays.equals(java.nio.file.Files.readAllBytes(fallback.toPath()),
                    java.nio.file.Files.readAllBytes(imported)),
                    "Browser cache did not import the complete map: " + fallback.getName());
        }
        java.nio.file.Path lumbridge = browserCache.resolve("map-fallback/l50_50.dat");
        byte[] expectedMap = java.nio.file.Files.readAllBytes(lumbridge);
        require(expectedMap.length > 0, "Lumbridge fallback is empty");
        byte[] staleMap = expectedMap.clone();
        staleMap[0] ^= 1;
        java.nio.file.Files.write(lumbridge, staleMap);
        BrowserClientBootstrap.copyMapFallbacks(stagedCache, browserCache.toFile());
        require(java.util.Arrays.equals(expectedMap, java.nio.file.Files.readAllBytes(lumbridge)),
                "A same-length stale browser map was not refreshed");
        System.out.println("Browser audio, keyboard, touch and map import checks passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
