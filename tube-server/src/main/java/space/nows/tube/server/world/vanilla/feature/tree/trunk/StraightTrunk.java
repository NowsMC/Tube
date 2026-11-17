package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.List;

final class StraightTrunk extends Trunk {

    StraightTrunk(JsonObject json) {
        super(json);
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        growth.soil(x, y - 1, z);
        for (int i = 0; i < height; i++) {
            growth.log(x, y + i, z);
        }
        return List.of(new Attachment(x, y + height, z, 0, false));
    }
}
