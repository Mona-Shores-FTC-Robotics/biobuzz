package org.firstinspires.ftc.teamcode.logging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Just enough JSON to read a Pedro Visualizer {@code .pp} file, and to read and write the glTF and
 * AdvantageScope configs {@link HiveAssets} edits, in a JVM test. The SDK's {@code org.json} is an
 * Android stub that throws in unit tests, and adding a JSON library for test tooling is not worth
 * a dependency. Objects become {@link Map}, arrays {@link List}, numbers {@link Double}.
 */
final class MiniJson {

    private final String s;
    private int i;

    private MiniJson(String s) {
        this.s = s;
    }

    static Object parse(String text) {
        MiniJson p = new MiniJson(text);
        Object v = p.value();
        p.ws();
        if (p.i != p.s.length()) throw p.error("trailing characters");
        return v;
    }

    /**
     * Writes what {@link #parse} reads (maps, lists, strings, numbers, booleans, null), compactly.
     * Whole numbers are written without a decimal point, so glTF indices stay integers.
     */
    static String write(Object value) {
        StringBuilder b = new StringBuilder();
        write(value, b);
        return b.toString();
    }

    @SuppressWarnings("unchecked")
    private static void write(Object v, StringBuilder b) {
        if (v == null) {
            b.append("null");
        } else if (v instanceof Map) {
            b.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) {
                if (!first) b.append(',');
                first = false;
                writeString(e.getKey(), b);
                b.append(':');
                write(e.getValue(), b);
            }
            b.append('}');
        } else if (v instanceof List) {
            b.append('[');
            boolean first = true;
            for (Object o : (List<Object>) v) {
                if (!first) b.append(',');
                first = false;
                write(o, b);
            }
            b.append(']');
        } else if (v instanceof String) {
            writeString((String) v, b);
        } else if (v instanceof Boolean) {
            b.append(v.toString());
        } else if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) throw new IllegalArgumentException("JSON has no " + d);
            if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                b.append((long) d);
            } else {
                b.append(d);
            }
        } else {
            throw new IllegalArgumentException("cannot write " + v.getClass());
        }
    }

    private static void writeString(String s, StringBuilder b) {
        b.append('"');
        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            switch (c) {
                case '"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break;
                case '\r': b.append("\\r"); break;
                case '\t': b.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        b.append('"');
    }

    private Object value() {
        ws();
        if (i >= s.length()) throw error("unexpected end");
        char c = s.charAt(i);
        switch (c) {
            case '{': return object();
            case '[': return array();
            case '"': return string();
            case 't': expect("true"); return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null"); return null;
            default: return number();
        }
    }

    private Map<String, Object> object() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++;
        ws();
        if (peek('}')) { i++; return m; }
        while (true) {
            ws();
            String k = string();
            ws();
            if (!peek(':')) throw error("expected :");
            i++;
            m.put(k, value());
            ws();
            if (peek(',')) { i++; continue; }
            if (peek('}')) { i++; return m; }
            throw error("expected , or }");
        }
    }

    private List<Object> array() {
        List<Object> a = new ArrayList<>();
        i++;
        ws();
        if (peek(']')) { i++; return a; }
        while (true) {
            a.add(value());
            ws();
            if (peek(',')) { i++; continue; }
            if (peek(']')) { i++; return a; }
            throw error("expected , or ]");
        }
    }

    private String string() {
        if (!peek('"')) throw error("expected string");
        i++;
        StringBuilder b = new StringBuilder();
        while (true) {
            char c = s.charAt(i++);
            if (c == '"') return b.toString();
            if (c != '\\') { b.append(c); continue; }
            char e = s.charAt(i++);
            switch (e) {
                case 'n': b.append('\n'); break;
                case 't': b.append('\t'); break;
                case 'r': b.append('\r'); break;
                case 'b': b.append('\b'); break;
                case 'f': b.append('\f'); break;
                case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                default: b.append(e);
            }
        }
    }

    private Double number() {
        int start = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (start == i) throw error("unexpected character");
        return Double.valueOf(s.substring(start, i));
    }

    private void expect(String word) {
        if (!s.startsWith(word, i)) throw error("expected " + word);
        i += word.length();
    }

    private boolean peek(char c) {
        return i < s.length() && s.charAt(i) == c;
    }

    private void ws() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private IllegalArgumentException error(String what) {
        return new IllegalArgumentException(what + " at character " + i);
    }
}
