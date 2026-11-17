package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.test.BlockTest;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

public record BlockColumnFeature(List<Layer> layers, int step, BlockTest room, boolean keepTip) implements Feature {

    public record Layer(IntSampler height, BlockChoice block) {
    }

    public static BlockColumnFeature read(JsonObject config) {
        List<Layer> layers = new ArrayList<>();
        JsonArray list = config.getAsJsonArray("layers");
        for (int i = 0; i < list.size(); i++) {
            JsonObject layer = list.get(i).getAsJsonObject();
            layers.add(new Layer(IntSampler.read(layer.get("height")), BlockChoice.read(layer.get("provider"))));
        }
        int step = config.get("direction").getAsString().equals("down") ? -1 : 1;
        return new BlockColumnFeature(List.copyOf(layers), step, BlockTest.read(config.get("allowed_placement")),
                config.get("prioritize_tip").getAsBoolean());
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        int[] lengths = new int[layers.size()];
        int total = 0;
        for (int i = 0; i < lengths.length; i++) {
            lengths[i] = layers.get(i).height().sample(random);
            total += lengths[i];
        }
        if (total == 0) {
            return false;
        }
        int fits = roomFor(area, x, y, z, total);
        if (fits < total) {
            shorten(lengths, total - fits);
        }
        int placeY = y;
        for (int i = 0; i < lengths.length; i++) {
            for (int block = 0; block < lengths[i]; block++) {
                BlockState state = layers.get(i).block().choose(random, area, x, placeY, z);
                if (state != null) {
                    area.set(x, placeY, z, state);
                }
                placeY += step;
            }
        }
        return true;
    }

    private int roomFor(DecorationArea area, int x, int y, int z, int wanted) {
        for (int i = 0; i < wanted; i++) {
            if (!room.test(area, x, y + i * step, z)) {
                return i;
            }
        }
        return wanted;
    }

    private void shorten(int[] lengths, int excess) {
        int first = keepTip ? 0 : lengths.length - 1;
        int direction = keepTip ? 1 : -1;
        for (int layer = first; layer >= 0 && layer < lengths.length && excess > 0; layer += direction) {
            int taken = Math.min(lengths[layer], excess);
            excess -= taken;
            lengths[layer] -= taken;
        }
    }
}
