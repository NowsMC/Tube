package space.nows.tube.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class Whitelist {

    private final Path file;
    private final Map<UUID, String> entries = new LinkedHashMap<>();
    private boolean enabled;

    Whitelist(Path directory, boolean enabled) {
        this.file = directory.resolve("whitelist.json");
        this.enabled = enabled;
        reload();
    }

    synchronized void reload() {
        entries.clear();
        for (JsonElement element : JsonFiles.readArray(file)) {
            JsonObject entry = element.getAsJsonObject();
            entries.put(UUID.fromString(entry.get("uuid").getAsString()), entry.get("name").getAsString());
        }
    }

    synchronized boolean enabled() {
        return enabled;
    }

    synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    synchronized boolean contains(UUID uuid) {
        return entries.containsKey(uuid);
    }

    synchronized boolean add(UUID uuid, String name) {
        boolean added = entries.put(uuid, name) == null;
        save();
        return added;
    }

    synchronized boolean remove(UUID uuid) {
        boolean removed = entries.remove(uuid) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    synchronized Collection<String> names() {
        return List.copyOf(entries.values());
    }

    private void save() {
        JsonArray array = new JsonArray();
        entries.forEach((uuid, name) -> {
            JsonObject json = new JsonObject();
            json.addProperty("uuid", uuid.toString());
            json.addProperty("name", name);
            array.add(json);
        });
        JsonFiles.write(file, array);
    }
}
