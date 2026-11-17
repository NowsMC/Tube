package space.nows.tube.server.world.vanilla.feature.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class BlockTestReaders {

    private static final Map<String, Function<JsonObject, BlockTest>> READERS = new HashMap<>();

    static {
        READERS.put("true", json -> BlockTest.ANY);
        READERS.put("matching_blocks", BlockTestReaders::matchingBlocks);
        READERS.put("matching_block_tag", BlockTestReaders::matchingTag);
        READERS.put("matching_fluids", BlockTestReaders::matchingFluids);
        READERS.put("would_survive", BlockTestReaders::wouldSurvive);
        READERS.put("solid", json -> (area, x, y, z) -> area.get(x, y, z).solid);
        READERS.put("replaceable", json -> (area, x, y, z) -> area.get(x, y, z).replaceable);
        READERS.put("has_sturdy_face", json -> (area, x, y, z) -> area.get(x, y, z).blocksMotion);
        READERS.put("unobstructed", json -> (area, x, y, z) -> !area.get(x, y, z).blocksMotion);
        READERS.put("inside_world_bounds", json -> (area, x, y, z) -> !area.outsideHeight(y));
        READERS.put("not", BlockTestReaders::not);
        READERS.put("all_of", json -> all(list(json.getAsJsonArray("predicates"))));
        READERS.put("any_of", json -> any(list(json.getAsJsonArray("predicates"))));
    }

    private BlockTestReaders() {
    }

    static BlockTest read(String type, JsonObject json) {
        Function<JsonObject, BlockTest> reader = READERS.get(type);
        if (reader == null) {
            throw new IllegalArgumentException("Unsupported block predicate " + type);
        }
        return reader.apply(json);
    }

    private static BlockTest matchingBlocks(JsonObject json) {
        Set<String> blocks = BlockNames.read(json.get("blocks"));
        return (area, x, y, z) -> blocks.contains(area.get(x, y, z).key);
    }

    private static BlockTest matchingTag(JsonObject json) {
        String tag = WorldgenData.key(json.get("tag").getAsString());
        return (area, x, y, z) -> area.get(x, y, z).is(tag);
    }

    private static BlockTest matchingFluids(JsonObject json) {
        Set<String> fluids = BlockNames.read(json.get("fluids"));
        boolean stillWater = fluids.contains("minecraft:water");
        boolean flowingWater = fluids.contains("minecraft:flowing_water");
        boolean stillLava = fluids.contains("minecraft:lava");
        boolean flowingLava = fluids.contains("minecraft:flowing_lava");
        return (area, x, y, z) -> {
            BlockState state = area.get(x, y, z);
            if (state.water) {
                return stillWater || flowingWater && !state.isSource();
            }
            if (state.lava) {
                return stillLava || flowingLava && !state.isSource();
            }
            return false;
        };
    }

    private static BlockTest wouldSurvive(JsonObject json) {
        BlockState state = BlockStates.of(json.getAsJsonObject("state"));
        return (area, x, y, z) -> PlantSupport.canStay(area, state, x, y, z);
    }

    private static BlockTest not(JsonObject json) {
        BlockTest inner = BlockTest.read(json.get("predicate"));
        return (area, x, y, z) -> !inner.test(area, x, y, z);
    }

    private static BlockTest[] list(JsonArray array) {
        BlockTest[] tests = new BlockTest[array.size()];
        for (int i = 0; i < tests.length; i++) {
            tests[i] = BlockTest.read(array.get(i));
        }
        return tests;
    }

    private static BlockTest all(BlockTest[] tests) {
        return (area, x, y, z) -> {
            for (BlockTest test : tests) {
                if (!test.test(area, x, y, z)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static BlockTest any(BlockTest[] tests) {
        return (area, x, y, z) -> {
            for (BlockTest test : tests) {
                if (test.test(area, x, y, z)) {
                    return true;
                }
            }
            return false;
        };
    }
}
