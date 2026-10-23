package unpackaged;

import com.rs2.cache.js5.Js5CacheStore;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import jagex.io.Buffer;

/** Run from Server with client/server classes and Server/lib on the classpath. */
public final class SnowballAppearanceChecks {
    public static void main(String[] args) throws Exception {
        ItemDefinition snowball = new ItemDefinition();
        final Map<Integer, byte[]> models = new HashMap<Integer, byte[]>();
        try (Js5CacheStore store = new Js5CacheStore(new File("cache"))) {
            snowball.decode(0, new Buffer(store.readFiles(2, 10).get(10501)));
            require(snowball.anInt1645 == 20504 && snowball.anInt1667 == 20504,
                    "Snowball must retain its original male and female worn model");
            models.put(20504, store.readFiles(7, 20504).get(0));
            require(models.get(20504) != null, "Native snowball hand model is missing");
        }
        Class4.aClass9_71 = new FileTable(false, false) {
            @Override public byte[] lookupFile(int archive, int child) {
                return child == 0 ? models.get(archive) : null;
            }
            @Override public boolean isArchiveLoaded(int archive, int child) {
                return child == 0 && models.get(archive) != null;
            }
        };
        for (boolean female : new boolean[]{false, true}) {
            require(snowball.method463(female, 0), "Snowball worn model is not ready");
            Model held = snowball.method474(female, 0);
            require(held != null && held.anInt2367 > 0 && held.anInt2366 > 0,
                    "Equipped snowball did not build visible geometry for gender " + female);
        }
        System.out.println("Snowball appearance checks passed: native worn model decodes and builds for both genders.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
