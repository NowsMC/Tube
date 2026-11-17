package space.nows.tube.server.world.vanilla.surface;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.YAnchor;
import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.noise.PairedNoise;
import space.nows.tube.server.world.vanilla.noise.WorldNoises;
import space.nows.tube.server.world.vanilla.random.PositionalRandom;
import space.nows.tube.server.world.vanilla.terrain.TerrainSettings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class SurfaceConditionReader {

    private final TerrainSettings settings;
    private final WorldNoises noises;
    private final Map<String, BiomeInfo> biomes;
    private final Map<String, Function<JsonObject, SurfaceCondition>> readers = new HashMap<>();

    SurfaceConditionReader(TerrainSettings settings, WorldNoises noises, Map<String, BiomeInfo> biomes) {
        this.settings = settings;
        this.noises = noises;
        this.biomes = biomes;
        readers.put("biome", this::inBiome);
        readers.put("noise_threshold", this::noiseBetween);
        readers.put("vertical_gradient", this::fadingBelow);
        readers.put("y_above", this::aboveY);
        readers.put("water", this::aboveWater);
        readers.put("temperature", json -> this::snowy);
        readers.put("steep", json -> SurfaceColumn::steep);
        readers.put("not", json -> {
            SurfaceCondition inverted = read(json.getAsJsonObject("invert"));
            return column -> !inverted.test(column);
        });
        readers.put("hole", json -> column -> column.surfaceDepth <= 0);
        readers.put("above_preliminary_surface", json -> column -> column.y >= column.lowestSurface());
        readers.put("stone_depth", this::stoneDepth);
    }

    SurfaceCondition read(JsonObject json) {
        String type = WorldgenData.type(json);
        Function<JsonObject, SurfaceCondition> reader = readers.get(type);
        if (reader == null) {
            throw new IllegalArgumentException("Unsupported surface condition " + type);
        }
        return reader.apply(json);
    }

    private SurfaceCondition inBiome(JsonObject json) {
        Set<String> keys = new HashSet<>();
        JsonElement list = json.get("biome_is");
        if (list.isJsonArray()) {
            list.getAsJsonArray().forEach(biome -> keys.add(WorldgenData.key(biome.getAsString())));
        } else {
            keys.add(WorldgenData.key(list.getAsString()));
        }
        return column -> keys.contains(column.biome());
    }

    private SurfaceCondition noiseBetween(JsonObject json) {
        PairedNoise noise = noises.noise(json.get("noise"));
        double min = json.get("min_threshold").getAsDouble();
        double max = json.get("max_threshold").getAsDouble();
        return column -> {
            double value = noise.value(column.x, 0, column.z);
            return value >= min && value <= max;
        };
    }

    private SurfaceCondition fadingBelow(JsonObject json) {
        PositionalRandom random = noises.positional(json.get("random_name").getAsString());
        int solidBelow = anchor(json.get("true_at_and_below"));
        int clearAbove = anchor(json.get("false_at_and_above"));
        return column -> {
            if (column.y <= solidBelow) {
                return true;
            }
            if (column.y >= clearAbove) {
                return false;
            }
            double chance = 1.0 - (double) (column.y - solidBelow) / (clearAbove - solidBelow);
            return random.at(column.x, column.y, column.z).nextFloat() < chance;
        };
    }

    private SurfaceCondition aboveY(JsonObject json) {
        int anchor = anchor(json.get("anchor"));
        int multiplier = json.get("surface_depth_multiplier").getAsInt();
        boolean addStoneDepth = json.get("add_stone_depth").getAsBoolean();
        return column -> column.y + (addStoneDepth ? column.stoneAbove : 0) >= anchor + column.surfaceDepth * multiplier;
    }

    private SurfaceCondition aboveWater(JsonObject json) {
        int offset = json.get("offset").getAsInt();
        int multiplier = json.get("surface_depth_multiplier").getAsInt();
        boolean addStoneDepth = json.get("add_stone_depth").getAsBoolean();
        return column -> column.waterTop == Integer.MIN_VALUE
                || column.y + (addStoneDepth ? column.stoneAbove : 0) >= column.waterTop + offset + column.surfaceDepth * multiplier;
    }

    private boolean snowy(SurfaceColumn column) {
        BiomeInfo biome = biomes.get(column.biome());
        return biome != null && biome.snowsAt(column.x, column.y, column.z, settings.seaLevel());
    }

    private SurfaceCondition stoneDepth(JsonObject json) {
        int offset = json.get("offset").getAsInt();
        boolean addSurfaceDepth = json.get("add_surface_depth").getAsBoolean();
        int secondaryRange = json.get("secondary_depth_range").getAsInt();
        boolean ceiling = json.get("surface_type").getAsString().equals("ceiling");
        return column -> {
            int depth = ceiling ? column.stoneBelow : column.stoneAbove;
            int surface = addSurfaceDepth ? column.surfaceDepth : 0;
            int secondary = secondaryRange == 0 ? 0 : (int) ((column.secondary() + 1) / 2 * secondaryRange);
            return depth <= 1 + offset + surface + secondary;
        };
    }

    private int anchor(JsonElement json) {
        return YAnchor.resolve(json.getAsJsonObject(), settings.minY(), settings.height());
    }
}
