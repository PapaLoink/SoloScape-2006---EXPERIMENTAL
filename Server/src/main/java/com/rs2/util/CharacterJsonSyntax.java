package com.rs2.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CharacterJsonSyntax {
    private final String source;
    private int offset;

    private CharacterJsonSyntax(String source) { this.source = source; }

    static Map<String, Object> object(Object... pairs) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1]);
        return result;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> asObject(Object value) {
        if (!(value instanceof Map)) throw new IllegalArgumentException("Expected JSON object");
        return (Map<String, Object>) value;
    }

    static Object parse(String text) {
        CharacterJsonSyntax parser = new CharacterJsonSyntax(text);
        Object value = parser.value(0);
        parser.space();
        if (parser.offset != text.length()) throw parser.invalid();
        return value;
    }

    static String write(Object value) {
        StringBuilder out = new StringBuilder();
        append(out, value);
        return out.toString();
    }

    private static void append(StringBuilder out, Object value) {
        if (value == null) { out.append("null"); return; }
        if (value instanceof Boolean || value instanceof Number) {
            if (value instanceof Number && !Double.isFinite(((Number) value).doubleValue()))
                throw new IllegalArgumentException("Non-finite JSON number");
            out.append(value); return;
        }
        if (value instanceof Map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) out.append(',');
                first = false;
                append(out, String.valueOf(entry.getKey())); out.append(':');
                append(out, entry.getValue());
            }
            out.append('}'); return;
        }
        if (value instanceof Iterable || value.getClass().isArray()) {
            out.append('[');
            boolean first = true;
            if (value instanceof Iterable) {
                for (Object item : (Iterable<?>) value) {
                    if (!first) out.append(','); first = false; append(out, item);
                }
            } else {
                for (int i = 0; i < Array.getLength(value); i++) {
                    if (i > 0) out.append(','); append(out, Array.get(value, i));
                }
            }
            out.append(']'); return;
        }
        if (!(value instanceof String)) throw new IllegalArgumentException("Unsupported JSON value");
        out.append('"');
        for (char c : ((String) value).toCharArray()) {
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 32) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        out.append('"');
    }

    private Object value(int depth) {
        if (depth > 64) throw invalid();
        space();
        if (offset >= source.length()) throw invalid();
        char c = source.charAt(offset);
        if (c == '"') return string();
        if (c == '{') {
            offset++;
            Map<String, Object> result = object();
            space();
            if (take('}')) return result;
            do {
                space();
                if (offset >= source.length() || source.charAt(offset) != '"') throw invalid();
                String key = string(); space();
                if (!take(':') || result.containsKey(key)) throw invalid();
                result.put(key, value(depth + 1)); space();
                if (take('}')) return result;
            } while (take(','));
            throw invalid();
        }
        if (c == '[') {
            offset++;
            List<Object> result = new ArrayList<Object>();
            space();
            if (take(']')) return result;
            do {
                result.add(value(depth + 1)); space();
                if (take(']')) return result;
            } while (take(','));
            throw invalid();
        }
        for (String literal : new String[] {"true", "false", "null"}) {
            if (source.startsWith(literal, offset)) {
                offset += literal.length();
                return "null".equals(literal) ? null : Boolean.valueOf(literal);
            }
        }
        int begin = offset;
        take('-');
        if (!take('0')) {
            if (offset >= source.length() || source.charAt(offset) < '1'
                    || source.charAt(offset) > '9') throw invalid();
            digits();
        }
        boolean decimal = false;
        if (take('.')) { decimal = true; requiredDigits(); }
        if (take('e') || take('E')) {
            decimal = true;
            if (!take('+')) take('-');
            requiredDigits();
        }
        try {
            String number = source.substring(begin, offset);
            if (!decimal) return Long.valueOf(number);
            double value = Double.parseDouble(number);
            if (!Double.isFinite(value)) throw invalid();
            return value;
        } catch (NumberFormatException exception) { throw invalid(); }
    }

    private String string() {
        offset++;
        StringBuilder out = new StringBuilder();
        while (offset < source.length()) {
            char c = source.charAt(offset++);
            if (c == '"') return out.toString();
            if (c < 32) throw invalid();
            if (c == '\\') {
                if (offset >= source.length()) throw invalid();
                c = source.charAt(offset++);
                switch (c) {
                    case '"': case '\\': case '/': break;
                    case 'b': c = '\b'; break;
                    case 'f': c = '\f'; break;
                    case 'n': c = '\n'; break;
                    case 'r': c = '\r'; break;
                    case 't': c = '\t'; break;
                    case 'u':
                        if (offset + 4 > source.length()) throw invalid();
                        try { c = (char) Integer.parseInt(source.substring(offset, offset + 4), 16); }
                        catch (NumberFormatException exception) { throw invalid(); }
                        offset += 4; break;
                    default: throw invalid();
                }
            }
            out.append(c);
        }
        throw invalid();
    }

    private void requiredDigits() {
        int before = offset; digits();
        if (before == offset) throw invalid();
    }

    private void digits() {
        while (offset < source.length() && source.charAt(offset) >= '0'
                && source.charAt(offset) <= '9') offset++;
    }

    private void space() {
        while (offset < source.length() && " \r\n\t".indexOf(source.charAt(offset)) >= 0) offset++;
    }

    private boolean take(char c) {
        if (offset < source.length() && source.charAt(offset) == c) { offset++; return true; }
        return false;
    }

    private IllegalArgumentException invalid() {
        return new IllegalArgumentException("Invalid JSON at offset " + offset);
    }
}
