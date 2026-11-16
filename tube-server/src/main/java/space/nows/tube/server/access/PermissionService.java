package space.nows.tube.server.access;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.api.PermissionManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public final class PermissionService implements PermissionManager {

    private static final int OP_LEVEL = 4;

    private final Path opsFile;
    private final Path permissionsFile;
    private final Map<UUID, String> ops = new LinkedHashMap<>();
    private final Map<UUID, Set<String>> grants = new HashMap<>();
    private volatile Set<UUID> opView = Set.of();
    private volatile Map<UUID, Set<String>> grantView = Map.of();

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
        }
    }

    @Override
    public boolean has(UUID uuid, String node) {
        if (opView.contains(uuid)) {
            return true;
        }
        Set<String> granted = grantView.get(uuid);
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
            publish();
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
            publish();
            saveGrants();
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
