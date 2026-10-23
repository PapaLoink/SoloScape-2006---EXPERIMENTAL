package unpackaged;

import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import jagex.graphics.DrawingArea;
import jagex.utils.Cache;
import jagex.world.actors.Player;

/** Christmas 2006 snowfall over the original southern Karamja snow footprint. */
public final class ChristmasEventSnow {
    /** Legacy Trollweiss atmospheric overlay retained separately from Karamja weather. */
    static final int OVERLAY_GROUP = 11877;

    private static final int MODEL_SNOWFALL_2 = 4962; // com_i357
    private static final int MODEL_SNOWFALL = 4961;   // com_i358
    private static final int SEQ_SNOWFALL_2 = 1490;
    private static final int SEQ_SNOWFALL = 1489;
    // GublinchChristmasEvent's overworld dungeon entrance (object 19039).
    private static final int DUNGEON_ENTRANCE_X = 2843;
    private static final int DUNGEON_ENTRANCE_Y = 3141;
    private static final int DUNGEON_SNOW_RADIUS = 10;

    private static final int VIEW_WIDTH = 512;
    private static final int VIEW_HEIGHT = 334;
    private static final int SNOWBALL_COUNT = 56;
    private static final int SNOWBALL_RADIUS = 5; // Use the largest original recreation size for every ball.
    private static final int PASS_TICKS = 150;    // ~50 client logic ticks/sec: one viewport pass is about 3 seconds.
    private static final int PATH_START_X = VIEW_WIDTH + SNOWBALL_RADIUS;
    private static final int PATH_START_Y = -SNOWBALL_RADIUS;
    private static final int PATH_END_X = -SNOWBALL_RADIUS;
    private static final int PATH_END_Y = VIEW_HEIGHT + SNOWBALL_RADIUS;
    private static final int OFFSET_MIN = -180;
    private static final int OFFSET_SPAN = 360;
    private static final int MIST_COLOR = 0xf3f7fb;
    private static final int MIST_ALPHA = 42;     // Soft white haze beneath the snowballs.
    private static final int HEAVY_MIST_ALPHA = 128; // Much thicker haze throughout the extra snow layer.
    private static final int SMALL_SNOWBALL_COUNT = 540;
    private static final int SMALL_SNOWBALL_RADIUS = 4;
    private static final int KITE_WIDTH = 7;
    private static final int KITE_HEIGHT = 6;
    private static int[] kitePixels;
    private static final int SMALL_OFFSET_MIN = -210;
    private static final int SMALL_OFFSET_SPAN = 420;

    /*
     * Parallel screen-space paths for the Karamja Christmas weather. Every ball
     * uses the same start/end vector and pass duration; offsets only move the path
     * sideways so particles remain separated while travelling together.
     */
    // Mutable so development hot-reloads can safely resize these when particle counts change.
    private static int[] pathOffsets = new int[SNOWBALL_COUNT];
    private static int[] phases = new int[SNOWBALL_COUNT];
    private static int[] smallPathOffsets = new int[SMALL_SNOWBALL_COUNT];
    private static int[] smallPhases = new int[SMALL_SNOWBALL_COUNT];

    private static final boolean[][] snowTiles = new boolean[104][104];
    private static final boolean[][] brightSnowTiles = new boolean[104][104];
    private static int baseX, baseY;

    static {
        reseedParticleArrays();
    }

    private static void ensureParticleArrays() {
        if (pathOffsets == null || pathOffsets.length != SNOWBALL_COUNT
                || phases == null || phases.length != SNOWBALL_COUNT
                || smallPathOffsets == null || smallPathOffsets.length != SMALL_SNOWBALL_COUNT
                || smallPhases == null || smallPhases.length != SMALL_SNOWBALL_COUNT) {
            reseedParticleArrays();
        }
    }

    private static void reseedParticleArrays() {
        pathOffsets = new int[SNOWBALL_COUNT];
        phases = new int[SNOWBALL_COUNT];
        smallPathOffsets = new int[SMALL_SNOWBALL_COUNT];
        smallPhases = new int[SMALL_SNOWBALL_COUNT];

        /*
         * Keep the 2006-style irregular ahead/behind stagger, while distributing
         * parallel paths across the viewport. Each particle still follows exactly
         * the same motion vector for the same three-second pass.
         */
        int seed = 0x20061218;
        for (int i = 0; i < SNOWBALL_COUNT; i++) {
            int slot = i * 17 % SNOWBALL_COUNT;
            seed = seed * 1103515245 + 12345;
            int jitter = ((seed >>> 16) & 7) - 3;
            pathOffsets[i] = OFFSET_MIN + slot * OFFSET_SPAN / (SNOWBALL_COUNT - 1) + jitter;

            seed = seed * 1103515245 + 12345;
            phases[i] = ((seed >>> 1) & 0x7fffffff) % PASS_TICKS;
        }

        // Dense foreground/background flurry using the supplied faceted kite artwork.
        for (int i = 0; i < SMALL_SNOWBALL_COUNT; i++) {
            int slot = i * 73 % SMALL_SNOWBALL_COUNT;
            seed = seed * 1103515245 + 12345;
            int jitter = ((seed >>> 16) & 15) - 7;
            smallPathOffsets[i] = SMALL_OFFSET_MIN
                    + slot * SMALL_OFFSET_SPAN / (SMALL_SNOWBALL_COUNT - 1) + jitter;

            seed = seed * 1103515245 + 12345;
            smallPhases[i] = ((seed >>> 1) & 0x7fffffff) % PASS_TICKS;
        }
    }

    private ChristmasEventSnow() {}

    // Original white stone/fleck decorations from the snowy Gublinch cave.
    private static final int[] GROUND_SNOW_OBJECTS = {20129, 20130, 20131};

    /** Request these extra scenery models during the normal region readiness pass. */
    static boolean groundSnowModelsReady() {
        if (Class63.aBoolean1120 || Class65.anInt1145 > 2879
                || Class65.anInt1145 + 103 < 2816 || JKeyListener.anInt618 > 3199
                || JKeyListener.anInt618 + 103 < DUNGEON_ENTRANCE_Y - DUNGEON_SNOW_RADIUS) {
            return true;
        }
        boolean ready = true;
        for (int id : GROUND_SNOW_OBJECTS) {
            ready &= Canvas_Sub1.method40(id, (byte) 65).method731(0);
        }
        return ready;
    }

    /** Build persistent, non-blocking snow scenery before terrain lighting/cleanup. */
    static void buildOutdoorGroundSnow(Class38 scene) {
        byte[][][] floors = Class39_Sub14.aByteArrayArrayArray1516;
        byte[][][] overlays = Class33.aByteArrayArrayArray596;
        if (!Class63.aBoolean1120 && floors != null && overlays != null) {
            for (int x = 1; x < 103; x++) for (int y = 1; y < 103; y++) {
                // Extend the white ground to the dry approach tiles with snowfall.
                // Keep water, lava, paths, bridge tiles and blocked terrain intact.
                if (isNearChristmasDungeon(Class65.anInt1145 + x, JKeyListener.anInt618 + y, 0)
                        && floors[0][x][y] != 0 && overlays[0][x][y] == 0
                        && (Class55.tileFlags[0][x][y] & 3) == 0) {
                    floors[0][x][y] = 58;
                }
            }
        }
        captureGroundSnow();
        if (Class63.aBoolean1120 || scene == null || floors == null || overlays == null) return;

        for (int x = 1; x < 103; x++) for (int y = 1; y < 103; y++) {
            int worldX = baseX + x, worldY = baseY + y;
            if ((!isSnowArea(worldX, worldY, 0) && !isNearChristmasDungeon(worldX, worldY, 0))
                    || floors[0][x][y] == 0 || overlays[0][x][y] != 0
                    || (Class55.tileFlags[0][x][y] & 3) != 0
                    || scene.method400(0, x, y) != null) continue;

            // World coordinates keep the pattern stable across region reloads/rebasing.
            int seed = worldX * 73428767 ^ worldY * 912931;
            int id = GROUND_SNOW_OBJECTS[(seed & 0x7fffffff) % GROUND_SNOW_OBJECTS.length];
            int rotation = (seed >>> 8) & 3;
            int h00 = Class67.heightMap[0][x][y];
            int h01 = Class67.heightMap[0][x][y + 1];
            int h11 = Class67.heightMap[0][x + 1][y + 1];
            int h10 = Class67.heightMap[0][x + 1][y];
            Model model = Canvas_Sub1.method40(id, (byte) 65)
                    .method742(66, 22, h00, rotation, h01, h11, h10);
            int tag = 0xc0000000 | (id << 14) | (y << 7) | x;
            // Place only in the scene: never alter walking/projectile collision maps.
            scene.method381(0, x, y, (h00 + h01 + h11 + h10) >> 2,
                    model, tag, 22 | (rotation << 6));
        }
    }

    /** Record the cache's footprint before the temporary floor arrays are discarded. */
    static void captureGroundSnow() {
        baseX = Class65.anInt1145;
        baseY = JKeyListener.anInt618;
        byte[][][] floors = Class39_Sub14.aByteArrayArrayArray1516;
        for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) {
            int floor = floors == null ? 0 : floors[0][x][y] & 255;
            boolean inKaramjaSnow = !Class63.aBoolean1120
                    && (baseX + x) >> 6 == 44 && (baseY + y) >> 6 == 49;
            snowTiles[x][y] = inKaramjaSnow && floor >= 58 && floor <= 60;
            brightSnowTiles[x][y] = inKaramjaSnow && floor == 58;
        }
    }

    static boolean isSnowArea(int x, int y, int plane) {
        x -= baseX;
        y -= baseY;
        return plane == 0 && x >= 0 && y >= 0 && x < 104 && y < 104 && snowTiles[x][y];
    }

    static boolean isBrightSnowArea(int x, int y, int plane) {
        x -= baseX;
        y -= baseY;
        return plane == 0 && x >= 0 && y >= 0 && x < 104 && y < 104 && brightSnowTiles[x][y];
    }

    static boolean isNearChristmasDungeon(int x, int y, int plane) {
        return !Class63.aBoolean1120 && plane == 0
                && Math.abs(x - DUNGEON_ENTRANCE_X) <= DUNGEON_SNOW_RADIUS
                && Math.abs(y - DUNGEON_ENTRANCE_Y) <= DUNGEON_SNOW_RADIUS;
    }

    /**
     * Supplies the legacy Trollweiss overlay (11877) that is absent as a JS5
     * interface group in the hybrid client.  This is intentionally not used for
     * the Christmas 2006 Karamja weather below.
     */
    public static boolean loadOverlayWidget(int group) {
        if (group != OVERLAY_GROUP) return false;

        int required = OVERLAY_GROUP + 1;
        if (Class62_Sub1.widgets == null) {
            Class62_Sub1.widgets = new Widget[required][];
        } else if (Class62_Sub1.widgets.length < required) {
            Class62_Sub1.widgets = Arrays.copyOf(Class62_Sub1.widgets, required);
        }
        if (Class39_Sub5_Sub4.widgetsLoaded == null) {
            Class39_Sub5_Sub4.widgetsLoaded = new boolean[required];
        } else if (Class39_Sub5_Sub4.widgetsLoaded.length < required) {
            Class39_Sub5_Sub4.widgetsLoaded = Arrays.copyOf(Class39_Sub5_Sub4.widgetsLoaded, required);
        }
        if (Class62_Sub1.widgets[OVERLAY_GROUP] != null
                && Class39_Sub5_Sub4.widgetsLoaded[OVERLAY_GROUP]) {
            return true;
        }

        Widget[] widgets = new Widget[3];
        Class62_Sub1.widgets[OVERLAY_GROUP] = widgets;

        Widget root = widgets[0] = new Widget();
        root.anInt2084 = OVERLAY_GROUP << 16;
        root.anInt2050 = -1;
        root.type = 0;
        root.quadWidth = root.anInt2020 = 512;
        root.quadHeight = root.anInt2095 = 334;
        root.aBoolean2055 = false;

        widgets[1] = modelWidget(1, MODEL_SNOWFALL_2, SEQ_SNOWFALL_2);
        widgets[2] = modelWidget(2, MODEL_SNOWFALL, SEQ_SNOWFALL);

        Class39_Sub5_Sub4.widgetsLoaded[OVERLAY_GROUP] = true;
        return true;
    }

    private static Widget modelWidget(int child, int modelId, int sequenceId) {
        Widget widget = new Widget();
        widget.anInt2084 = OVERLAY_GROUP << 16 | child;
        widget.anInt2050 = OVERLAY_GROUP << 16;
        widget.type = 6;
        widget.aBoolean2055 = false;
        widget.anInt2090 = widget.anInt2091 = 248;
        widget.anInt2024 = widget.anInt2021 = 354;
        widget.quadWidth = 32;
        widget.quadHeight = 32;
        widget.anInt2009 = 1;
        widget.anInt2026 = modelId;
        widget.anInt2103 = sequenceId;
        widget.anInt2074 = 200;
        return widget;
    }

    /** Draw the Karamja weather as screen-space snowballs sweeping top-right to bottom-left. */
    static void drawSnowfall() {
        Player player = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        if (Class63.aBoolean1120 || player == null || DrawingArea.buffer == null
                || NameTable.height != 0 || baseX != Class65.anInt1145 || baseY != JKeyListener.anInt618) return;

        int px = player.anInt2301 >> 7;
        int py = player.anInt2275 >> 7;
        int worldX = baseX + px;
        int worldY = baseY + py;
        boolean groundSnow = isSnowArea(worldX, worldY, 0);
        boolean nearDungeon = isNearChristmasDungeon(worldX, worldY, 0);
        if (!groundSnow && !nearDungeon) return;

        boolean heavySnow = nearDungeon || isBrightSnowArea(worldX, worldY, 0);
        drawMist(heavySnow ? HEAVY_MIST_ALPHA : MIST_ALPHA);

        int tick = Class2.logicCycle;
        if (heavySnow) {
            drawDenseSmallSnow(tick);
        }
        if (!groundSnow) return;
        for (int i = 0; i < SNOWBALL_COUNT; i++) {
            drawSnowball(snowballX(i, tick), snowballY(i, tick), SNOWBALL_RADIUS,
                    snowballSpin(i, tick));
        }
    }

    /** Dense viewport flurry using the user's faceted kite sprite. */
    private static void drawDenseSmallSnow(int tick) {
        for (int i = 0; i < SMALL_SNOWBALL_COUNT; i++) {
            int phase = smallSnowPhase(i, tick);
            int x = VIEW_WIDTH + SMALL_SNOWBALL_RADIUS
                    + (-VIEW_WIDTH - SMALL_SNOWBALL_RADIUS * 2) * phase / (PASS_TICKS - 1)
                    + smallPathOffsets[i] * 2 / 3;
            int y = -SMALL_SNOWBALL_RADIUS
                    + (VIEW_HEIGHT + SMALL_SNOWBALL_RADIUS * 2) * phase / (PASS_TICKS - 1)
                    + smallPathOffsets[i];
            drawSmallCacheFlake(x, y, 190 + ((i * 17) & 31), smallSnowSpin(i, tick));
        }
    }

    static int smallSnowPhase(int index, int tick) {
        ensureParticleArrays();
        int phase = (smallPhases[index] + tick) % PASS_TICKS;
        if (phase < 0) phase += PASS_TICKS;
        return phase;
    }

    static int smallSnowballCount() {
        return SMALL_SNOWBALL_COUNT;
    }

    static int smallSnowSpin(int index, int tick) {
        ensureParticleArrays();
        return ((tick + smallPhases[index]) * 2048 / 50) & 2047;
    }

    /** Fill the lower-right triangle directly so small flakes cannot break into sampled dashes. */
    private static void drawSmallCacheFlake(int cx, int cy, int alpha, int spin) {
        int sin = Model.anIntArray2394[spin];
        int cos = Model.anIntArray2394[(spin + 512) & 2047];
        int x0 = -KITE_WIDTH / 2, y0 = -KITE_HEIGHT / 2;
        int x1 = x0 + KITE_WIDTH - 1, y1 = y0 + KITE_HEIGHT / 2;
        int x2 = x0 + KITE_WIDTH / 3, y2 = y0 + KITE_HEIGHT - 1;
        drawTinyTriangle(cx + rotateX(x0, y0, sin, cos), cy + rotateY(x0, y0, sin, cos),
                cx + rotateX(x1, y1, sin, cos), cy + rotateY(x1, y1, sin, cos),
                cx + rotateX(x2, y2, sin, cos), cy + rotateY(x2, y2, sin, cos),
                0xffffff, alpha);
    }

    private static int rotateX(int x, int y, int sin, int cos) {
        return (int) Math.round((x * cos - y * sin) / 65536.0);
    }

    private static int rotateY(int x, int y, int sin, int cos) {
        return (int) Math.round((x * sin + y * cos) / 65536.0);
    }
    private static void drawTinyTriangle(int x0, int y0, int x1, int y1,
                                         int x2, int y2, int color, int alpha) {
        int minX = Math.max(0, Math.min(x0, Math.min(x1, x2)));
        int maxX = Math.min(Math.min(VIEW_WIDTH, DrawingArea.bufferWidth) - 1,
                Math.max(x0, Math.max(x1, x2)));
        int minY = Math.max(0, Math.min(y0, Math.min(y1, y2)));
        int maxY = Math.min(Math.min(VIEW_HEIGHT, DrawingArea.bufferHeight) - 1,
                Math.max(y0, Math.max(y1, y2)));
        int area = edge(x0, y0, x1, y1, x2, y2);
        if (area == 0) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                int w0 = edge(x1, y1, x2, y2, x, y);
                int w1 = edge(x2, y2, x0, y0, x, y);
                int w2 = edge(x0, y0, x1, y1, x, y);
                if ((area > 0 && w0 >= 0 && w1 >= 0 && w2 >= 0)
                        || (area < 0 && w0 <= 0 && w1 <= 0 && w2 <= 0)) {
                    blendPixel(x + y * DrawingArea.bufferWidth, color, alpha);
                }
            }
        }
    }

    private static int edge(int ax, int ay, int bx, int by, int px, int py) {
        return (px - ax) * (by - ay) - (py - ay) * (bx - ax);
    }

    private static void drawMist(int alpha) {
        int width = Math.min(VIEW_WIDTH, DrawingArea.bufferWidth);
        int height = Math.min(VIEW_HEIGHT, DrawingArea.bufferHeight);
        DrawingArea.drawQuadOverlay(0, 0, width, height, MIST_COLOR, alpha);
    }

    static int snowballX(int index, int tick) {
        int phase = snowballPhase(index, tick);
        int base = PATH_START_X + (PATH_END_X - PATH_START_X) * phase / (PASS_TICKS - 1);
        return base + pathOffsets[index] * 2 / 3;
    }

    static int snowballY(int index, int tick) {
        int phase = snowballPhase(index, tick);
        int base = PATH_START_Y + (PATH_END_Y - PATH_START_Y) * phase / (PASS_TICKS - 1);
        return base + pathOffsets[index];
    }

    static int snowballPhase(int index, int tick) {
        ensureParticleArrays();
        int phase = (phases[index] + tick) % PASS_TICKS;
        if (phase < 0) phase += PASS_TICKS;
        return phase;
    }

    static int snowballSpin(int index, int tick) {
        ensureParticleArrays();
        return ((tick + phases[index]) * 2048 / 50) & 2047; // one visible rotation per second
    }

    static int snowballCount() {
        return SNOWBALL_COUNT;
    }

    static int snowballPassTicks() {
        return PASS_TICKS;
    }

    private static void drawSnowball(int cx, int cy, int radius, int spin) {
        int maxX = Math.min(VIEW_WIDTH, DrawingArea.bufferWidth);
        int maxY = Math.min(VIEW_HEIGHT, DrawingArea.bufferHeight);
        int r2 = radius * radius;
        int sin = Model.anIntArray2394[spin];
        int cos = Model.anIntArray2394[(spin + 512) & 2047];
        for (int dy = -radius; dy <= radius; dy++) {
            int y = cy + dy;
            if (y < 0 || y >= maxY) continue;
            for (int dx = -radius; dx <= radius; dx++) {
                int x = cx + dx;
                if (x < 0 || x >= maxX || dx * dx + dy * dy > r2) continue;

                // Rotate the light/shadow across the sphere so the snowball visibly
                // spins while retaining the chunky 2006 software-rendered appearance.
                int dot = (dx * cos + dy * sin) >> 16;
                int color;
                int alpha;
                if (dot <= -2) {
                    color = 0xffffff;
                    alpha = 238;
                } else if (dot >= 2) {
                    color = 0xb7c2cb;
                    alpha = 205;
                } else {
                    color = 0xe8eef2;
                    alpha = 226;
                }
                blendPixel(x + y * DrawingArea.bufferWidth, color, alpha);
            }
        }
    }

    private static void blendPixel(int offset, int color, int alpha) {
        int under = DrawingArea.buffer[offset];
        int inverse = 256 - alpha;
        int rb = ((color & 0xff00ff) * alpha + (under & 0xff00ff) * inverse) & 0xff00ff00;
        int g = ((color & 0x00ff00) * alpha + (under & 0x00ff00) * inverse) & 0x00ff0000;
        DrawingArea.buffer[offset] = (rb | g) >>> 8;
    }
}



