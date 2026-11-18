package space.nows.tube.server.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class TableSet {

    private static final String ROOT = "/net/minestom/data/loot_tables/";

    private final String file;
    private final Function<String, LootTable> resolver;
    private final Map<String, LootTable> parsed = new ConcurrentHashMap<>();
    private volatile Map<String, JsonObject> sources;

    TableSet(String file, Function<String, LootTable> resolver) {
        this.file = file;
        this.resolver = resolver;
    }

    LootTable find(String key) {
        LootTable table = parsed.get(key);
        if (table != null) {
            return table;
        }
        JsonObject source = sources().get(key);
        if (source == null) {
            return null;
        }
        return parsed.computeIfAbsent(key, ignored -> LootTable.parse(source, resolver));
    }

    private Map<String, JsonObject> sources() {
        Map<String, JsonObject> loaded = sources;
        if (loaded == null) {
            loaded = read();
            sources = loaded;
        }
        return loaded;
    }

    private Map<String, JsonObject> read() {
        try (InputStream in = TableSet.class.getResourceAsStream(ROOT + file)) {
            if (in == null) {
                return Map.of();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, JsonObject> tables = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                tables.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
            return Map.copyOf(tables);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
