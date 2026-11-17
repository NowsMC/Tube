package space.nows.tube.server.world.vanilla.biome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.noise.GradientNoise;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record BiomeInfo(String key, float temperature, List<List<String>> featureSteps, Set<String> features, List<String> carvers) {

    private static final GradientNoise ALTITUDE_CHILL = new GradientNoise(new Xoroshiro(1234L));
    private static final float SNOW_BELOW = 0.15F;

    public static Map<String, BiomeInfo> readAll(WorldgenData data) {
        Map<String, BiomeInfo> biomes = new HashMap<>();
        for (String key : data.names("biome")) {
            biomes.put(key, read(key, data.get("biome", key)));
        }
        return Map.copyOf(biomes);
    }

    private static BiomeInfo read(String key, JsonObject json) {
        List<List<String>> steps = new ArrayList<>();
        Set<String> all = new HashSet<>();
        for (JsonElement step : json.getAsJsonArray("features")) {
            List<String> features = new ArrayList<>();
            step.getAsJsonArray().forEach(feature -> features.add(WorldgenData.key(feature.getAsString())));
            steps.add(List.copyOf(features));
            all.addAll(features);
        }
        return new BiomeInfo(key, json.get("temperature").getAsFloat(), List.copyOf(steps), Set.copyOf(all), carvers(json.get("carvers")));
    }

    private static List<String> carvers(JsonElement json) {
        List<String> carvers = new ArrayList<>();
        if (json instanceof JsonArray array) {
            array.forEach(carver -> carvers.add(WorldgenData.key(carver.getAsString())));
        } else if (json != null && json.isJsonPrimitive()) {
            carvers.add(WorldgenData.key(json.getAsString()));
        }
        return List.copyOf(carvers);
    }

    public boolean hasFeature(int step, String feature) {
        return step < featureSteps.size() && featureSteps.get(step).contains(feature);
    }

    public float temperatureAt(int x, int y, int z, int seaLevel) {
        int snowLine = seaLevel + 17;
        if (y <= snowLine) {
            return temperature;
        }
        float wobble = (float) (ALTITUDE_CHILL.sample(x / 8.0, 0, z / 8.0) * 8.0);
        return temperature - (wobble + y - snowLine) * 0.05F / 40.0F;
    }

    public boolean snowsAt(int x, int y, int z, int seaLevel) {
        return temperatureAt(x, y, z, seaLevel) < SNOW_BELOW;
    }
}
