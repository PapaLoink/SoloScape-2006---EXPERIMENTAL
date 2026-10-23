package unpackaged;

import com.rs2.cache.js5.Js5CacheStore;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import jagex.io.Buffer;

/** Renders actual skinned child poses from the original cache for choreography review. */
public final class ChildAnimationChecks {
    public static void main(String[] args) throws Exception {
        boolean preview = args.length > 0 && args[0].equals("--preview");
        try (Js5CacheStore cache = new Js5CacheStore(new File("cache"))) {
          for (int childId : new int[]{5046, 5047, 5048}) {
            NpcDefinition child = new NpcDefinition();
            child.decode(new Buffer(cache.readFiles(2, 9).get(childId)));
            Model[] parts = new Model[child.anIntArray1885.length];
            for (int i = 0; i < parts.length; i++)
                parts[i] = new Model(cache.readFiles(7, child.anIntArray1885[i]).get(0));
            Model base = new Model(parts, parts.length);
            base.method553();
            final Map<Integer, byte[]> frames = cache.readFiles(0, 432);
            final Map<Integer, byte[]> skeletons = new HashMap<Integer, byte[]>();
            for (byte[] bytes : frames.values()) {
                int skeleton = (bytes[0] & 255) << 8 | bytes[1] & 255;
                if (!skeletons.containsKey(skeleton)) skeletons.put(skeleton, cache.readFiles(1, skeleton).get(0));
            }
            FileTable frameFiles = new FileTable(false, false) {
                @Override public byte[] lookupFile(int group, int file) { return frames.get(file); }
                @Override public int[] getChildrenEntries(int group) {
                    int[] ids = new int[frames.size()]; int index = 0;
                    for (int id : frames.keySet()) ids[index++] = id;
                    return ids;
                }
                @Override public int getAmountChildren(int group) {
                    return Collections.max(frames.keySet()) + 1;
                }
            };
            FileTable skeletonFiles = new FileTable(false, false) {
                @Override public byte[] lookupFile(int group, int file) { return skeletons.get(group); }
            };
            Class39_Sub5_Sub12 poses = new Class39_Sub5_Sub12(frameFiles, skeletonFiles, 432, false);
            Map<Integer, byte[]> sequences = cache.readFiles(2, 12);
            int[] candidates = new int[]{3854};
            for (int id : candidates) {
                Class39_Sub5_Sub11 sequence = new Class39_Sub5_Sub11();
                sequence.method698(new Buffer(sequences.get(id)), 0);
                for (int frame : sequence.anIntArray1833) {
                    byte[] bytes = frames.get(frame & 65535);
                    if (frame >>> 16 != 432 || bytes == null
                            || (((bytes[0] & 255) << 8) | (bytes[1] & 255)) != 319)
                        throw new AssertionError("Child emote skeleton mismatch");
                    Model pose = base.method567(true);
                    pose.method557(poses, frame & 65535);
                    verifyProportions(base, pose);
                }
                if (!preview) continue;
                BufferedImage strip = new BufferedImage(840,
                        ((sequence.anIntArray1833.length + 6) / 7) * 170, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = strip.createGraphics();
                g.setColor(Color.WHITE); g.fillRect(0, 0, strip.getWidth(), strip.getHeight());
                g.setColor(Color.BLACK); g.drawString("Sequence " + id, 8, 16);
                for (int sample = 0; sample < sequence.anIntArray1833.length; sample++) {
                    int frame = sequence.anIntArray1833[sample];
                    if (frame >>> 16 != 432) throw new AssertionError("Wrong child frame group");
                    Model pose = base.method567(true);
                    pose.method557(poses, frame & 65535);
                    draw(g, pose, (sample % 7) * 120 + 60, (sample / 7) * 170 + 150);
                    g.setColor(Color.BLACK);
                    g.drawString("Frame " + sample, (sample % 7) * 120 + 8, (sample / 7) * 170 + 166);
                }
                g.dispose();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ImageIO.write(strip, "png", bytes);
                System.out.println("CHILD_" + childId + "_POSE_" + id + "=" + Base64.getEncoder().encodeToString(bytes.toByteArray()));
            }
          }
        }
        System.out.println("Child animation mesh checks passed: all three models, every jump-for-joy frame, compatible skeleton and bounded proportions.");
    }

    private static void verifyProportions(Model base, Model pose) {
        int baseMinY = Integer.MAX_VALUE, baseMaxY = Integer.MIN_VALUE;
        for (int i = 0; i < base.anInt2367; i++) {
            baseMinY = Math.min(baseMinY, base.anIntArray2356[i]);
            baseMaxY = Math.max(baseMaxY, base.anIntArray2356[i]);
        }
        int limit = (baseMaxY - baseMinY) * 5 / 2;
        for (int[] coordinates : new int[][]{pose.anIntArray2376, pose.anIntArray2356, pose.anIntArray2359}) {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (int i = 0; i < pose.anInt2367; i++) {
                min = Math.min(min, coordinates[i]);
                max = Math.max(max, coordinates[i]);
            }
            if (max - min > limit) throw new AssertionError("Child emote stretched the mesh");
        }
    }

    private static void draw(Graphics2D g, Model pose, int ox, int oy) {
        final double[] depth = new double[pose.anInt2367];
        int[] x = new int[pose.anInt2367], y = new int[pose.anInt2367];
        for (int i = 0; i < x.length; i++) {
            double px = pose.anIntArray2376[i], py = pose.anIntArray2356[i], pz = pose.anIntArray2359[i];
            x[i] = ox + (int)((px * 0.87 + pz * 0.5) * 0.82);
            y[i] = oy + (int)((py * 0.96 - pz * 0.22 + px * 0.13) * 0.82);
            depth[i] = pz * 0.87 - px * 0.5;
        }
        Integer[] faces = new Integer[pose.anInt2366];
        for (int i = 0; i < faces.length; i++) faces[i] = i;
        Arrays.sort(faces, (a, b) -> Double.compare(
                depth[pose.anIntArray2385[b]] + depth[pose.anIntArray2363[b]] + depth[pose.anIntArray2368[b]],
                depth[pose.anIntArray2385[a]] + depth[pose.anIntArray2363[a]] + depth[pose.anIntArray2368[a]]));
        for (int face : faces) {
            int a = pose.anIntArray2385[face], b = pose.anIntArray2363[face], c = pose.anIntArray2368[face];
            int shade = 80 + (face % 8) * 14;
            g.setColor(new Color(shade / 2, shade, Math.min(255, shade + 35)));
            g.fillPolygon(new int[]{x[a], x[b], x[c]}, new int[]{y[a], y[b], y[c]}, 3);
        }
    }
}
