package space.nows.tube.server.world.vanilla.noise;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.random.PositionalRandom;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldNoises {

    private final WorldgenData data;
    private final PositionalRandom root;
    private final Map<String, PairedNoise> noises = new ConcurrentHashMap<>();
    private final Map<String, PositionalRandom> randoms = new ConcurrentHashMap<>();

    public WorldNoises(WorldgenData data, long seed) {
        this.data = data;
        this.root = new Xoroshiro(seed).split();
    }

    public Xoroshiro named(String name) {
        return root.named(name);
    }

    public PairedNoise noise(String id) {
        String key = WorldgenData.key(id);
        return noises.computeIfAbsent(key, name -> create(root.named(name), data.get("noise", name)));
    }

    public PairedNoise noise(JsonElement reference) {
        if (reference.isJsonPrimitive()) {
            return noise(reference.getAsString());
        }
        return create(root.named(reference.toString()), reference.getAsJsonObject());
    }

    public PositionalRandom positional(String id) {
        return randoms.computeIfAbsent(WorldgenData.key(id), name -> root.named(name).split());
    }

    public static PairedNoise create(Xoroshiro random, JsonObject parameters) {
        JsonArray list = parameters.getAsJsonArray("amplitudes");
        double[] amplitudes = new double[list.size()];
        for (int i = 0; i < amplitudes.length; i++) {
            amplitudes[i] = list.get(i).getAsDouble();
        }
        return new PairedNoise(random, parameters.get("firstOctave").getAsInt(), amplitudes);
    }
}
