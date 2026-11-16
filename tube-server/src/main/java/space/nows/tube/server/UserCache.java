package space.nows.tube.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class UserCache {

    private static final int EXPIRY_DAYS = 30;

    private record Entry(UUID uuid, String name, ZonedDateTime expires) {
    }

    private final Path file;
    private final Map<UUID, Entry> entries = new LinkedHashMap<>();

    UserCache(Path directory) {
        this.file = directory.resolve("usercache.json");
        for (JsonElement element : JsonFiles.readArray(file)) {
            JsonObject entry = element.getAsJsonObject();
            ZonedDateTime expires = JsonFiles.parseTimestamp(entry.get("expiresOn").getAsString()).orElse(ZonedDateTime.now().minusDays(1));
            UUID uuid = UUID.fromString(entry.get("uuid").getAsString());
            entries.put(uuid, new Entry(uuid, entry.get("name").getAsString(), expires));
        }
    }

    synchronized void remember(UUID uuid, String name) {
        entries.put(uuid, new Entry(uuid, name, ZonedDateTime.now().plusDays(EXPIRY_DAYS)));
        save();
    }

    synchronized Optional<UUID> find(String name) {
        ZonedDateTime now = ZonedDateTime.now();
        return entries.values().stream()
                .filter(entry -> entry.expires().isAfter(now) && entry.name().toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT)))
                .map(Entry::uuid)
                .findFirst();
    }

    synchronized Optional<String> name(UUID uuid) {
        Entry entry = entries.get(uuid);
        return entry == null ? Optional.empty() : Optional.of(entry.name());
    }

    private void save() {
        JsonArray array = new JsonArray();
        entries.values().forEach(entry -> {
            JsonObject json = new JsonObject();
            json.addProperty("uuid", entry.uuid().toString());
            json.addProperty("name", entry.name());
            json.addProperty("expiresOn", JsonFiles.TIMESTAMP.format(entry.expires()));
            array.add(json);
        });
        JsonFiles.write(file, array);
    }
}
