package org.firstinspires.ftc.teamcode.logging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Just enough JSON to read a Pedro Visualizer {@code .pp} file in a JVM test. The SDK's
 * {@code org.json} is an Android stub that throws in unit tests, and adding a JSON library for one
 * test reader is not worth a dependency. Objects become {@link Map}, arrays {@link List}, numbers
 * {@link Double}.
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
