package space.nows.tube.server.world.vanilla.feature.ore;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public final class ScatteredOreFeature implements Feature {

    private static final int MAX_SPREAD = 7;

    private final int size;
    private final OreTargets targets;

    public ScatteredOreFeature(JsonObject config) {
        this.size = config.get("size").getAsInt();
        this.targets = new OreTargets(config);
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        int count = random.nextInt(size + 1);
        for (int i = 0; i < count; i++) {
            int spread = Math.min(i, MAX_SPREAD);
            int px = x + offset(random, spread);
            int py = y + offset(random, spread);
            int pz = z + offset(random, spread);
            targets.replace(pass.area, random, px, py, pz);
        }
        return true;
    }

    private static int offset(Xoroshiro random, int spread) {
        return Math.round((random.nextFloat() - random.nextFloat()) * spread);
    }
}
