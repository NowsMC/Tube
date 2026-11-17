package space.nows.tube.server.world.vanilla.feature.sample;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.random.SeededRandom;

import java.util.Map;
import java.util.function.Function;

@FunctionalInterface
public interface IntSampler {

    int sample(SeededRandom random);

    static IntSampler read(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return fixed(json.getAsInt());
        }
        JsonObject object = json.getAsJsonObject();
        String type = WorldgenData.type(object);
        Function<JsonObject, IntSampler> reader = Readers.ALL.get(type);
        if (reader == null) {
            throw new IllegalArgumentException("Unsupported int provider " + type);
        }
        return reader.apply(object);
    }

    static IntSampler fixed(int value) {
        return random -> value;
    }

    final class Readers {

        private static final Map<String, Function<JsonObject, IntSampler>> ALL = Map.of(
                "constant", json -> fixed(json.get("value").getAsInt()),
                "uniform", Readers::uniform,
                "biased_to_bottom", Readers::biasedLow,
                "clamped", Readers::clamped,
                "clamped_normal", Readers::normal,
                "trapezoid", Readers::trapezoid,
                "weighted_list", Readers::weighted);

        private Readers() {
        }

        private static IntSampler uniform(JsonObject json) {
            int min = json.get("min_inclusive").getAsInt();
            int max = json.get("max_inclusive").getAsInt();
            return random -> random.nextBetween(min, max);
        }

        private static IntSampler biasedLow(JsonObject json) {
            int min = json.get("min_inclusive").getAsInt();
            int max = json.get("max_inclusive").getAsInt();
            return random -> min + random.nextInt(random.nextInt(max - min + 1) + 1);
        }

        private static IntSampler clamped(JsonObject json) {
            IntSampler source = read(json.get("source"));
            int min = json.get("min_inclusive").getAsInt();
            int max = json.get("max_inclusive").getAsInt();
            return random -> Math.clamp(source.sample(random), min, max);
        }

        private static IntSampler normal(JsonObject json) {
            float mean = json.get("mean").getAsFloat();
            float deviation = json.get("deviation").getAsFloat();
            int min = json.get("min_inclusive").getAsInt();
            int max = json.get("max_inclusive").getAsInt();
            return random -> (int) Math.clamp(mean + random.nextGaussian() * deviation, min, max);
        }

        private static IntSampler trapezoid(JsonObject json) {
            int min = json.get("min").getAsInt();
            int max = json.get("max").getAsInt();
            int plateau = json.has("plateau") ? json.get("plateau").getAsInt() : 0;
            int range = max - min;
            int slope = (range - plateau) / 2;
            return random -> min + random.nextBetween(0, range - slope) + random.nextBetween(0, slope);
        }

        private static IntSampler weighted(JsonObject json) {
            JsonArray entries = json.getAsJsonArray("distribution");
            Weighted<IntSampler> choices = Weighted.read(entries, entry -> read(entry));
            return random -> choices.pick(random).sample(random);
        }
    }
}
