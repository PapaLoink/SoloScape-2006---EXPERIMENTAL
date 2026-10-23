package com.rs2.net.packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Classic (2004) combat tabs for the "Classic interface" client setting. The client builds groups
 * 508-523 from LostCity's revision 274 combat layouts, whose component ids are the legacy ids this
 * server already uses; data/content/classic_combat_ids.tsv maps them to the classic widgets.
 */
public final class ClassicCombat {
    public static final int FIRST_GROUP = 508, LAST_GROUP = 524; // 524: 2004-style whip tab
    private static final Map<Integer, Integer> GROUP_BY_ROOT = new HashMap<Integer, Integer>();
    private static final Map<Integer, Integer> CLASSIC_BY_LEGACY = new HashMap<Integer, Integer>();
    private static final Map<Integer, Integer> LEGACY_BY_CLASSIC = new HashMap<Integer, Integer>();
    static {
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get("data/content", "classic_combat_ids.tsv"), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] f = line.split("\t");
                int group = Integer.parseInt(f[0]), root = Integer.parseInt(f[1]);
                int legacy = Integer.parseInt(f[3]), child = Integer.parseInt(f[5]);
                GROUP_BY_ROOT.put(root, group);
                CLASSIC_BY_LEGACY.put(legacy, group << 16 | child);
                LEGACY_BY_CLASSIC.put(group << 16 | child, legacy);
            }
        } catch (IOException | RuntimeException failure) {
            throw new IllegalStateException("Unable to load classic combat tab ids", failure);
        }
    }

    private ClassicCombat() { }

    public static boolean isClassicGroup(int group) {
        return group >= FIRST_GROUP && group <= LAST_GROUP;
    }

    /** Classic group for a legacy combat tab interface, or -1 (e.g. the whip, which postdates 2004). */
    public static int groupForRoot(int legacyRoot) {
        Integer group = GROUP_BY_ROOT.get(legacyRoot);
        return group == null ? -1 : group;
    }

    /** Packed classic widget for a legacy combat component, or -1. */
    public static int component(int legacyId) {
        Integer packed = CLASSIC_BY_LEGACY.get(legacyId);
        return packed == null ? -1 : packed;
    }

    /** Legacy component for a packed classic widget, or -1. */
    public static int legacyComponent(int packed) {
        Integer legacy = LEGACY_BY_CLASSIC.get(packed);
        return legacy == null ? -1 : legacy;
    }

    public static Map<Integer, Integer> groupsByRoot() {
        return Collections.unmodifiableMap(GROUP_BY_ROOT);
    }
}
