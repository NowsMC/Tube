package space.nows.tube.server.world.vanilla.feature.placement;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

record SurfaceBand(Heightmap heightmap, long below, long above) implements PlacementStep {

    static SurfaceBand read(JsonObject json) {
        return new SurfaceBand(Heightmap.parse(json.get("heightmap").getAsString()),
                json.has("min_inclusive") ? json.get("min_inclusive").getAsLong() : Integer.MIN_VALUE,
                json.has("max_inclusive") ? json.get("max_inclusive").getAsLong() : Integer.MAX_VALUE);
    }

    @Override
    public void apply(DecorationPass pass, Xoroshiro random, int x, int y, int z, Next next) {
        long top = pass.area.height(heightmap, x, z);
        if (top + below <= y && y <= top + above) {
            next.at(x, y, z);
        }
    }
}
