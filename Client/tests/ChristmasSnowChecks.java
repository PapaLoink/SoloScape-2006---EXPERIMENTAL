package unpackaged;

import com.rs2.cache.js5.Js5CacheStore;
import com.rs2.cache.js5.MapGroups;
import java.io.File;
import java.util.Arrays;
import jagex.graphics.DrawingArea;
import jagex.io.Buffer;
import jagex.utils.Cache;
import jagex.utils.Deque;
import jagex.world.actors.Player;

/** Run from Server with client/server classes and Server/lib on the classpath. */
public final class ChristmasSnowChecks {
    public static void main(String[] args) throws Exception {
        Class65.anInt1145 = 2816;
        JKeyListener.anInt618 = 3136;
        Class39_Sub14.aByteArrayArrayArray1516 = new byte[4][104][104];
        Class33.aByteArrayArrayArray596 = new byte[4][104][104];
        Class67.aByteArrayArrayArray1179 = new byte[4][104][104];
        jagex.utils.IsaacPrng.aByteArrayArrayArray1081 = new byte[4][104][104];
        Class67.heightMap = new int[4][105][105];
        Class55.tileFlags = new byte[4][104][104];

        byte[] terrain;
        try (Js5CacheStore store = new Js5CacheStore(new File("cache"))) {
            terrain = MapGroups.readTerrain(store, store.readReferenceTable(5), 44, 49);
            require(store.readFiles(7, 4961).get(0) != null, "Missing cache model 4961/com_i358");
            require(store.readFiles(7, 4962).get(0) != null, "Missing cache model 4962/com_i357");
            require(store.readFiles(2, 12).get(1489) != null, "Missing cache sequence 1489/snowfall");
            require(store.readFiles(2, 12).get(1490) != null, "Missing cache sequence 1490/snowfall2");
        }
        Buffer data = new Buffer(terrain);
        for (int plane = 0; plane < 4; plane++) for (int x = 0; x < 64; x++)
            for (int y = 0; y < 64; y++) Deque.loadMapTile(data, 0, 0, y, 0, x, plane);

        byte[][][] original = copy(Class39_Sub14.aByteArrayArrayArray1516);
        ChristmasEventSnow.captureGroundSnow();
        require(Arrays.deepEquals(original, Class39_Sub14.aByteArrayArrayArray1516),
                "Snow modified original terrain");
        require(ChristmasEventSnow.isSnowArea(2844, 3152, 0),
                "Southern native snow was not detected");
        require(!ChristmasEventSnow.isSnowArea(2856, 3162, 0),
                "Snow still extends onto the dry east side");
        require(!ChristmasEventSnow.isSnowArea(2844, 3152, 1),
                "Snow detected on upper plane");
        require(ChristmasEventSnow.isBrightSnowArea(2823, 3136, 0),
                "Bright floor 58 was not detected for the dense small-snow trigger");
        require(!ChristmasEventSnow.isBrightSnowArea(2843, 3150, 0),
                "Darker floor 59 incorrectly triggers the dense small-snow layer");
        require(!ChristmasEventSnow.isBrightSnowArea(2823, 3136, 1),
                "Bright snow trigger leaked onto an upper plane");

        // Development hot-reload can preserve old static array instances even when
        // particle-count constants change. Reproduce the reported index-38 crash
        // and verify the snow code repairs stale arrays before indexing them.
        verifyHotReloadArrayRepair();

        // The floor decoder's temporary arrays are freed after building the scene.
        Class39_Sub14.aByteArrayArrayArray1516 = null;
        require(ChristmasEventSnow.isSnowArea(2844, 3152, 0),
                "Footprint lost after floor arrays freed");
        require(ChristmasEventSnow.isBrightSnowArea(2823, 3136, 0),
                "Bright snow trigger lost after floor arrays freed");
        for (int[] position : new int[][]{{2904, 3186}, {3164, 5324}, {2880, 3162}}) {
            require(!ChristmasEventSnow.isSnowArea(position[0], position[1], 0),
                    "Snowfall leaked outside southern volcano at "
                            + position[0] + "," + position[1]);
        }

        // The reference footage has irregular depth along the fall direction: some
        // balls are nearly level while others are visibly far ahead or behind.
        int[] startY = new int[ChristmasEventSnow.snowballCount()];
        for (int i = 0; i < startY.length; i++) startY[i] = ChristmasEventSnow.snowballY(i, 0);
        Arrays.sort(startY);
        int minGap = Integer.MAX_VALUE;
        int maxGap = 0;
        for (int i = 1; i < startY.length; i++) {
            int gap = startY[i] - startY[i - 1];
            if (gap < minGap) minGap = gap;
            if (gap > maxGap) maxGap = gap;
        }
        require(minGap <= 2, "Snowballs are still too uniformly staggered");
        require(maxGap >= 20, "Snowballs do not have enough ahead/behind separation");
        require(startY[startY.length - 1] - startY[0] > 400,
                "Snowball phases do not cover the full travel field");

        // A complete pass is 150 client logic ticks (about three seconds at 50 Hz),
        // and every particle uses the same displacement vector despite its own offset.
        require(ChristmasEventSnow.snowballPassTicks() == 150,
                "Snowball viewport pass is not configured for three seconds");
        Integer expectedDx = null;
        Integer expectedDy = null;
        for (int i = 0; i < Math.min(8, ChristmasEventSnow.snowballCount()); i++) {
            int startTick = -1;
            for (int tick = 0; tick < ChristmasEventSnow.snowballPassTicks(); tick++) {
                if (ChristmasEventSnow.snowballPhase(i, tick) == 0) {
                    startTick = tick;
                    break;
                }
            }
            require(startTick >= 0, "Could not locate snowball pass start");

            int x0 = ChristmasEventSnow.snowballX(i, startTick);
            int y0 = ChristmasEventSnow.snowballY(i, startTick);
            int x1 = ChristmasEventSnow.snowballX(i, startTick + 149);
            int y1 = ChristmasEventSnow.snowballY(i, startTick + 149);
            int dx = x1 - x0;
            int dy = y1 - y0;
            require(dx < 0 && dy > 0, "Snowball is not crossing top-right to bottom-left");
            if (expectedDx == null) {
                expectedDx = dx;
                expectedDy = dy;
            } else {
                require(dx == expectedDx && dy == expectedDy,
                        "Snowballs do not share the same full-pass speed/vector");
            }
            require(ChristmasEventSnow.snowballX(i, startTick + 150) == x0
                            && ChristmasEventSnow.snowballY(i, startTick + 150) == y0,
                    "Snowball pass does not loop after 150 ticks");
            require(ChristmasEventSnow.snowballSpin(i, startTick + 1)
                            != ChristmasEventSnow.snowballSpin(i, startTick),
                    "Snowball rotation is not advancing");
        }

        Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109 = new Player();
        Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301 = (2844 - Class65.anInt1145) * 128 + 64;
        Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275 = (3152 - JKeyListener.anInt618) * 128 + 64;
        NameTable.height = 0;
        int[] pixels = new int[512 * 334];
        DrawingArea.setBuffer(pixels, 512, 334);
        Class2.logicCycle = 100;
        ChristmasEventSnow.drawSnowfall();
        int mist = pixels[0];
        require(mist != 0, "Karamja snow mist was not drawn");
        long mistPixels = Arrays.stream(pixels).filter(pixel -> pixel == mist).count();
        long snowPixels = Arrays.stream(pixels).filter(pixel -> pixel != mist).count();
        require(mistPixels > pixels.length / 2, "Karamja mist is not covering the snowy viewport");
        require(snowPixels > 100, "Karamja snowballs were not visible above the mist");

        // Bright floor 58 adds a dense layer of the cache-style tiny two-triangle
        // particles, while the darker snow keeps only the normal large layer.
        require(ChristmasEventSnow.smallSnowballCount() >= 150,
                "Dense small-snow layer is not actually dense");
        Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301 = (2823 - Class65.anInt1145) * 128 + 64;
        Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275 = (3136 - JKeyListener.anInt618) * 128 + 64;
        int[] brightPixels = new int[512 * 334];
        DrawingArea.setBuffer(brightPixels, 512, 334);
        ChristmasEventSnow.drawSnowfall();
        int brightMist = brightPixels[0];
        require((brightMist >> 16 & 255) > (mist >> 16 & 255) * 2,
                "Extra snow layer did not make the viewport substantially mistier");
        long brightSnowPixels = Arrays.stream(brightPixels).filter(pixel -> pixel != brightMist).count();
        require(brightSnowPixels > snowPixels + 100,
                "Bright tiles did not add a visibly denser small-particle layer");

        verifyDungeonApproachSnow();
        verifyKiteArtwork();

        // 11877 remains the separate legacy Trollweiss atmospheric model overlay.
        require(!ChristmasEventSnow.loadOverlayWidget(11876),
                "Snow widget loader claimed an unrelated interface");
        require(ChristmasEventSnow.loadOverlayWidget(11877),
                "Legacy snow overlay did not load");
        Widget[] snow = Class62_Sub1.widgets[11877];
        require(snow != null && snow.length == 3, "Unexpected snow widget layout");
        require(snow[0].type == 0 && snow[0].anInt2050 == -1,
                "Snow overlay root is invalid");
        checkModel(snow[1], 4962, 1490, "com_i357/snowfall2");
        checkModel(snow[2], 4961, 1489, "com_i358/snowfall");

        // Instances must not inherit the overworld footprint when it is recaptured.
        Class63.aBoolean1120 = true;
        Class39_Sub14.aByteArrayArrayArray1516 = copy(original);
        ChristmasEventSnow.captureGroundSnow();
        require(!ChristmasEventSnow.isSnowArea(2844, 3152, 0),
                "Snow footprint leaked into an instance");
        Class63.aBoolean1120 = false;

        // Region rebasing must preserve the same world tile after a fresh capture.
        Class65.anInt1145 = 2824;
        Class39_Sub14.aByteArrayArrayArray1516 = new byte[4][104][104];
        Class39_Sub14.aByteArrayArrayArray1516[0][20][16] = original[0][28][16];
        ChristmasEventSnow.captureGroundSnow();
        require(ChristmasEventSnow.isSnowArea(2844, 3152, 0),
                "Snow moved after region rebase");

        verifyOutdoorGroundSnow();
        System.out.println("Christmas snow checks passed: native footprint, snowballs, bright-tile flakes, ten-tile dungeon approach flakes, viewport clipping, overlay, instances and region rebasing.");
    }

    /** Exercise scene placement using the real cave definitions and model bytes. */
    private static void verifyOutdoorGroundSnow() throws Exception {
        final java.util.Map<Integer, byte[]> definitions;
        final java.util.Map<Integer, byte[]> models = new java.util.HashMap<Integer, byte[]>();
        try (Js5CacheStore store = new Js5CacheStore(new File("cache"))) {
            definitions = store.readFiles(2, 6);
            for (int id : new int[]{20129, 20130, 20131}) {
                ObjectDefinition definition = new ObjectDefinition();
                definition.decode(new Buffer(definitions.get(id)));
                require(definition.anIntArray1960[0] == 22, "Snow asset is not ground decoration");
                for (int model : definition.anIntArray1954) {
                    byte[] bytes = store.readFiles(7, model).get(0);
                    require(bytes != null, "Missing native cave snow model " + model);
                    models.put(model, bytes);
                }
            }
        }
        Deque.objectFileLoader = new FileTable(false, false) {
            @Override public byte[] lookupFile(int archive, int child) {
                return archive == 6 ? definitions.get(child) : null;
            }
        };
        jagex.world.actors.Npc.modelFileLoader = new FileTable(false, false) {
            @Override public byte[] lookupFile(int archive, int child) {
                return child == 0 ? models.get(archive) : null;
            }
            @Override public boolean isArchiveLoaded(int archive, int child) {
                return child == 0 && models.containsKey(archive);
            }
        };
        Class63.aBoolean1120 = false;
        Class65.anInt1145 = 2816;
        JKeyListener.anInt618 = 3136;
        require(ChristmasEventSnow.groundSnowModelsReady(), "Native snow models not ready");
        Class39_Sub14.aByteArrayArrayArray1516 = new byte[4][104][104];
        Class33.aByteArrayArrayArray596 = new byte[4][104][104];
        Class55.tileFlags = new byte[4][104][104];
        Class67.heightMap = new int[4][105][105];
        for (int[] row : Class67.heightMap[0]) Arrays.fill(row, 96);
        byte[][][] floors = Class39_Sub14.aByteArrayArrayArray1516;
        floors[0][7][4] = 58; // Native snow, outside the entrance radius.
        floors[0][8][4] = 58; // An existing floor decoration must survive.
        floors[0][9][4] = 58; // Snow underlay beneath water must stay undecorated.
        Class33.aByteArrayArrayArray596[0][9][4] = 6;
        floors[0][27][5] = 72; // Dry entrance approach with snowfall.
        floors[0][28][5] = 72; // Water within that same weather radius.
        Class33.aByteArrayArrayArray596[0][28][5] = 6;
        floors[0][29][5] = 72; // Bridge/roof tile.
        Class55.tileFlags[0][29][5] = 2;
        floors[0][30][5] = 72; // Blocked terrain.
        Class55.tileFlags[0][30][5] = 1;
        floors[0][27][16] = 72; // Eleven tiles north: outside the weather radius.
        floors[1][27][5] = 72;
        Class38 scene = new Class38(4, 104, 104, Class67.heightMap);
        Model existing = new Model();
        scene.method381(0, 8, 4, 96, existing, 123, 22);
        ChristmasEventSnow.buildOutdoorGroundSnow(scene);
        Class50 nativeSnow = scene.method400(0, 7, 4);
        require(nativeSnow != null && nativeSnow.aClass39_Sub5_Sub4_933 instanceof Model,
                "Native snow did not receive visible ground scenery");
        require(nativeSnow.anInt929 == 96, "Snow did not follow the terrain height");
        require(nativeSnow.anInt935 < 0, "Decorative snow gained a selectable scene tag");
        require(scene.method400(0, 8, 4).aClass39_Sub5_Sub4_933 == existing,
                "Snow replaced existing floor scenery");
        require(scene.method400(0, 9, 4) == null && scene.method400(0, 28, 5) == null,
                "Snow scenery was placed over water");
        require(floors[0][27][5] == 58 && scene.method400(0, 27, 5) != null,
                "Dry snowfall approach did not receive white ground and snow detail");
        require(floors[0][28][5] == 72 && floors[0][29][5] == 72
                        && floors[0][30][5] == 72 && floors[0][27][16] == 72
                        && floors[1][27][5] == 72,
                "Snow changed water, bridge, blocked, distant or upper-floor terrain");
        require(scene.method400(0, 29, 5) == null && scene.method400(0, 30, 5) == null
                        && scene.method400(0, 27, 16) == null && scene.method400(1, 27, 5) == null,
                "Snow scenery escaped its ground footprint");
        int nativeTag = nativeSnow.anInt935;
        int nativeInfo = nativeSnow.anInt932;
        ChristmasEventSnow.buildOutdoorGroundSnow(scene);
        require(scene.method400(0, 7, 4) == nativeSnow, "Repeated build replaced snow scenery");

        // Same world tile, different scene origin: retain the chosen asset and rotation.
        Class65.anInt1145 = 2808;
        floors = Class39_Sub14.aByteArrayArrayArray1516 = new byte[4][104][104];
        floors[0][15][4] = 58;
        scene = new Class38(4, 104, 104, Class67.heightMap);
        ChristmasEventSnow.buildOutdoorGroundSnow(scene);
        Class50 rebased = scene.method400(0, 15, 4);
        require(rebased != null && (rebased.anInt935 & ~127) == (nativeTag & ~127)
                        && rebased.anInt932 == nativeInfo,
                "Ground snow pattern changed after region rebasing");

        Class63.aBoolean1120 = true;
        floors[0][35][5] = 72;
        scene = new Class38(4, 104, 104, Class67.heightMap);
        ChristmasEventSnow.buildOutdoorGroundSnow(scene);
        require(floors[0][35][5] == 72 && scene.method400(0, 15, 4) == null,
                "Outdoor snow leaked into an instance");
        Class63.aBoolean1120 = false;
        Class65.anInt1145 = 3136;
        JKeyListener.anInt618 = 5312;
        floors[0][28][12] = 58;
        scene = new Class38(4, 104, 104, Class67.heightMap);
        ChristmasEventSnow.buildOutdoorGroundSnow(scene);
        require(scene.method400(0, 28, 12) == null, "Outdoor pass modified cave scenery");
    }

    private static void verifyDungeonApproachSnow() {
        // Include the ten-tile boundary in every direction, using normal tile distance.
        for (int[] tile : new int[][]{{2843,3141}, {2833,3141}, {2853,3141},
                {2843,3131}, {2843,3151}, {2853,3151}}) {
            require(ChristmasEventSnow.isNearChristmasDungeon(tile[0], tile[1], 0),
                    "Dungeon snow missing within ten tiles");
        }
        for (int[] tile : new int[][]{{2832,3141}, {2854,3141}, {2843,3130}, {2843,3152}, {3164,5324}}) {
            require(!ChristmasEventSnow.isNearChristmasDungeon(tile[0], tile[1], 0),
                    "Dungeon approach trigger extends beyond ten tiles");
        }
        require(!ChristmasEventSnow.isNearChristmasDungeon(2843,3141,1),
                "Dungeon snow leaked onto an upper plane");

        // The extra snow layer also brings thick mist onto dry tiles near the dungeon.
        require(!ChristmasEventSnow.isSnowArea(2843,3131,0), "Approach check needs a dry tile");
        Player player = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        player.anInt2301 = (2843 - Class65.anInt1145) * 128 + 64;
        player.anInt2275 = (3131 - JKeyListener.anInt618) * 128 + 64;
        int[] approach = new int[765 * 503];
        DrawingArea.setBuffer(approach,765,503);
        ChristmasEventSnow.drawSnowfall();
        int approachMist = approach[0];
        require((approachMist >> 16 & 255) >= 100,
                "Heavy mist was not drawn on the dry dungeon approach");
        require(Arrays.stream(approach).filter(pixel -> pixel == approachMist).count() > 512 * 334 / 2,
                "Heavy mist did not cover the viewport");
        require(Arrays.stream(approach).filter(pixel -> pixel != 0 && pixel != approachMist).count() > 100,
                "Dungeon approach did not draw the small snowfall layer");
        for (int y = 0; y < 503; y++) for (int x = 0; x < 765; x++)
            if (x >= 512 || y >= 334) require(approach[x + y * 765] == 0,
                    "Dungeon flakes covered the HUD or chatbox");
        int[] firstFrame = approach.clone();
        Arrays.fill(approach,0);
        Class2.logicCycle += 10;
        ChristmasEventSnow.drawSnowfall();
        require(!Arrays.equals(firstFrame,approach), "Dungeon flakes did not animate");
        Arrays.fill(approach,0);
        player.anInt2275 = (3130 - JKeyListener.anInt618) * 128 + 64;
        ChristmasEventSnow.drawSnowfall();
        require(Arrays.stream(approach).allMatch(pixel -> pixel == 0),
                "Dungeon flakes did not stop at eleven tiles");
        player.anInt2275 = (3131 - JKeyListener.anInt618) * 128 + 64;
        Class63.aBoolean1120 = true;
        require(!ChristmasEventSnow.isNearChristmasDungeon(2843,3141,0),
                "Instance passed the approach trigger");
        ChristmasEventSnow.drawSnowfall();
        require(Arrays.stream(approach).allMatch(pixel -> pixel == 0), "Snow rendered in an instance");
        Class63.aBoolean1120 = false;
    }

    private static void verifyKiteArtwork() throws Exception {
        int[] pixels = new int[512 * 334];
        Arrays.fill(pixels, 0x889988);
        DrawingArea.setBuffer(pixels,512,334);
        java.lang.reflect.Method draw = ChristmasEventSnow.class.getDeclaredMethod(
                "drawSmallCacheFlake", int.class, int.class, int.class, int.class);
        draw.setAccessible(true);
        draw.invoke(null,256,167,255,0);
        long drawn = Arrays.stream(pixels).filter(pixel -> pixel != 0x889988).count();
        require(drawn > 8 && drawn < 30, "Small lower-right triangle outline or transparency is incorrect");
        for (int y = 164; y <= 169; y++) {
            int first = -1, last = -1;
            for (int x = 253; x <= 259; x++) if (pixels[x + y * 512] != 0x889988) {
                if (first == -1) first = x;
                last = x;
            }
            require(first != -1, "Triangle is broken into disconnected rows");
            for (int x = first; x <= last; x++) {
                int color = pixels[x + y * 512];
                require((color >> 16 & 255) > 230 && (color & 255) > 230,
                        "Triangle contains dark seams or gaps instead of a solid white fill");
            }
        }
        require(pixels[259 + 164 * 512] == 0x889988, "Upper-right kite section was drawn");
        require(pixels[253 + 169 * 512] == 0x889988, "Lower-left kite section was drawn");
        java.awt.image.BufferedImage preview = new java.awt.image.BufferedImage(512,334,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        preview.setRGB(0,0,512,334,pixels,0,512);
        javax.imageio.ImageIO.write(preview,"png",new File("../qa-output/christmas-kite-preview.png"));
        java.awt.image.BufferedImage enlarged = new java.awt.image.BufferedImage(192,192,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D graphics = enlarged.createGraphics();
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(preview,0,0,192,192,250,161,262,173,null);
        graphics.dispose();
        javax.imageio.ImageIO.write(enlarged,"png",new File("../qa-output/christmas-triangle-detail.png"));
        int[] unrotated = pixels.clone();
        for (int spin = 256; spin < 2048; spin += 256) {
            Arrays.fill(pixels,0x889988);
            draw.invoke(null,256,167,255,spin);
            require(!Arrays.equals(unrotated,pixels), "Triangle spin did not change its silhouette");
            long rotatedPixels = Arrays.stream(pixels).filter(pixel -> pixel != 0x889988).count();
            require(rotatedPixels > 6 && rotatedPixels < 30,
                    "Spinning triangle collapsed or became larger than the snowballs");
            for (int y = 160; y < 175; y++) {
                int first = -1, last = -1;
                for (int x = 249; x < 264; x++) if (pixels[x + y * 512] != 0x889988) {
                    if (first == -1) first = x;
                    last = x;
                }
                for (int x = first; first != -1 && x <= last; x++)
                    require((pixels[x + y * 512] & 255) > 230,
                            "Spinning triangle developed gaps or dark seams");
            }
        }
        require(ChristmasEventSnow.smallSnowSpin(0,100) != ChristmasEventSnow.smallSnowSpin(0,101),
                "Triangle angle did not advance with the client tick");
        require(ChristmasEventSnow.smallSnowSpin(0,100) == ChristmasEventSnow.smallSnowSpin(0,150),
                "Triangles do not share the snowballs' one-second spin period");
        Arrays.fill(pixels, 0x889988);
        java.lang.reflect.Method ball = ChristmasEventSnow.class.getDeclaredMethod(
                "drawSnowball", int.class, int.class, int.class, int.class);
        ball.setAccessible(true);
        ball.invoke(null,256,167,5,0);
        require(Arrays.stream(pixels).filter(pixel -> pixel != 0x889988).count() > drawn,
                "Triangles must appear smaller than the snowballs");
    }

    private static void verifyHotReloadArrayRepair() throws Exception {
        java.lang.reflect.Field largeOffsets = ChristmasEventSnow.class.getDeclaredField("pathOffsets");
        largeOffsets.setAccessible(true);
        largeOffsets.set(null, new int[38]);
        ChristmasEventSnow.snowballX(38, 0);
        require(((int[]) largeOffsets.get(null)).length == ChristmasEventSnow.snowballCount(),
                "Stale large-snow array was not repaired after hot reload");

        java.lang.reflect.Field smallPhases = ChristmasEventSnow.class.getDeclaredField("smallPhases");
        smallPhases.setAccessible(true);
        smallPhases.set(null, new int[180]);
        ChristmasEventSnow.smallSnowPhase(180, 0);
        require(((int[]) smallPhases.get(null)).length == ChristmasEventSnow.smallSnowballCount(),
                "Stale dense-snow array was not repaired after hot reload");
    }

    private static void checkModel(Widget widget, int modelId, int sequenceId, String name) {
        require(widget != null && widget.type == 6, name + " is not a model widget");
        require(widget.anInt2026 == modelId, name + " model id mismatch: " + widget.anInt2026);
        require(widget.anInt2103 == sequenceId, name + " sequence id mismatch: " + widget.anInt2103);
        require(widget.anInt2091 == 248 && widget.anInt2021 == 354,
                name + " position mismatch");
        require(widget.quadWidth == 32 && widget.quadHeight == 32,
                name + " dimensions mismatch");
        require(widget.anInt2074 == 200, name + " zoom mismatch");
    }

    private static byte[][][] copy(byte[][][] source) {
        byte[][][] result = new byte[source.length][][];
        for (int z = 0; z < source.length; z++) {
            result[z] = new byte[source[z].length][];
            for (int x = 0; x < source[z].length; x++) result[z][x] = source[z][x].clone();
        }
        return result;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}



