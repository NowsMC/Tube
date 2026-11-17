package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

class BlobCanopy extends Canopy {

    BlobCanopy(JsonObject json) {
        super(json);
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        for (int dy = lift; dy >= lift - crownHeight; dy--) {
            int size = Math.max(crownRadius + at.radiusOffset() - 1 - dy / 2, 0);
            layer(growth, random, at.x(), at.y(), at.z(), size, dy, at.wide());
        }
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        return corner(dx, dz, radius) && (random.nextInt(2) == 0 || y == 0);
    }
}
