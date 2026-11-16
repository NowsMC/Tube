import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

// Prints the org.bukkit classes and members that plugins reference but tube-bukkit-api does not provide.
// usage: java -cp <tube-bukkit-api classes and dependencies> Coverage.java <scan.json> [jar name part...]
public class Coverage {

    public static void main(String[] args) throws Exception {
        JsonObject scan = JsonParser.parseString(Files.readString(Path.of(args[0]))).getAsJsonObject();
        List<String> only = Arrays.asList(args).subList(1, args.length);
        var missing = new TreeMap<String, TreeSet<String>>();
        for (String section : List.of("members", "classes")) {
            for (var entry : scan.getAsJsonObject(section).entrySet()) {
                String ref = section.equals("classes") ? "CLASS " + entry.getKey() : entry.getKey();
                String problem = null;
                for (var user : entry.getValue().getAsJsonArray()) {
                    String jar = user.getAsString();
                    if (!only.isEmpty() && only.stream().noneMatch(jar::contains)) {
                        continue;
                    }
                    if (problem == null) {
                        problem = check(ref);
                        if (problem == null) {
                            break;
                        }
                    }
                    missing.computeIfAbsent(jar, k -> new TreeSet<>()).add(problem);
                }
            }
        }
        missing.forEach((jar, set) -> {
            System.out.println("== " + jar + " (" + set.size() + ")");
            set.forEach(p -> System.out.println("  " + p));
        });
    }

    static Class<?> load(String internal) {
        try {
            return Class.forName(internal.replace('/', '.'), false, Coverage.class.getClassLoader());
        } catch (Throwable e) {
            return null;
        }
    }

    static String check(String ref) {
        if (ref.startsWith("CLASS ")) {
            String name = ref.substring(6);
            return load(name) == null ? "class " + name : null;
        }
        int hash = ref.indexOf('#');
        String owner = ref.substring(0, hash);
        String rest = ref.substring(hash + 1);
        Class<?> type = load(owner);
        if (type == null) {
            return "class " + owner;
        }
        int paren = rest.indexOf('(');
        if (paren < 0) {
            for (int i = 1; i < rest.length(); i++) {
                if (rest.substring(i).matches("\\[*(L[\\w/$]+;|[ZBCSIJFD])") && hasField(type, rest.substring(0, i))) {
                    return null;
                }
            }
            return "field " + owner + "." + rest;
        }
        String name = rest.substring(0, paren);
        String desc = rest.substring(paren);
        return hasMethod(type, name, desc) ? null : "method " + owner + "." + rest;
    }

    static boolean hasField(Class<?> type, String name) {
        for (Class<?> c : hierarchy(type)) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean hasMethod(Class<?> type, String name, String desc) {
        for (Class<?> c : hierarchy(type)) {
            if (name.equals("<init>")) {
                for (Constructor<?> k : c.getDeclaredConstructors()) {
                    if (c == type && descriptor(k.getParameterTypes(), void.class).equals(desc)) {
                        return true;
                    }
                }
                continue;
            }
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && descriptor(m.getParameterTypes(), m.getReturnType()).equals(desc)) {
                    return true;
                }
            }
        }
        return false;
    }

    static Set<Class<?>> hierarchy(Class<?> type) {
        Set<Class<?>> seen = new HashSet<>();
        Deque<Class<?>> queue = new ArrayDeque<>();
        queue.add(type);
        while (!queue.isEmpty()) {
            Class<?> c = queue.poll();
            if (c == null || !seen.add(c)) {
                continue;
            }
            if (c.getSuperclass() != null) {
                queue.add(c.getSuperclass());
            }
            for (Class<?> i : c.getInterfaces()) {
                queue.add(i);
            }
        }
        seen.add(Object.class);
        return seen;
    }

    static String descriptor(Class<?>[] params, Class<?> ret) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> p : params) {
            sb.append(desc(p));
        }
        return sb.append(')').append(desc(ret)).toString();
    }

    static String desc(Class<?> c) {
        if (c.isArray()) {
            return "[" + desc(c.getComponentType());
        }
        if (c.isPrimitive()) {
            return switch (c.getName()) {
                case "void" -> "V";
                case "boolean" -> "Z";
                case "byte" -> "B";
                case "char" -> "C";
                case "short" -> "S";
                case "int" -> "I";
                case "long" -> "J";
                case "float" -> "F";
                case "double" -> "D";
                default -> throw new IllegalStateException();
            };
        }
        return "L" + c.getName().replace('.', '/') + ";";
    }
}
