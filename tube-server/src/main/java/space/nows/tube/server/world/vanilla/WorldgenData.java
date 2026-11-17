package space.nows.tube.server.world.vanilla;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class WorldgenData {

    private static final String NAMESPACE = "minecraft:";

    private final Map<String, JsonObject> files;

    private WorldgenData(Map<String, JsonObject> files) {
        this.files = Map.copyOf(files);
    }

    public static WorldgenData read(Path zip) throws IOException {
        Map<String, JsonObject> files = new HashMap<>();
        try (ZipFile file = new ZipFile(zip.toFile())) {
            var entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".json")) {
                    continue;
                }
                try (InputStream in = file.getInputStream(entry)) {
                    JsonElement json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                    if (json.isJsonObject()) {
                        files.put(entry.getName().substring(0, entry.getName().length() - 5), json.getAsJsonObject());
                    }
                }
            }
        }
        return new WorldgenData(files);
    }

    public JsonObject get(String folder, String id) {
        JsonObject json = files.get(folder + "/" + path(id));
        if (json == null) {
            throw new IllegalArgumentException("Missing world generation data " + folder + "/" + id);
        }
        return json;
    }

    public boolean has(String folder, String id) {
        return files.containsKey(folder + "/" + path(id));
    }

    public Set<String> names(String folder) {
        Set<String> names = new TreeSet<>();
        String prefix = folder + "/";
        for (String key : files.keySet()) {
            if (key.startsWith(prefix)) {
                names.add(NAMESPACE + key.substring(prefix.length()));
            }
        }
        return names;
    }

    public static String path(String id) {
        return id.startsWith(NAMESPACE) ? id.substring(NAMESPACE.length()) : id;
    }

    public static String key(String id) {
        return id.indexOf(':') < 0 ? NAMESPACE + id : id;
    }

    public static String type(JsonObject json) {
        return path(json.get("type").getAsString());
    }
}
