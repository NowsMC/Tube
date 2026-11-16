package space.nows.tube.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import space.nows.tube.api.PermissionManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

final class PermissionService implements PermissionManager {

    private static final int OP_LEVEL = 4;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path opsFile;
    private final Path permissionsFile;
    private final Map<UUID, String> ops = new LinkedHashMap<>();
    private final Map<UUID, Set<String>> grants = new HashMap<>();

    PermissionService(Path directory) {
        this.opsFile = directory.resolve("ops.json");
        this.permissionsFile = directory.resolve("permissions.json");
        loadOps();
        loadGrants();
    }

    @Override
    public synchronized boolean isOp(UUID uuid) {
        return ops.containsKey(uuid);
    }

    @Override
    public synchronized void setOp(UUID uuid, String name, boolean op) {
        boolean changed = op ? !name.equals(ops.put(uuid, name)) : ops.remove(uuid) != null;
        if (changed) {
            saveOps();
        }
    }

    @Override
    public synchronized boolean has(UUID uuid, String node) {
        if (ops.containsKey(uuid)) {
            return true;
        }
        Set<String> granted = grants.get(uuid);
        if (granted == null) {
            return false;
        }
        if (granted.contains(node) || granted.contains("*")) {
            return true;
        }
        for (String pattern : granted) {
            if (pattern.endsWith(".*") && node.startsWith(pattern.substring(0, pattern.length() - 1))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void grant(UUID uuid, String node) {
        if (grants.computeIfAbsent(uuid, key -> new HashSet<>()).add(node)) {
            saveGrants();
        }
    }

    @Override
    public synchronized void revoke(UUID uuid, String node) {
        Set<String> granted = grants.get(uuid);
        if (granted != null && granted.remove(node)) {
            if (granted.isEmpty()) {
                grants.remove(uuid);
            }
            saveGrants();
        }
    }

    private void loadOps() {
        for (JsonElement element : readArray(opsFile)) {
            JsonObject entry = element.getAsJsonObject();
            ops.put(UUID.fromString(entry.get("uuid").getAsString()), entry.has("name") ? entry.get("name").getAsString() : "");
        }
    }

    private void loadGrants() {
        if (!Files.isRegularFile(permissionsFile)) {
            return;
        }
        JsonObject root = parse(permissionsFile).getAsJsonObject();
        root.entrySet().forEach(entry -> {
            Set<String> nodes = new HashSet<>();
            entry.getValue().getAsJsonArray().forEach(node -> nodes.add(node.getAsString()));
            grants.put(UUID.fromString(entry.getKey()), nodes);
        });
    }

    private void saveOps() {
        JsonArray array = new JsonArray();
        ops.forEach((uuid, name) -> {
            JsonObject entry = new JsonObject();
            entry.addProperty("uuid", uuid.toString());
            entry.addProperty("name", name);
            entry.addProperty("level", OP_LEVEL);
            array.add(entry);
        });
        write(opsFile, array);
    }

    private void saveGrants() {
        JsonObject root = new JsonObject();
        grants.forEach((uuid, nodes) -> {
            JsonArray array = new JsonArray();
            new TreeSet<>(nodes).forEach(array::add);
            root.add(uuid.toString(), array);
        });
        write(permissionsFile, root);
    }

    private static JsonArray readArray(Path file) {
        return Files.isRegularFile(file) ? parse(file).getAsJsonArray() : new JsonArray();
    }

    private static JsonElement parse(Path file) {
        try {
            return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not parse " + file + ": " + e.getMessage(), e);
        }
    }

    private static void write(Path file, JsonElement content) {
        try {
            Files.writeString(file, GSON.toJson(content) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
