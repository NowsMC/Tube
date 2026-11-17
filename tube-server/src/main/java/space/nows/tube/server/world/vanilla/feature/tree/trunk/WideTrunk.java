package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.List;

class WideTrunk extends Trunk {

    WideTrunk(JsonObject json) {
        super(json);
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        soilUnderSquare(growth, x, y, z);
        for (int i = 0; i < height; i++) {
            growth.log(x, y + i, z);
            if (i < height - 1) {
                growth.log(x + 1, y + i, z);
                growth.log(x + 1, y + i, z + 1);
                growth.log(x, y + i, z + 1);
            }
        }
        return List.of(new Attachment(x, y + height, z, 0, true));
    }
}
