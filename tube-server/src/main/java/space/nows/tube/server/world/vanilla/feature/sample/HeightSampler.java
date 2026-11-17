package space.nows.tube.server.world.vanilla.feature.sample;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.YAnchor;
import space.nows.tube.server.world.vanilla.random.SeededRandom;

@FunctionalInterface
public interface HeightSampler {

    int sample(SeededRandom random);

    static HeightSampler read(JsonElement json, int minY, int height) {
        JsonObject object = json.getAsJsonObject();
        if (!object.has("type")) {
            int value = YAnchor.resolve(object, minY, height);
            return random -> value;
        }
        String type = WorldgenData.type(object);
        if (type.equals("constant")) {
            int value = YAnchor.resolve(object.getAsJsonObject("value"), minY, height);
            return random -> value;
        }
        if (!object.has("min_inclusive") || !object.has("max_inclusive")) {
            throw new IllegalArgumentException("Unsupported height provider " + type);
        }
        int min =YAnchor.resolve(object.getAsJsonObject("min_inclusive"), minY, height);
        int max = YAnchor.resolve(object.getAsJsonObject("max_inclusive"), minY, height);
        int inner = object.has("inner") ? object.get("inner").getAsInt() : 1;
        int plateau = object.has("plateau") ? object.get("plateau").getAsInt() : 0;
        return switch (type) {
            case "uniform" -> random -> min > max ? min : random.nextBetween(min, max);
            case "trapezoid" -> trapezoid(min, max, plateau);
            case "biased_to_bottom" -> biasedLow(min, max, inner);
            case "very_biased_to_bottom" -> veryBiasedLow(min, max, inner);
            default -> throw new IllegalArgumentException("Unsupported height provider " + type);
        };
    }

    private static HeightSampler trapezoid(int min, int max, int plateau) {
        return random -> {
            if (min > max) {
                return min;
            }
            int range = max - min;
            if (plateau >= range) {
                return random.nextBetween(min, max);
            }
            int slope = (range - plateau) / 2;
            return min + random.nextBetween(0, range - slope) + random.nextBetween(0, slope);
        };
    }

    private static HeightSampler biasedLow(int min, int max, int inner) {
        return random -> {
            if (max - min - inner + 1 <= 0) {
                return min;
            }
            int spread = random.nextInt(max - min - inner + 1);
            return min + random.nextInt(spread + inner);
        };
    }

    private static HeightSampler veryBiasedLow(int min, int max, int inner) {
        return random -> {
            if (max - min - inner + 1 <= 0) {
                return min;
            }
            int upper = random.nextBetween(min + inner, max);
            int lower = random.nextBetween(min, upper - 1);
            return random.nextBetween(min, lower - 1 + inner);
        };
    }
}
