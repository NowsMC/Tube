package space.nows.tube.server.world.vanilla.feature.choice;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.sample.Weighted;
import space.nows.tube.server.world.vanilla.feature.test.BlockTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class BlockChoiceReaders {

    private static final String[] AXES = {"x", "y", "z"};
    private static final Map<String, Function<JsonObject, BlockChoice>> READERS = new HashMap<>();

    static {
        READERS.put("simple_state_provider", json -> BlockChoice.always(BlockStates.of(json.getAsJsonObject("state"))));
        READERS.put("rotated_block_provider", BlockChoiceReaders::rotated);
        READERS.put("weighted_state_provider", BlockChoiceReaders::weighted);
        READERS.put("randomized_int_state_provider", BlockChoiceReaders::randomizedProperty);
        READERS.put("rule_based_state_provider", BlockChoiceReaders::ruleBased);
    }

    private BlockChoiceReaders() {
    }

    static BlockChoice read(String type, JsonObject json) {
        Function<JsonObject, BlockChoice> reader = READERS.get(type);
        if (reader == null) {
            throw new IllegalArgumentException("Unsupported block state provider " + type);
        }
        return reader.apply(json);
    }

    private static BlockChoice rotated(JsonObject json) {
        BlockState state = BlockStates.of(json.getAsJsonObject("state"));
        return (random, area, x, y, z) -> state.with("axis", AXES[random.nextInt(AXES.length)]);
    }

    private static BlockChoice weighted(JsonObject json) {
        Weighted<BlockState> states = Weighted.read(json.getAsJsonArray("entries"), data -> BlockStates.of(data.getAsJsonObject()));
        return (random, area, x, y, z) -> states.pick(random);
    }

    private static BlockChoice randomizedProperty(JsonObject json) {
        BlockChoice source = BlockChoice.read(json.get("source"));
        String property = json.get("property").getAsString();
        IntSampler values = IntSampler.read(json.get("values"));
        return (random, area, x, y, z) -> {
            BlockState state = source.choose(random, area, x, y, z);
            return state == null ? null : state.with(property, Integer.toString(values.sample(random)));
        };
    }

    private static BlockChoice ruleBased(JsonObject json) {
        BlockChoice fallback = json.has("fallback") ? BlockChoice.read(json.get("fallback")) : null;
        List<BlockTest> tests = new ArrayList<>();
        List<BlockChoice> results = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("rules")) {
            JsonObject rule = element.getAsJsonObject();
            tests.add(BlockTest.read(rule.get("if_true")));
            results.add(BlockChoice.read(rule.get("then")));
        }
        return (random, area, x, y, z) -> {
            for (int i = 0; i < tests.size(); i++) {
                if (tests.get(i).test(area, x, y, z)) {
                    return results.get(i).choose(random, area, x, y, z);
                }
            }
            return fallback == null ? null : fallback.choose(random, area, x, y, z);
        };
    }
}
