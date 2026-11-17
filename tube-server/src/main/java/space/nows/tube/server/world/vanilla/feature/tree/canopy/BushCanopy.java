package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class BushCanopy extends Canopy {

    BushCanopy(JsonObject json) {
        super(json);
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        for (int dy = lift; dy >= lift - crownHeight; dy--) {
            layer(growth, random, at.x(), at.y(), at.z(), crownRadius + at.radiusOffset() - 1 - dy, dy, at.wide());
        }
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        return corner(dx, dz, radius) && random.nextInt(2) == 0;
    }
}
