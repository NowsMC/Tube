package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.Decoration;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

public record SequenceFeature(List<Decoration> steps) implements Feature {

    public static SequenceFeature read(JsonObject config, DecorationSource source) {
        List<Decoration> steps = new ArrayList<>();
        config.getAsJsonArray("features").forEach(element -> steps.add(source.decoration(element)));
        return new SequenceFeature(List.copyOf(steps));
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        boolean placed = false;
        for (Decoration step : steps) {
            placed |= step.place(pass, random, x, y, z);
        }
        return placed;
    }
}
