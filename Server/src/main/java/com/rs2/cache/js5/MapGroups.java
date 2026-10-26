package com.rs2.cache.js5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/** Named terrain and XTEA-encrypted location groups in JS5 archive 5. */
public final class MapGroups {
    private MapGroups() {
    }

    public static byte[] readTerrain(Js5CacheStore store, Js5ReferenceTable table,
                                     int x, int y) throws IOException {
        // A terrain override in map-fallback replaces the cached terrain for that square (the client does the same).
        File override = new File("cache/map-fallback", "m" + x + "_" + y + ".dat");
        if (override.isFile()) {
            System.out.println("Using terrain override: m" + x + "_" + y);
            return Files.readAllBytes(override.toPath());
        }
        return read(store, table, "m" + x + "_" + y, null);
    }

    public static byte[] readLocations(Js5CacheStore store, Js5ReferenceTable table,
                                       int x, int y) throws IOException {
        int[] key = XteaKeys.forMapSquare(x, y);
        if (key[0] == 0 && key[1] == 0 && key[2] == 0 && key[3] == 0) {
            key = null;
        }
        String name = "l" + x + "_" + y;
        try {
            return read(store, table, name, key);
        } catch (IOException exception) {
            File fallback = new File("cache/map-fallback", name + ".dat");
            if (!fallback.isFile()) throw exception;
            byte[] data = Files.readAllBytes(fallback.toPath());
            System.out.println("Using verified 443 map fallback: " + name);
            return data;
        }
    }

    private static byte[] read(Js5CacheStore store, Js5ReferenceTable table,
                               String name, int[] key) throws IOException {
        int groupId = table.getGroupId(name);
        if (groupId < 0) return null;
        try {
            Map<Integer, byte[]> files = store.readFiles(5, groupId, key);
            if (files.size() != 1) {
                throw new IOException("443 map group " + name + " has " + files.size() + " files");
            }
            return files.values().iterator().next();
        } catch (IOException exception) {
            throw new IOException("Unable to decode 443 map group " + name, exception);
        }
    }
}
