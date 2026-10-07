package mg.rr.framework;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sprint 6 : convertit un objet Java en JSON.
 *
 * Strategie (par ordre de priorite) :
 *   1. Jackson (com.fasterxml.jackson.databind.ObjectMapper) si present
 *   2. Gson (com.google.gson.Gson) si present
 *   3. Serialiseur maison minimal (aucune dependance externe)
 *
 * L'appel aux libs se fait par reflexion pour ne pas obliger le framework
 * a etre compile avec elles. Meme approche que SpringContext.
 */
public final class JsonSerializer {

    private JsonSerializer() {}

    public static String toJson(Object value) {
        String viaJackson = tryJackson(value);
        if (viaJackson != null) return viaJackson;

        String viaGson = tryGson(value);
        if (viaGson != null) return viaGson;

        StringBuilder sb = new StringBuilder();
        write(value, sb);
        return sb.toString();
    }

    // ---------- Jackson ----------

    private static String tryJackson(Object value) {
        try {
            Class<?> mapperClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            Object mapper = mapperClass.getDeclaredConstructor().newInstance();
            Method writeValueAsString = mapperClass.getMethod("writeValueAsString", Object.class);
            return (String) writeValueAsString.invoke(mapper, value);
        } catch (ClassNotFoundException e) {
            return null; // Jackson absent : normal
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Jackson present mais erreur : on laisse Gson/fallback essayer
            return null;
        }
    }

    // ---------- Gson ----------

    private static String tryGson(Object value) {
        try {
            Class<?> gsonClass = Class.forName("com.google.gson.Gson");
            Object gson = gsonClass.getDeclaredConstructor().newInstance();
            Method toJson = gsonClass.getMethod("toJson", Object.class);
            return (String) toJson.invoke(gson, value);
        } catch (ClassNotFoundException e) {
            return null; // Gson absent : normal
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    // ---------- Fallback maison ----------

    private static void write(Object value, StringBuilder sb) {
        if (value == null) { sb.append("null"); return; }
        if (value instanceof String || value instanceof Character) {
            writeString(value.toString(), sb); return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString()); return;
        }
        if (value instanceof Enum<?> e) {
            writeString(e.name(), sb); return;
        }
        if (value instanceof Map<?, ?> map) {
            writeMap(map, sb); return;
        }
        if (value instanceof Collection<?> col) {
            writeCollection(col, sb); return;
        }
        if (value.getClass().isArray()) {
            writeArray(value, sb); return;
        }
        writePojo(value, sb);
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    private static void writeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            writeString(String.valueOf(entry.getKey()), sb);
            sb.append(':');
            write(entry.getValue(), sb);
        }
        sb.append('}');
    }

    private static void writeCollection(Collection<?> col, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object item : col) {
            if (!first) sb.append(',');
            first = false;
            write(item, sb);
        }
        sb.append(']');
    }

    private static void writeArray(Object arr, StringBuilder sb) {
        sb.append('[');
        int len = Array.getLength(arr);
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(',');
            write(Array.get(arr, i), sb);
        }
        sb.append(']');
    }

    private static void writePojo(Object value, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        Set<String> done = new HashSet<>();

        for (Method m : value.getClass().getMethods()) {
            if (m.getParameterCount() != 0) continue;
            if (m.getDeclaringClass() == Object.class) continue;
            String name = m.getName();
            String prop;
            if (name.startsWith("get") && name.length() > 3 && !name.equals("getClass")) {
                prop = decapitalize(name.substring(3));
            } else if (name.startsWith("is") && name.length() > 2
                    && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                prop = decapitalize(name.substring(2));
            } else {
                continue;
            }
            try {
                Object v = m.invoke(value);
                if (!first) sb.append(',');
                first = false;
                done.add(prop);
                writeString(prop, sb);
                sb.append(':');
                write(v, sb);
            } catch (Exception ignored) { /* getter defectueux : ignore */ }
        }

        for (Field f : value.getClass().getFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            if (done.contains(f.getName())) continue;
            try {
                Object v = f.get(value);
                if (!first) sb.append(',');
                first = false;
                writeString(f.getName(), sb);
                sb.append(':');
                write(v, sb);
            } catch (Exception ignored) { /* champ inaccessible : ignore */ }
        }

        sb.append('}');
    }

    private static String decapitalize(String s) {
        if (s.isEmpty()) return s;
        if (s.length() > 1
                && Character.isUpperCase(s.charAt(0))
                && Character.isUpperCase(s.charAt(1))) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}