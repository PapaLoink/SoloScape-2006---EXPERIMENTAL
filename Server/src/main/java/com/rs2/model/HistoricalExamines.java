package com.rs2.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/** Server-side descriptions absent from the revision 443 client cache. */
public final class HistoricalExamines {
    private static final Map<String, String[]> REVISION_443 = loadFile("examines443.tsv");
    private static final Map<String, String[]> TEXTS = loadFile("examines2006.tsv");
    private static final Map<String, String[]> SUPPLEMENTS = loadFile("examines2009scape.tsv");

    private HistoricalExamines() { }

    private static Map<String, String[]> loadFile(String file) {
        Map<String, String[]> texts = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(
                Paths.get("data/content", file), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] fields = line.split("\t", -1);
                if (fields.length != 4 || fields[3].trim().isEmpty()) {
                    throw new IOException("Malformed examine row: " + line);
                }
                int id = Integer.parseInt(fields[1]);
                if (texts.put(fields[0] + ":" + id, new String[]{fields[2], fields[3]}) != null) {
                    throw new IOException("Duplicate examine: " + fields[0] + ":" + id);
                }
            }
        } catch (IOException | NumberFormatException failure) {
            throw new IllegalStateException("Unable to load historical examine descriptions", failure);
        }
        return texts;
    }

    public static String find(String kind, int id, String name) {
        String[] text = REVISION_443.get(kind + ":" + id);
        if (text != null && text[0].equalsIgnoreCase(name)) return text[1];
        text = TEXTS.get(kind + ":" + id);
        // Custom replacements must not inherit the original entity's description.
        if (text != null && (text[0].isEmpty() || text[0].equalsIgnoreCase(name))) return text[1];
        text = SUPPLEMENTS.get(kind + ":" + id);
        return text != null && text[0].equalsIgnoreCase(name) ? text[1] : null;
    }

    public static String resolve(String kind, int id, String name, String description) {
        if (description != null && !description.trim().isEmpty()
                && !description.equals("It's an item!") && !description.equals("Its an object!")) {
            return description;
        }
        String historical = find(kind, id, name);
        if (historical != null) return historical;
        if (name != null && !name.trim().isEmpty() && !name.equalsIgnoreCase("null")
                && !name.startsWith("NPC #") && !name.startsWith("Object: #") && !name.equals("# + id")) {
            return "It's " + name + ".";
        }
        return "npc".equals(kind) ? "You see a creature." : "object".equals(kind)
                ? "You see an object." : "You see an item.";
    }
}
