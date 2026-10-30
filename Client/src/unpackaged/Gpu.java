package unpackaged;

import jagex.graphics.DrawingArea;

/**
 * GPU scene rendering (client setting, not in 2006). While the world is drawn, the rasterizer's three
 * triangle fills (gouraud method660, flat method662, textured method665) are recorded here instead of being
 * filled on the CPU; end() hands the list to OpenGL (GpuGL), which draws it in the same back-to-front order
 * with shaders that repeat the 2006 colour and texture maths, and reads the image back into the view.
 * Everything else (picking, sorting, overhead text, HUD) stays on the CPU. Any OpenGL failure turns the
 * option off and the CPU rasterizer carries on, so a missing driver or library never breaks the client.
 * This class must not touch LWJGL directly: it loads even when the lib jars are absent.
 */
public final class Gpu {
    public static boolean enabled;                         // the setting
    public static boolean capturing;                       // inside the world draw, triangles go to the lists below
    static boolean direct;                                 // this frame's world goes straight to the window (GpuPresent)
    static boolean presentFailed;                          // drawing straight to the window broke: copy-back from now on
    static long presentedAt;                               // when GpuPresent last showed a frame

    /**
     * Hands the game window back to Java2D: drops the window's OpenGL context and gives the canvas a fresh
     * native window, since one OpenGL has drawn to can't show Java2D drawing any more.
     */
    static void releaseCanvas(java.awt.Component canvas) {
        presentedAt = 0;
        try { GpuPresent.dispose(); } catch (Throwable ignored) { }
        if (canvas == null) return;
        java.awt.EventQueue.invokeLater(() -> {
            canvas.removeNotify();
            canvas.addNotify();
            ModHook.focusGame();
        });
    }

    /** OpenGL is showing the game window right now, so Java2D must keep off the canvas. */
    static boolean presenting() {
        return System.currentTimeMillis() - presentedAt < 500;
    }
    static String failure;                                 // why the GPU path turned itself off, shown in settings
    static final int TRI_INTS = 28;                        // one record per triangle (layout in GpuGL's vertex shader)
    static int[] tris = new int[TRI_INTS * 8192];            // the CPU-recorded triangles, packed
    static int[] dest = new int[8192];                     // the draw-order slot each packed triangle goes to
    static int recs, recsFlushed;                          // packed triangles this frame / already sent
    static int count;
    static int flushed;                                    // triangles already handed to the GPU this frame
    static final int BATCH = 32768;
    static int width, height, offX, offY;                  // the rasterizer area inside DrawingArea.buffer
    static int centerX, centerY;
    static int texSize;                                    // 64 (low memory) or 128 texels
    static int frame;

    private Gpu() { }

    /** Start recording the world; false keeps the CPU path for this frame. */
    public static boolean begin() {
        if (!enabled || failure != null) return false;
        int[] rows = Class39_Sub5_Sub10_Sub2.anIntArray2460;
        if (rows == null || DrawingArea.buffer == null) return false;
        width = Class39_Sub5_Sub10_Sub2.anInt2461;
        height = Class39_Sub5_Sub10_Sub2.anInt2463;
        if (width <= 0 || height <= 0) return false;
        offY = rows[0] / DrawingArea.bufferWidth;
        offX = rows[0] - offY * DrawingArea.bufferWidth;
        centerX = Class39_Sub5_Sub10_Sub2.anInt2462;
        centerY = Class39_Sub5_Sub10_Sub2.anInt2467;
        count = 0;
        flushed = 0;
        recs = recsFlushed = 0;
        GpuModels.clear();
        direct = Resizable.directReady(width, height, offX, offY);
        frame++;
        GpuStats.begin();
        try {
            GpuGL.start();
        } catch (Throwable t) {
            fail(t);
            return false;
        }
        capturing = true;
        return true;
    }

    /** The 3D area is the whole drawing buffer, so the GPU's picture (or its marker) fills every pixel of it. */
    static boolean coversView() {
        return offX == 0 && offY == 0 && width == DrawingArea.bufferWidth && height == DrawingArea.bufferHeight;
    }

    /** Draw what was recorded into the view; on failure the frame is redrawn by the CPU next time. */
    public static void end() {
        if (!capturing) return;
        capturing = false;
        try {
            GpuGL.finish(flushed, recsFlushed);
        } catch (Throwable t) {
            fail(t);
        }
    }

    /** Hand the finished triangles to the GPU every BATCH, so it draws while the CPU is still walking the scene. */
    private static void flush() {
        try {
            GpuGL.flush(flushed, count, recsFlushed, recs);
            flushed = count;
            recsFlushed = recs;
        } catch (Throwable t) {
            capturing = false;
            fail(t);
        }
    }

    static void fail(Throwable t) {
        failure = t.toString();
        System.out.println("[GPU] turned off: " + failure);
        t.printStackTrace(System.out);
        if (presentedAt != 0) releaseCanvas(ModHook.app);
        try { GpuGL.dispose(); } catch (Throwable ignored) { }
    }

    public static void setEnabled(boolean on) {
        enabled = on;
        if (on) failure = null;
        ModHook.saveSettings();
    }

    /** n triangle slots in a row at this point of the draw order, for a model the GPU fills in; -1 if not recording. */
    static int reserve(int n) {
        if (count - flushed >= BATCH) flush();
        if (!capturing) return -1;
        int base = count;
        count += n;
        return base;
    }

    private static int slot() {
        if (count - flushed >= BATCH) flush();
        if ((recs + 1) * TRI_INTS > tris.length) tris = java.util.Arrays.copyOf(tris, tris.length * 2);
        if (recs == dest.length) dest = java.util.Arrays.copyOf(dest, recs * 2);
        dest[recs] = count++;
        return recs++;
    }

    /** Ints 16-27 of a record: x, y and shade (HSL or texture lightness) of each corner, one ivec4 each. */
    private static void corners(int t, int y0, int y1, int y2, int x0, int x1, int x2, int s0, int s1, int s2) {
        int o = t * TRI_INTS + 16;
        int[] a = tris;
        a[o] = x0; a[o + 1] = y0; a[o + 2] = s0;
        a[o + 4] = x1; a[o + 5] = y1; a[o + 6] = s1;
        a[o + 8] = x2; a[o + 9] = y2; a[o + 10] = s2;
    }

    private static int head(int t, int mode, int rgb) {
        int o = t * TRI_INTS;
        tris[o] = mode;
        tris[o + 1] = rgb;
        tris[o + 2] = 256 - Class39_Sub5_Sub10_Sub2.anInt2468;     // the 2006 alpha is the share of what's behind
        return o;
    }

    /** method660: colours are HSL palette indices, interpolated across the triangle. */
    static void gouraud(int y0, int y1, int y2, int x0, int x1, int x2, int c0, int c1, int c2) {
        int t = slot();
        corners(t, y0, y1, y2, x0, x1, x2, c0, c1, c2);
        head(t, 0, 0);
    }

    /** method662: one RGB colour. */
    static void flat(int y0, int y1, int y2, int x0, int x1, int x2, int rgb) {
        int t = slot();
        corners(t, y0, y1, y2, x0, x1, x2, 0, 0, 0);
        head(t, 1, rgb & 0xffffff);
    }

    /**
     * method665 after the texture was found: the same texture-space set-up as the CPU (camera-space P, M, N
     * crossed into U, V, W planes), passed per triangle so the shader solves u = U/W, v = V/W per pixel.
     */
    static void textured(int y0, int y1, int y2, int x0, int x1, int x2, int s0, int s1, int s2,
                         int px, int mx, int nx, int py, int my, int ny, int pz, int mz, int nz,
                         int texture, int[] texels, boolean small, boolean opaque) {
        int layer = GpuTextures.layer(texture, texels, small);
        if (layer < 0) { gouraudFallback(y0, y1, y2, x0, x1, x2, s0, s1, s2, texture); return; }
        mx = px - mx; my = py - my; mz = pz - mz;
        nx -= px; ny -= py; nz -= pz;
        int u0 = nx * py - ny * px << 14, ux = ny * pz - nz * py << 8, uy = nz * px - nx * pz << 5;
        int v0 = mx * py - my * px << 14, vx = my * pz - mz * py << 8, vy = mz * px - mx * pz << 5;
        int w0 = my * nx - mx * ny << 14, wx = mz * ny - my * nz << 8, wy = mx * nz - mz * nx << 5;
        int t = slot();
        corners(t, y0, y1, y2, x0, x1, x2, s0, s1, s2);
        int o = head(t, 2, 0);
        tris[o + 3] = layer;
        tris[o + 4] = u0; tris[o + 5] = ux >> 3; tris[o + 6] = uy;
        tris[o + 7] = v0; tris[o + 8] = vx >> 3; tris[o + 9] = vy;
        tris[o + 10] = w0; tris[o + 11] = wx >> 3; tris[o + 12] = wy;
        tris[o + 13] = small ? 64 : 128;
        tris[o + 14] = opaque ? 1 : 0;
        tris[o + 15] = small ? 12 : 14;                    // W is shifted down this much
    }

    private static void gouraudFallback(int y0, int y1, int y2, int x0, int x1, int x2, int s0, int s1, int s2, int texture) {
        int hsl = Class39_Sub5_Sub10_Sub2.anInterface2_2458.method7(105, texture);
        gouraud(y0, y1, y2, x0, x1, x2, Class39_Sub5_Sub10_Sub2.method649(hsl, s0),
                Class39_Sub5_Sub10_Sub2.method649(hsl, s1), Class39_Sub5_Sub10_Sub2.method649(hsl, s2));
    }
}
