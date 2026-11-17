package space.nows.tube.server.world.vanilla.feature.placement;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.sample.HeightSampler;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.test.BlockTest;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PlacementSteps {

    private final Map<String, Function<JsonObject, PlacementStep>> readers = new HashMap<>();

    public PlacementSteps(int minY, int height) {
        readers.put("count", json -> new Repeat(IntSampler.read(json.get("count"))));
        readers.put("in_square", json -> new SpreadInChunk());
        readers.put("biome", json -> new BiomeFilter());
        readers.put("block_predicate_filter", json -> new BlockFilter(BlockTest.read(json.get("predicate"))));
        readers.put("heightmap", json -> new OnHeightmap(Heightmap.parse(json.get("heightmap").getAsString())));
        readers.put("random_offset", json -> new Jitter(IntSampler.read(json.get("xz_spread")), IntSampler.read(json.get("y_spread"))));
        readers.put("height_range", json -> new AtHeight(HeightSampler.read(json.get("height"), minY, height)));
        readers.put("rarity_filter", json -> new Rarity(1.0F / json.get("chance").getAsInt()));
    }

    public PlacementStep read(JsonObject json) {
        String type = WorldgenData.type(json);
        Function<JsonObject, PlacementStep> reader = readers.get(type);
        if (reader == null) {
            throw new IllegalArgumentException("Unsupported placement modifier " + type);
        }
        return reader.apply(json);
    }
}
