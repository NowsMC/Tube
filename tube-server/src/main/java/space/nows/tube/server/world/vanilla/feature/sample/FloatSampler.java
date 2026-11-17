package space.nows.tube.server.world.vanilla.feature.sample;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.random.SeededRandom;

@FunctionalInterface
public interface FloatSampler {

    float sample(SeededRandom random);

    static FloatSampler read(JsonElement json) {
        if (json.isJsonPrimitive()) {
            float value = json.getAsFloat();
            return random -> value;
        }
        JsonObject object = json.getAsJsonObject();
        String type = WorldgenData.type(object);
        return switch (type) {
            case "constant" -> {
                float value = object.get("value").getAsFloat();
                yield random -> value;
            }
            case "uniform" -> uniform(object.get("min_inclusive").getAsFloat(), object.get("max_exclusive").getAsFloat());
            case "trapezoid" -> trapezoid(object.get("min").getAsFloat(), object.get("max").getAsFloat(),
                    object.has("plateau") ? object.get("plateau").getAsFloat() : 0);
            case "clamped_normal" -> normal(object.get("mean").getAsFloat(), object.get("deviation").getAsFloat(),
                    object.get("min").getAsFloat(), object.get("max").getAsFloat());
            default -> throw new IllegalArgumentException("Unsupported float provider " + type);
        };
    }

    private static FloatSampler uniform(float min, float max) {
        return random -> random.nextFloat() * (max - min) + min;
    }

    private static FloatSampler trapezoid(float min, float max, float plateau) {
        float range = max - min;
        float slope = (range - plateau) / 2.0F;
        return random -> min + random.nextFloat() * (range - slope) + random.nextFloat() * slope;
    }

    private static FloatSampler normal(float mean, float deviation, float min, float max) {
        return random -> Math.clamp(mean + (float) random.nextGaussian() * deviation, min, max);
    }
}
