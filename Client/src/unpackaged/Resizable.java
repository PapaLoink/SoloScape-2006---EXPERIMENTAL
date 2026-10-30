package unpackaged;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import jagex.graphics.AbstractImage;
import jagex.utils.Queue;
import jagex.utils.SubNode;

/**
 * Resizable layout (OSRS "classic resizable", not in 2006): the 3D view fills the canvas and the 2006 minimap,
 * side panel and chatbox are laid over it at the top right, bottom right and bottom left.
 *
 * The game still draws every panel in its fixed 765x503 layout. Those blits are caught here into an off-screen
 * frame, and at the end of each frame the view plus the three panel regions cut from that frame are put on the
 * canvas in one blit. Mouse positions over a panel are mapped back to fixed coordinates; positions over the view
 * are moved by {@link #OFF} so that no fixed-layout panel check can mistake them for a panel, and the view's own
 * checks read them through {@link #vx}/{@link #vy}/{@link #inView}.
 */
public final class Resizable {
    private Resizable() {}

    /** The setting. */
    static volatile boolean enabled;
    /** View mouse coordinates are moved this far right and down (beyond every fixed-layout panel). */
    public static final int OFF = 4096;

    static final int FW = 765, FH = 503;
    // Panel regions in the fixed layout
    static final int MAP_X = 516, MAP_Y = 0, MAP_W = 249, MAP_H = 168;
    static final int SIDE_X = 516, SIDE_Y = 168, SIDE_W = 249, SIDE_H = 335;
    static final int CHAT_X = 0, CHAT_Y = 338, CHAT_W = 516, CHAT_H = 165;

    /** Current view size (the canvas size while active, 512x334 otherwise). */
    public static int vw = 512, vh = 334;
    /**
     * Perspective scale (the fixed client's "<< 9"). While the world is drawn it is {@link #sceneZoom}; everything
     * else (interfaces, item icons, chathead models) keeps 512.
     */
    public static int zoom = 512, sceneZoom = 512;
    /** Scene scale for a view of this height: the 2006 scale, about 1% closer in resizable mode (tuned by eye against OSRS). */
    static int zoomFor(int h) { return h <= 334 ? 512 : 518; }
    private static int cw, ch;                       // canvas size the view was built for

    private static final int[] fixed = new int[FW * FH];
    private static AbstractImage view;               // the game view image last blitted
    private static BufferedImage screen;
    private static int[] screenPx;
    private static boolean fullscreenFixed;          // a full-screen interface is open: show the fixed frame
    private static boolean wasActive;

    // ------------------------------------------------------------------ state
    /** True while the resizable layout is on screen. */
    public static boolean active() {
        return enabled && Class31.state >= 25 && vw > 512 && cw > 0;      // 25 = "Loading - please wait"
    }

    static void setEnabled(boolean b) {
        enabled = b;
        ModHook.saveSettings();
        ModHook.relayout();
        ModHook.refresh();
    }

    // ------------------------------------------------------------ coordinates
    /** View-relative x of a game mouse/click x (the fixed layout's "x - 4"). */
    public static int vx(int x) { return active() ? x - OFF - 4 : x - 4; }
    public static int vy(int y) { return active() ? y - OFF - 4 : y - 4; }

    /** The fixed layout's "mouse over the 3D view" test. */
    public static boolean inView(int x, int y) {
        if (!active()) return x > 4 && y > 4 && x < 516 && y < 338;
        x -= OFF; y -= OFF;
        return x > 4 && y > 4 && x < vw + 4 && y < vh + 4;
    }

    /** Left/top of a 512x334 interface drawn over the view, in view pixels (centred clear of the panels). */
    public static int ix() { return active() ? Math.max(0, (vw - sw(SIDE_W) - 512) / 2) : 0; }
    public static int iy() { return active() ? Math.max(0, (vh - sw(CHAT_H) - 334) / 2) : 0; }
    /** The same in game mouse coordinates (the fixed layout's 4, 4). */
    public static int mx() { return active() ? OFF + 4 + ix() : 4; }
    public static int my() { return active() ? OFF + 4 + iy() : 4; }

    /** Size limits for a right-click menu opened over the view. */
    public static int menuW() { return active() ? vw : 512; }
    public static int menuH() { return active() ? vh : 334; }

    /** Canvas pixel -> game mouse coordinate {x, y}. */
    static int[] map(int x, int y, int w, int h) {
        if (fullscreenFixed) {
            int ox = Math.max(0, (w - FW) / 2), oy = Math.max(0, (h - FH) / 2);
            return new int[]{clamp(x - ox, FW), clamp(y - oy, FH)};
        }
        // an open view menu stays usable where it overlaps a panel
        if (Class37.anInt653 == 0 && Class39_Sub12.aBoolean1493) {
            int mx0 = Widget.anInt2063, my0 = Class39_Sub5_Sub7.anInt1784;
            if (x >= mx0 - 10 && x <= mx0 + Class14.anInt227 + 10 && y >= my0 - 10 && y <= my0 + jagex.utils.Huffmans.anInt757 + 10)
                return new int[]{x + OFF + 4, y + OFF + 4};
        }
        int[] p;
        if ((p = panelHit(x, y, w - sw(MAP_W), 0, MAP_X, MAP_Y, MAP_W, MAP_H, mapMask())) != null) return p;
        if ((p = panelHit(x, y, w - sw(SIDE_W), h - sw(SIDE_H), SIDE_X, SIDE_Y, SIDE_W, SIDE_H, sideMask())) != null) return p;
        if ((p = panelHit(x, y, 0, h - sw(CHAT_H), CHAT_X, CHAT_Y, CHAT_W, CHAT_H, null)) != null) return p;
        return new int[]{x + OFF + 4, y + OFF + 4};
    }

    /**
     * Panel scale: OSRS panels are drawn about a quarter larger than the 2006 ones, but never so large that the
     * minimap and side panel stop fitting one above the other, or the chat and side panel side by side.
     */
    static double scale() { return Math.min(1.25, Math.min(vw / (double) (CHAT_W + SIDE_W), vh / (double) (MAP_H + SIDE_H))); }
    static int sw(int n) { return (int) Math.round(n * scale()); }

    /** Fixed-layout coordinates of a canvas point on a scaled panel, or null when it misses the panel's shape. */
    private static int[] panelHit(int x, int y, int px, int py, int fx, int fy, int fw, int fh, boolean[] mask) {
        double s = scale();
        int lx = (int) Math.floor((x - px) / s), ly = (int) Math.floor((y - py) / s);
        if (x < px || y < py || lx >= fw || ly >= fh) return null;
        if (mask != null && !mask[ly * fw + lx]) return null;
        return new int[]{fx + lx, fy + ly};
    }

    private static int clamp(int v, int n) { return Math.max(0, Math.min(n - 1, v)); }

    // ----------------------------------------------------------------- render
    /**
     * Called from JImage.draw for every blit. Returns true when the blit was taken over (nothing goes to the
     * canvas until {@link #present}).
     */
    public static boolean capture(AbstractImage img, int[] px, int w, int h, int x, int y) {
        if (!active() && enabled && Class31.state >= 25) ensureView();   // the first in-game frame never shows the fixed layout
        if (!active()) return false;
        if (img == Class23.aClass57_435) { view = img; return true; }
        int sx = 0, sy = 0, cwid = w, chei = h;
        if (x < 0) { sx = -x; cwid += x; x = 0; }
        if (y < 0) { sy = -y; chei += y; y = 0; }
        if (x + cwid > FW) cwid = FW - x;
        if (y + chei > FH) chei = FH - y;
        for (int r = 0; r < chei; r++) System.arraycopy(px, (sy + r) * w + sx, fixed, (y + r) * FW + x, cwid);
        return true;
    }

    /** End of a frame: puts the view and the panels on the canvas in one blit. */
    static void present() {
        if (enabled && Class31.state >= 25) ensureView();     // straight into the layout, even on the loading screen
        if (!active()) {
            if (Gpu.presentedAt != 0) Gpu.releaseCanvas(ModHook.app);   // login screen / fixed layout: Java2D draws again
            fullscreenFixed = false;
            if (wasActive) { wasActive = false; ModHook.refresh(); }   // logging out: clear the last game frame
            return;
        }
        wasActive = true;
        java.awt.Component c = ModHook.app;
        if (!(c instanceof Canvas_Sub1)) return;
        int w = c.getWidth(), h = c.getHeight();
        if (w <= 0 || h <= 0) return;
        if (screen == null || screen.getWidth() != w || screen.getHeight() != h) {
            screen = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            screenPx = ((DataBufferInt) screen.getRaster().getDataBuffer()).getData();
        }
        fullscreenFixed = SubNode.anInt1348 != -1;
        if (fullscreenFixed || view == null || view.width != w || view.height != h)
            java.util.Arrays.fill(screenPx, 0);                // otherwise the view covers every pixel
        if (fullscreenFixed) {
            copy(fixed, FW, 0, 0, FW, FH, Math.max(0, (w - FW) / 2), Math.max(0, (h - FH) / 2), w, h);
        } else {
            Graphics2D g2 = screen.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (view != null) {
                if (view.width == w && view.height == h) copy(view.buffer, view.width, 0, 0, w, h, 0, 0, w, h);
                else g2.drawImage(wrap(view), 0, 0, w, h, null);   // the window was just resized: stretch the last frame
            }
            drawPanel(g2, 0, MAP_X, MAP_Y, MAP_W, MAP_H, mapMask(), w - sw(MAP_W), 0);
            drawPanel(g2, 1, SIDE_X, SIDE_Y, SIDE_W, SIDE_H, sideMask(), w - sw(SIDE_W), h - sw(SIDE_H));
            drawPanel(g2, 2, CHAT_X, CHAT_Y, CHAT_W, CHAT_H, null, 0, h - sw(CHAT_H));
            g2.dispose();
            // a right-click menu opened over the view is drawn above the panels
            if (view != null && Class37.anInt653 == 0 && Class39_Sub12.aBoolean1493)
                copy(view.buffer, view.width, Widget.anInt2063, Class39_Sub5_Sub7.anInt1784,
                    Class14.anInt227, jagex.utils.Huffmans.anInt757, Widget.anInt2063, Class39_Sub5_Sub7.anInt1784, w, h);
        }
        boolean direct = Gpu.direct;
        Gpu.direct = false;
        // once OpenGL has drawn to the canvas, Java2D can't show anything on it (NVIDIA): every frame goes through GL
        if (direct && !fullscreenFixed && view != null && view.width == w && view.height == h || Gpu.presentedAt != 0) {
            Graphics2D g2 = screen.createGraphics();          // the side buttons go into the frame, not onto the canvas
            ModHook.drawButtons(g2, w, h);
            g2.dispose();
            if (GpuPresent.present(c, screenPx, w, h)) return;
        }
        Graphics g = ((Canvas_Sub1) c).rawGraphics();
        if (g == null) return;
        g.drawImage(screen, 0, 0, null);
        g.dispose();
        ModHook.paintButtons();
    }

    /** True when the world being drawn (w x h at ox, oy) is the whole resizable view, so GpuPresent can show it. */
    static boolean directReady(int w, int h, int ox, int oy) {
        if (Gpu.presentFailed || !active() || SubNode.anInt1348 != -1 || ox != 0 || oy != 0) return false;
        java.awt.Component c = ModHook.app;
        return c instanceof Canvas_Sub1 && c.getWidth() == w && c.getHeight() == h
            && view != null && view.width == w && view.height == h && view.buffer == jagex.graphics.DrawingArea.buffer;
    }

    // per panel (map, side, chat): its cut-out at 2006 size, the last pixels it was cut from, and its scaled copy
    private static final BufferedImage[] panelImg = new BufferedImage[3], scaledImg = new BufferedImage[3];
    private static final int[][] panelSrc = new int[3][];
    private static BufferedImage viewImg;
    private static int[] viewImgPx;

    /** The view's pixels as an image, for stretching an old-size frame while the window is resized. */
    private static BufferedImage wrap(AbstractImage v) {
        if (viewImg == null || viewImgPx != v.buffer) {
            DataBufferInt db = new DataBufferInt(v.buffer, v.width * v.height);
            java.awt.image.WritableRaster r = java.awt.image.Raster.createPackedRaster(db, v.width, v.height, v.width, new int[]{0xff0000, 0xff00, 0xff}, null);
            viewImg = new BufferedImage(new java.awt.image.DirectColorModel(24, 0xff0000, 0xff00, 0xff), r, false, null);
            viewImgPx = v.buffer;
        }
        return viewImg;
    }

    /** A panel cut from the fixed frame, transparent outside its shape. */
    private static BufferedImage panel(BufferedImage img, int fx, int fy, int fw, int fh, boolean[] mask) {
        if (img == null) img = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_ARGB);
        int[] px = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        for (int y = 0; y < fh; y++) for (int x = 0; x < fw; x++) {
            int i = y * fw + x;
            px[i] = mask != null && !mask[i] ? 0 : 0xff000000 | fixed[(fy + y) * FW + fx + x];
        }
        return img;
    }

    /** Draws panel k at (x, y), scaling it again only when its 2006 pixels or the window scale changed. */
    private static void drawPanel(Graphics2D g, int k, int fx, int fy, int fw, int fh, boolean[] mask, int x, int y) {
        int tw = sw(fw), th = sw(fh);
        BufferedImage out = scaledImg[k];
        boolean resized = out == null || out.getWidth() != tw || out.getHeight() != th;
        if (changed(k, fx, fy, fw, fh) || resized) {
            panelImg[k] = panel(panelImg[k], fx, fy, fw, fh, mask);
            if (resized) out = scaledImg[k] = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
            Graphics2D s = out.createGraphics();
            s.setComposite(java.awt.AlphaComposite.Src);
            s.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            s.drawImage(panelImg[k], 0, 0, tw, th, null);
            s.dispose();
        }
        g.drawImage(out, x, y, null);
    }

    /** True when the panel's pixels in the fixed frame differ from last time (and remembers them). */
    private static boolean changed(int k, int fx, int fy, int fw, int fh) {
        int[] last = panelSrc[k];
        boolean diff = false;
        if (last == null) { last = panelSrc[k] = new int[fw * fh]; diff = true; }
        for (int y = 0, i = 0; y < fh; y++) {
            int row = (fy + y) * FW + fx;
            for (int x = 0; x < fw; x++, i++) {
                int p = fixed[row + x];
                if (last[i] != p) { last[i] = p; diff = true; }
            }
        }
        return diff;
    }

    // Panel shapes: the square stone around the minimap's dragon ring and beside the side panel's frame is left out.
    private static boolean[] mapMask, sideMask;

    private static boolean[] mapMask() {
        if (mapMask != null) return mapMask;
        boolean[] m = new boolean[MAP_W * MAP_H];
        for (int y = 0; y < MAP_H; y++) for (int x = 0; x < MAP_W; x++) {
            int fx = MAP_X + x, fy = MAP_Y + y;
            // the dragon ring's outline: x 546-755, top corners radius 70, bottom corners radius 25
            boolean ring = fx >= 546 && fx <= 755
                && !cutCorner(fx, fy, 616, 70, -1, -1, 70) && !cutCorner(fx, fy, 685, 70, 1, -1, 70)
                && !cutCorner(fx, fy, 571, 143, -1, 1, 25) && !cutCorner(fx, fy, 730, 143, 1, 1, 25);
            int cx = fx - 566, cy = fy - 21;
            boolean compass = cx * cx + cy * cy <= 22 * 22;
            m[y * MAP_W + x] = ring || compass;
        }
        return mapMask = m;
    }

    private static boolean[] sideMask() {
        if (sideMask != null) return sideMask;
        boolean[] m = new boolean[SIDE_W * SIDE_H];
        final int left = 525 - SIDE_X, r = 12;
        for (int y = 0; y < SIDE_H; y++) for (int x = 0; x < SIDE_W; x++) {
            boolean keep = x >= left;
            if (keep) keep = !cutCorner(x, y, left + r, r, -1, -1, r)                   // top left
                && !cutCorner(x, y, SIDE_W - 1 - r, r, 1, -1, r)                      // top right
                && !cutCorner(x, y, left + r, SIDE_H - 1 - r, -1, 1, r);             // bottom left
            m[y * SIDE_W + x] = keep;
        }
        return sideMask = m;
    }

    /** True when (x, y) is beyond the quarter circle of radius r centred at (cx, cy), in the quadrant (sx, sy). */
    private static boolean cutCorner(int x, int y, int cx, int cy, int sx, int sy, int r) {
        int dx = (x - cx) * sx, dy = (y - cy) * sy;
        return dx > 0 && dy > 0 && dx * dx + dy * dy > r * r;
    }

    private static void copy(int[] src, int sw, int sx, int sy, int w, int h, int dx, int dy, int dw, int dh) {
        if (dx < 0) { sx -= dx; w += dx; dx = 0; }
        if (dy < 0) { sy -= dy; h += dy; dy = 0; }
        if (dx + w > dw) w = dw - dx;
        if (dy + h > dh) h = dh - dy;
        if (w <= 0 || h <= 0) return;
        for (int r = 0; r < h; r++) System.arraycopy(src, (sy + r) * sw + sx, screenPx, (dy + r) * dw + dx, w);
    }

    /**
     * Before the 3D view is drawn and at the end of every frame: resizes the view image and the rasterizer bounds
     * to the canvas (or back to 512x334) as soon as the canvas changes size.
     */
    public static void ensureView() {
        java.awt.Component c = ModHook.app;
        int w = 512, h = 334;
        if (enabled && c != null && Class31.state >= 25 && c.getWidth() >= FW && c.getHeight() >= FH) {
            w = c.getWidth(); h = c.getHeight();
        }
        AbstractImage img = Class23.aClass57_435;              // the game recreates it at 512x334 when logging in
        if (w == vw && h == vh && img != null && img.width == w && img.height == h) { cw = w > 512 ? w : 0; ch = h; return; }
        rebuild(c, w, h);
    }

    /** Size the scene's visibility tables were last built for (the game builds them for 512x334 at start-up). */
    private static int tablesW = 512, tablesH = 334;

    private static void rebuild(java.awt.Component c, int w, int h) {
        if (c == null) return;
        Class23.aClass57_435 = Queue.method994(c, w, h, (byte) -104);
        Class39_Sub5_Sub7.anIntArray1789 = null;               // line offsets for the old size
        sceneZoom = zoomFor(h);
        // Tables built for a larger view are still right for a smaller one (they only decide which tiles may be
        // seen), so they are built once, for the whole screen, instead of on every resize (about a second each).
        if (w > tablesW || h > tablesH) {
            java.awt.Rectangle sb = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            int tw = Math.max(w, sb.width), th = Math.max(h, sb.height);
            int[] is = new int[9];
            for (int i = 0; i < 9; i++) {
                int a = i * 32 + 128 + 15;
                is[i] = Class39_Sub5_Sub10_Sub2.sineTable[a] * (a * 3 + 600) >> 16;
            }
            zoom = zoomFor(th);                                // the visibility tables are projected like the world
            Class38.method403(is, 500, 2400, tw, th);
            zoom = 512;
            tablesW = tw; tablesH = th;
        }
        vw = w; vh = h;
        cw = w > 512 ? w : 0; ch = h;
        ClientScript.aBoolean1690 = true;                      // redraw every panel
        if (w == 512) ModHook.refresh();
    }
}
