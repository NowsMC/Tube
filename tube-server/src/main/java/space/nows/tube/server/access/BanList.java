package space.nows.tube.server.access;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class BanList {

    public static final String DEFAULT_REASON = "Banned by an operator.";

    public record Ban(String key, String name, String created, String source, String expires, String reason) {

        boolean active() {
            if (expires.equals("forever")) {
                return true;
            }
            return JsonFiles.parseTimestamp(expires).map(date -> date.isAfter(ZonedDateTime.now())).orElse(true);
        }
    }

    private final Path playersFile;
    private final Path ipsFile;
    private final List<Ban> players = new ArrayList<>();
    private final List<Ban> ips = new ArrayList<>();

    public BanList(Path directory) {
        this.playersFile = directory.resolve("banned-players.json");
        this.ipsFile = directory.resolve("banned-ips.json");
        read(playersFile, "uuid", "name", players);
        read(ipsFile, "ip", "ip", ips);
    }

    public synchronized void reload() {
        players.clear();
        ips.clear();
        read(playersFile, "uuid", "name", players);
        read(ipsFile, "ip", "ip", ips);
    }

    synchronized Optional<Ban> player(UUID uuid) {
        return players.stream().filter(ban -> ban.key().equals(uuid.toString()) && ban.active()).findFirst();
    }

    synchronized Optional<Ban> ip(String address) {
        return ips.stream().filter(ban -> ban.key().equals(address) && ban.active()).findFirst();
    }

    public synchronized void banPlayer(UUID uuid, String name, String reason, String source) {
        players.removeIf(ban -> ban.key().equals(uuid.toString()));
        players.add(new Ban(uuid.toString(), name, JsonFiles.now(), source, "forever", reason));
        write(playersFile, "uuid", "name", players);
    }

    public synchronized boolean pardonPlayer(UUID uuid) {
        boolean removed = players.removeIf(ban -> ban.key().equals(uuid.toString()));
        if (removed) {
            write(playersFile, "uuid", "name", players);
        }
        return removed;
    }

    public synchronized void banIp(String address, String reason, String source) {
        ips.removeIf(ban -> ban.key().equals(address));
        ips.add(new Ban(address, address, JsonFiles.now(), source, "forever", reason));
        write(ipsFile, "ip", "ip", ips);
    }

    public synchronized boolean pardonIp(String address) {
        boolean removed = ips.removeIf(ban -> ban.key().equals(address));
        if (removed) {
            write(ipsFile, "ip", "ip", ips);
        }
        return removed;
    }

    public synchronized List<Ban> players() {
        return List.copyOf(players);
    }

    public synchronized List<Ban> ips() {
        return List.copyOf(ips);
    }

    private static void read(Path file, String keyField, String nameField, List<Ban> target) {
        for (JsonElement element : JsonFiles.readArray(file)) {
            JsonObject json = element.getAsJsonObject();
            target.add(new Ban(
                    json.get(keyField).getAsString(),
                    json.has(nameField) ? json.get(nameField).getAsString() : json.get(keyField).getAsString(),
                    json.has("created") ? json.get("created").getAsString() : JsonFiles.now(),
                    json.has("source") ? json.get("source").getAsString() : "Server",
                    json.has("expires") ? json.get("expires").getAsString() : "forever",
                    json.has("reason") ? json.get("reason").getAsString() : DEFAULT_REASON));
        }
    }

    private static void write(Path file, String keyField, String nameField, List<Ban> bans) {
        JsonArray array = new JsonArray();
        for (Ban ban : bans) {
            JsonObject json = new JsonObject();
            json.addProperty(keyField, ban.key());
            if (!keyField.equals(nameField)) {
                json.addProperty(nameField, ban.name());
            }
            json.addProperty("created", ban.created());
            json.addProperty("source", ban.source());
            json.addProperty("expires", ban.expires());
            json.addProperty("reason", ban.reason());
            array.add(json);
        }
        JsonFiles.write(file, array);
    }
}
