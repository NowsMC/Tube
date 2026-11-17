package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class PineCanopy extends Canopy {

    PineCanopy(JsonObject json) {
        super(json);
    }

    @Override
    public int crownRadius(Xoroshiro random, int trunkLength) {
        return super.crownRadius(random, trunkLength) + random.nextInt(Math.max(trunkLength + 1, 1));
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        int size = 0;
        for (int dy = lift; dy >= lift - crownHeight; dy--) {
            layer(growth, random, at.x(), at.y(), at.z(), size, dy, at.wide());
            if (size >= 1 && dy == lift - crownHeight + 1) {
                size--;
            } else if (size < crownRadius + at.radiusOffset()) {
                size++;
            }
        }
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        return corner(dx, dz, radius) && radius > 0;
    }
}
