package space.nows.tube.server.access;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.api.permission.PermissionDefault;
import space.nows.tube.api.permission.PermissionManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class PermissionService implements PermissionManager {

    private static final int OP_LEVEL = 4;
    private static final int MAX_PARENT_DEPTH = 8;

    private record Definition(PermissionDefault fallback, Map<String, Boolean> children) {
    }

    private final Path opsFile;
    private final Path permissionsFile;
    private final Map<UUID, String> ops = new LinkedHashMap<>();
    private final Map<UUID, Set<String>> grants = new HashMap<>();
    private volatile Set<UUID> opView = Set.of();
    private volatile Map<UUID, Set<String>> grantView = Map.of();
    private final Map<String, Definition> definitions = new ConcurrentHashMap<>();
    private final List<Consumer<UUID>> listeners = new CopyOnWriteArrayList<>();

    public PermissionService(Path directory) {
        this.opsFile = directory.resolve("ops.json");
        this.permissionsFile = directory.resolve("permissions.json");
        loadOps();
        loadGrants();
        publish();
    }

    private void publish() {
        opView = Set.copyOf(ops.keySet());
        Map<UUID, Set<String>> copy = new HashMap<>();
        grants.forEach((uuid, nodes) -> copy.put(uuid, Set.copyOf(nodes)));
        grantView = Map.copyOf(copy);
    }

    public void onChange(Consumer<UUID> listener) {
        listeners.add(listener);
    }

    private void notifyChange(UUID uuid) {
        listeners.forEach(listener -> listener.accept(uuid));
    }

    @Override
    public boolean isOp(UUID uuid) {
        return opView.contains(uuid);
    }

    @Override
    public synchronized void setOp(UUID uuid, String name, boolean op) {
        boolean changed = op ? !name.equals(ops.put(uuid, name)) : ops.remove(uuid) != null;
        if (changed) {
            publish();
            saveOps();
            notifyChange(uuid);
        }
    }

    @Override
    public boolean has(UUID uuid, String node) {
        if (opView.contains(uuid)) {
            return true;
        }
        return holds(grantView.getOrDefault(uuid, Set.of()), node, 0);
    }

    @Override
    public void define(String node, PermissionDefault fallback, Map<String, Boolean> children) {
        definitions.put(node, new Definition(fallback, Map.copyOf(children)));
    }

    @Override
    public void undefine(String node) {
        definitions.remove(node);
    }

    private boolean holds(Set<String> granted, String node, int depth) {
        if (matches(granted, node)) {
            return true;
        }
        Definition definition = definitions.get(node);
        if (definition != null && definition.fallback().grants(false)) {
            return true;
        }
        if (depth >= MAX_PARENT_DEPTH) {
            return false;
        }
        for (Map.Entry<String, Definition> parent : definitions.entrySet()) {
            if (Boolean.TRUE.equals(parent.getValue().children().get(node)) && holds(granted, parent.getKey(), depth + 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(Set<String> granted, String node) {
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
            publish();
            saveGrants();
            notifyChange(uuid);
        }
    }

    @Override
    public synchronized void revoke(UUID uuid, String node) {
        Set<String> granted = grants.get(uuid);
        if (granted != null && granted.remove(node)) {
            if (granted.isEmpty()) {
                grants.remove(uuid);
            }
            publish();
            saveGrants();
            notifyChange(uuid);
        }
    }

    private void loadOps() {
        for (JsonElement element : JsonFiles.readArray(opsFile)) {
            JsonObject entry = element.getAsJsonObject();
            ops.put(UUID.fromString(entry.get("uuid").getAsString()), entry.has("name") ? entry.get("name").getAsString() : "");
        }
    }

    private void loadGrants() {
        if (!Files.isRegularFile(permissionsFile)) {
            return;
        }
        JsonObject root = JsonFiles.read(permissionsFile).getAsJsonObject();
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
            entry.addProperty("bypassesPlayerLimit", false);
            array.add(entry);
        });
        JsonFiles.write(opsFile, array);
    }

    private void saveGrants() {
        JsonObject root = new JsonObject();
        grants.forEach((uuid, nodes) -> {
            JsonArray array = new JsonArray();
            new TreeSet<>(nodes).forEach(array::add);
            root.add(uuid.toString(), array);
        });
        JsonFiles.write(permissionsFile, root);
    }
}
