package space.nows.tube.server.world.vanilla.feature.placement;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.noise.GradientNoise;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.function.DoubleUnaryOperator;

record NoiseCount(double spread, DoubleUnaryOperator count) implements PlacementStep {

    private static final GradientNoise PATCHES = new GradientNoise(new Xoroshiro(2345L));

    static NoiseCount threshold(JsonObject json) {
        double level = json.get("noise_level").getAsDouble();
        int below = json.get("below_noise").getAsInt();
        int above = json.get("above_noise").getAsInt();
        return new NoiseCount(200.0, noise -> noise < level ? below : above);
    }

    static NoiseCount scaled(JsonObject json) {
        int ratio = json.get("noise_to_count_ratio").getAsInt();
        double offset = json.has("noise_offset") ? json.get("noise_offset").getAsDouble() : 0;
        return new NoiseCount(json.get("noise_factor").getAsDouble(), noise -> Math.ceil((noise + offset) * ratio));
    }

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        int times = (int) count.applyAsDouble(PATCHES.sample(x / spread, 0, z / spread));
        for (int i = 0; i < times; i++) {
            next.at(x, y, z);
        }
    }
}
