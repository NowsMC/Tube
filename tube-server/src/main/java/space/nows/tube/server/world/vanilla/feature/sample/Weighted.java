package space.nows.tube.server.world.vanilla.feature.sample;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.random.SeededRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Weighted<T> {

    private final List<T> values;
    private final int[] weights;
    private final int total;

    private Weighted(List<T> values, int[] weights) {
        this.values = List.copyOf(values);
        this.weights = weights;
        int sum = 0;
        for (int weight : weights) {
            sum += weight;
        }
        this.total = sum;
    }

    public static <T> Weighted<T> read(JsonArray entries, Function<JsonElement, T> reader) {
        List<T> values = new ArrayList<>();
        int[] weights = new int[entries.size()];
        for (int i = 0; i < weights.length; i++) {
            JsonObject entry = entries.get(i).getAsJsonObject();
            values.add(reader.apply(entry.get("data")));
            weights[i] = entry.get("weight").getAsInt();
        }
        return new Weighted<>(values, weights);
    }

    public T pick(SeededRandom random) {
        int roll = random.nextInt(total);
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return values.get(i);
            }
        }
        return values.getLast();
    }
}
