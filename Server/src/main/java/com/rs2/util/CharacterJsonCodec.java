package com.rs2.util;

import com.rs2.model.item.ItemStack;
import com.rs2.model.player.CharacterFileBankTab;
import com.rs2.model.player.CharacterFileRecord;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Versioned, human-editable character persistence. No executable class/type
 * names in JSON, no Java object deserialization and no third-party dependency.
 * Every public persistent CharacterFileRecord field is stored by name.
 */
public final class CharacterJsonCodec {
    private static final String FORMAT = "SoloScapeCharacter";
    private static final int VERSION = 1;
    private static final long MAX_BYTES = 8L * 1024L * 1024L;

    private CharacterJsonCodec() {}

    public static void save(File file, CharacterFileRecord record) throws IOException {
        if (record == null || record.username == null || record.username.isEmpty())
            throw new IOException("Cannot save a character without a username");
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("format", FORMAT);
        root.put("version", VERSION);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        for (Field field : CharacterFileRecord.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            try {
                data.put(field.getName(), encode(field.get(record)));
            } catch (IllegalAccessException exception) {
                throw new IOException("Cannot encode " + field.getName(), exception);
            }
        }
        root.put("character", data);
        byte[] bytes = pretty(root).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IOException("Character JSON is too large");

        File parent = file.getAbsoluteFile().getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs())
            throw new IOException("Cannot create character directory: " + parent);
        File tmp = new File(parent, file.getName() + ".tmp");
        try {
            Files.write(tmp.toPath(), bytes);
            // Validate the exact on-disk bytes before replacing the primary save.
            read(tmp, record.username);
            if (file.exists()) {
                // Keep one last known good JSON snapshot as a recovery option.
                Files.copy(file.toPath(), new File(parent, file.getName() + ".bak").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(tmp.toPath(), file.toPath(),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp.toPath());
        }
    }

    @SuppressWarnings("unchecked")
    public static CharacterFileRecord read(File file, String expectedUsername) throws IOException {
        if (!file.isFile() || file.length() == 0 || file.length() > MAX_BYTES)
            throw new IOException("Missing, empty or oversized JSON character file: " + file);
        final Map<String, Object> root;
        try {
            Object parsed = CharacterJsonSyntax.parse(new String(
                    Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            root = object(parsed, "document");
            if (!FORMAT.equals(root.get("format")) || intValue(root.get("version"), "version") != VERSION)
                throw new IllegalArgumentException("Unsupported character format/version");
            Map<String, Object> data = object(root.get("character"), "character");
            CharacterFileRecord record = new CharacterFileRecord();
            for (Field field : CharacterFileRecord.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) || !data.containsKey(field.getName())) continue;
                Object oldValue = field.get(record);
                Object value = decode(data.get(field.getName()), field.getType(),
                        oldValue, field.getName());
                field.set(record, value);
            }
            if (record.username == null || !record.username.equalsIgnoreCase(expectedUsername))
                throw new IllegalArgumentException("Character username does not match filename");
            if (record.playerRights < 0 || record.playerRights > 3)
                throw new IllegalArgumentException("Invalid player rights");
            if (record.x < 0 || record.y < 0 || record.plane < 0 || record.plane > 3)
                throw new IllegalArgumentException("Invalid player coordinates");
            return record;
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            throw new IOException("Invalid character JSON in " + file + ": " + exception.getMessage(), exception);
        }
    }

    private static Object encode(Object value) throws IOException {
        if (value == null || value instanceof String || value instanceof Number
                || value instanceof Boolean) return value;
        if (value instanceof ItemStack) {
            ItemStack item = (ItemStack) value;
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("id", item.getId());
            map.put("amount", item.getAmount());
            map.put("metadata", item.getMetadata());
            return map;
        }
        if (value instanceof CharacterFileBankTab) {
            return encode(((CharacterFileBankTab) value).getItems());
        }
        if (value instanceof List) {
            List<Object> list = new ArrayList<Object>();
            for (Object entry : (List<?>) value) list.add(encode(entry));
            return list;
        }
        if (value.getClass().isArray()) {
            List<Object> list = new ArrayList<Object>();
            for (int i = 0; i < Array.getLength(value); i++)
                list.add(encode(Array.get(value, i)));
            return list;
        }
        throw new IOException("Unsupported save value type: " + value.getClass());
    }

    private static Object decode(Object value, Class<?> type, Object previous, String name) {
        if (value == null) {
            if (type.isPrimitive()) throw new IllegalArgumentException(name + " cannot be null");
            return null;
        }
        if (type == String.class) {
            if (!(value instanceof String)) throw new IllegalArgumentException(name + " must be a string");
            return value;
        }
        if (type == boolean.class || type == Boolean.class) {
            if (!(value instanceof Boolean)) throw new IllegalArgumentException(name + " must be boolean");
            return value;
        }
        if (type == int.class || type == Integer.class) return intValue(value, name);
        if (type == byte.class || type == Byte.class) {
            int n = intValue(value, name);
            if (n < -128 || n > 127) throw new IllegalArgumentException(name + " byte out of range");
            return (byte) n;
        }
        if (type == long.class || type == Long.class) return longValue(value, name);
        if (type == double.class || type == Double.class) {
            if (!(value instanceof Number) || !Double.isFinite(((Number) value).doubleValue()))
                throw new IllegalArgumentException(name + " must be a finite number");
            return ((Number) value).doubleValue();
        }
        if (type.isArray()) {
            List<?> list = array(value, name);
            int required = previous == null ? list.size() : Array.getLength(previous);
            if (list.size() != required)
                throw new IllegalArgumentException(name + " must contain " + required + " entries");
            if (required > 50000) throw new IllegalArgumentException(name + " exceeds array size limit");
            Object result = Array.newInstance(type.getComponentType(), required);
            for (int i = 0; i < required; i++)
                Array.set(result, i, decodeArrayEntry(list.get(i), type.getComponentType(), name + "[" + i + "]"));
            return result;
        }
        if (List.class.isAssignableFrom(type)) {
            List<?> source = array(value, name);
            if (source.size() > 50000) throw new IllegalArgumentException(name + " too many list entries");
            ArrayList result = new ArrayList();
            for (int i = 0; i < source.size(); i++) {
                Object item = source.get(i);
                if ("bankTabs".equals(name)) {
                    List<?> tabItems = array(item, name + "[" + i + "]");
                    ArrayList<ItemStack> items = new ArrayList<ItemStack>();
                    for (Object cell : tabItems) items.add(readItem(cell, name));
                    result.add(CharacterFileBankTab.fromItems(items));
                } else {
                    result.add(item == null ? null : intValue(item, name + "[" + i + "]"));
                }
            }
            return result;
        }
        throw new IllegalArgumentException("Unsupported field type " + type + ": " + name);
    }

    private static Object decodeArrayEntry(Object value, Class<?> type, String name) {
        if (type == ItemStack.class) return readItem(value, name);
        return decode(value, type, null, name);
    }

    private static ItemStack readItem(Object value, String name) {
        if (value == null) return null;
        Map<String, Object> item = object(value, name);
        int id = intValue(item.get("id"), name + ".id");
        int amount = intValue(item.get("amount"), name + ".amount");
        int metadata = item.containsKey("metadata") ?
                intValue(item.get("metadata"), name + ".metadata") : -1;
        if (id < 0 || id >= 11883 || amount <= 0)
            throw new IllegalArgumentException(name + " invalid item ID or amount");
        return new ItemStack(id, amount, metadata);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value, String name) {
        if (!(value instanceof Map)) throw new IllegalArgumentException(name + " must be an object");
        return (Map<String, Object>) value;
    }

    private static List<?> array(Object value, String name) {
        if (!(value instanceof List)) throw new IllegalArgumentException(name + " must be an array");
        return (List<?>) value;
    }

    private static long longValue(Object value, String name) {
        if (!(value instanceof Number)) throw new IllegalArgumentException(name + " must be an integer");
        double number = ((Number) value).doubleValue();
        long result = ((Number) value).longValue();
        if (!Double.isFinite(number) || number != (double) result)
            throw new IllegalArgumentException(name + " must be an integer");
        return result;
    }

    private static int intValue(Object value, String name) {
        long result = longValue(value, name);
        if (result < Integer.MIN_VALUE || result > Integer.MAX_VALUE)
            throw new IllegalArgumentException(name + " int out of range");
        return (int) result;
    }

    private static String pretty(Object obj) {
        StringBuilder out = new StringBuilder();
        format(out, obj, 0);
        out.append('\n');
        return out.toString();
    }

    private static void format(StringBuilder out, Object value, int depth) {
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            out.append("{");
            int pos = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                out.append(pos++ == 0 ? "\n" : ",\n");
                indent(out, depth + 1);
                out.append(CharacterJsonSyntax.write(String.valueOf(entry.getKey()))).append(": ");
                format(out, entry.getValue(), depth + 1);
            }
            if (!map.isEmpty()) { out.append('\n'); indent(out, depth); }
            out.append("}");
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            boolean simple = true;
            for (Object item : list) if (item instanceof Map || item instanceof List) { simple = false; break; }
            out.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (simple) {
                    if (i > 0) out.append(", ");
                    if (i > 0 && i % 20 == 0) { out.append('\n'); indent(out, depth + 1); }
                } else {
                    out.append(i == 0 ? "\n" : ",\n");
                    indent(out, depth + 1);
                }
                format(out, list.get(i), depth + 1);
            }
            if (!simple && !list.isEmpty()) { out.append('\n'); indent(out, depth); }
            out.append("]");
        } else {
            out.append(CharacterJsonSyntax.write(value));
        }
    }

    private static void indent(StringBuilder out, int depth) {
        for (int i = 0; i < depth; i++) out.append("  ");
    }
}
