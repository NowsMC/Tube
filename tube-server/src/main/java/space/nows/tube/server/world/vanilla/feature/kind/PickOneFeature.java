package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.Decoration;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.sample.Weighted;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record PickOneFeature(Function<Xoroshiro, Decoration> picker) implements Feature {

    public static PickOneFeature uniform(JsonObject config, DecorationSource source) {
        List<Decoration> options = new ArrayList<>();
        config.getAsJsonArray("features").forEach(element -> options.add(source.decoration(element)));
        return new PickOneFeature(random -> options.get(random.nextInt(options.size())));
    }

    public static PickOneFeature coinFlip(JsonObject config, DecorationSource source) {
        Decoration heads = source.decoration(config.get("feature_true"));
        Decoration tails = source.decoration(config.get("feature_false"));
        return new PickOneFeature(random -> random.nextBoolean() ? heads : tails);
    }

    public static PickOneFeature weighted(JsonObject config, DecorationSource source) {
        Weighted<Decoration> options = Weighted.read(config.getAsJsonArray("features"), source::decoration);
        return new PickOneFeature(options::pick);
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        return picker.apply(random).place(pass, random, x, y, z);
    }
}
