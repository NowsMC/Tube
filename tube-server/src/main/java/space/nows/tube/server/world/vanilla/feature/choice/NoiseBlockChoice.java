package space.nows.tube.server.world.vanilla.feature.choice;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.noise.PairedNoise;
import space.nows.tube.server.world.vanilla.noise.WorldNoises;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class NoiseBlockChoice {

    private NoiseBlockChoice() {
    }

    static BlockChoice threshold(JsonObject json) {
        PairedNoise noise = noise(json);
        double scale = json.get("scale").getAsDouble();
        float threshold = json.get("threshold").getAsFloat();
        float highChance = json.get("high_chance").getAsFloat();
        BlockState common = BlockStates.of(json.getAsJsonObject("default_state"));
        BlockState[] low = states(json.getAsJsonArray("low_states"));
        BlockState[] high = states(json.getAsJsonArray("high_states"));
        return (random, area, x, y, z) -> {
            if (noise.value(x * scale, y * scale, z * scale) < threshold) {
                return low[random.nextInt(low.length)];
            }
            return random.nextFloat() < highChance ? high[random.nextInt(high.length)] : common;
        };
    }

    static BlockChoice gradient(JsonObject json) {
        PairedNoise noise = noise(json);
        double scale = json.get("scale").getAsDouble();
        BlockState[] states = states(json.getAsJsonArray("states"));
        return (random, area, x, y, z) -> along(states, noise.value(x * scale, y * scale, z * scale));
    }

    static BlockChoice mixed(JsonObject json) {
        PairedNoise noise = noise(json);
        double scale = json.get("scale").getAsDouble();
        BlockState[] states = states(json.getAsJsonArray("states"));
        JsonObject variety = json.getAsJsonObject("variety");
        int fewest = variety.has("min_inclusive") ? variety.get("min_inclusive").getAsInt() : 1;
        int most = variety.has("max_inclusive") ? variety.get("max_inclusive").getAsInt() : 1;
        PairedNoise slow = WorldNoises.create(new Xoroshiro(json.get("seed").getAsLong() + 1), json.getAsJsonObject("slow_noise"));
        double slowScale = json.get("slow_scale").getAsDouble();
        return (random, area, x, y, z) -> {
            double slowValue = slow.value(x * slowScale, y * slowScale, z * slowScale);
            int count = (int) Math.clamp(fewest + (slowValue + 1) / 2 * (most + 1 - fewest), fewest, most + 1);
            BlockState[] palette = new BlockState[Math.max(1, count)];
            for (int i = 0; i < palette.length; i++) {
                palette[i] = along(states, noise.value((x + i * 54545) * scale, y * scale, (z + i * 34234) * scale));
            }
            return along(palette, noise.value(x * scale, y * scale, z * scale));
        };
    }

    private static PairedNoise noise(JsonObject json) {
        return WorldNoises.create(new Xoroshiro(json.get("seed").getAsLong()), json.getAsJsonObject("noise"));
    }

    private static BlockState along(BlockState[] states, double noise) {
        double position = Math.clamp((1 + noise) / 2, 0.0, 0.9999);
        return states[(int) (position * states.length)];
    }

    private static BlockState[] states(JsonArray array) {
        BlockState[] states = new BlockState[array.size()];
        for (int i = 0; i < states.length; i++) {
            states[i] = BlockStates.of(array.get(i).getAsJsonObject());
        }
        return states;
    }
}
