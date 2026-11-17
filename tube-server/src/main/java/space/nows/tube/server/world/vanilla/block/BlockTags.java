package space.nows.tube.server.world.vanilla.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class BlockTags {

    private static final String RESOURCE = "/net/minestom/data/tags/block.json";
    private static final Map<String, Set<String>> TAGS_OF_BLOCK = load();

    private BlockTags() {
    }

    public static Set<String> of(String block) {
        return TAGS_OF_BLOCK.getOrDefault(block, Set.of());
    }

    public static Set<String> members(String tag) {
        Set<String> blocks = new HashSet<>();
        TAGS_OF_BLOCK.forEach((block, tags) -> {
            if (tags.contains(tag)) {
                blocks.add(block);
            }
        });
        return blocks;
    }

    private static Map<String, Set<String>> load() {
        JsonObject definitions = readDefinitions();
        Map<String, Set<String>> tagsOfBlock = new HashMap<>();
        for (String tag : definitions.keySet()) {
            for (String block : expand(definitions, tag, new HashSet<>())) {
                tagsOfBlock.computeIfAbsent(block, key -> new HashSet<>()).add(tag);
            }
        }
        Map<String, Set<String>> frozen = new HashMap<>();
        tagsOfBlock.forEach((block, tags) -> frozen.put(block, Set.copyOf(tags)));
        return Map.copyOf(frozen);
    }

    private static JsonObject readDefinitions() {
        try (InputStream in = BlockTags.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return new JsonObject();
            }
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Set<String> expand(JsonObject definitions, String tag, Set<String> visited) {
        Set<String> blocks = new HashSet<>();
        JsonObject definition = definitions.getAsJsonObject(tag);
        if (definition == null || !visited.add(tag)) {
            return blocks;
        }
        for (JsonElement value : definition.getAsJsonArray("values")) {
            String name = value.getAsString();
            if (name.startsWith("#")) {
                blocks.addAll(expand(definitions, name.substring(1), visited));
            } else {
                blocks.add(name);
            }
        }
        return blocks;
    }
}
