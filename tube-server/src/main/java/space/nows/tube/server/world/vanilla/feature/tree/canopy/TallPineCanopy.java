package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class TallPineCanopy extends Canopy {

    TallPineCanopy(JsonObject json) {
        super(json);
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        int previous = 0;
        for (int y = at.y() - crownHeight + lift; y <= at.y() + lift; y++) {
            int below = at.y() - y;
            int size = crownRadius + at.radiusOffset() + (int) Math.floor((float) below / crownHeight * 3.5F);
            int rowSize = below > 0 && size == previous && (y & 1) == 0 ? size + 1 : size;
            layer(growth, random, at.x(), y, at.z(), rowSize, 0, at.wide());
            previous = size;
        }
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        return dx + dz >= 7 || dx * dx + dz * dz > radius * radius;
    }
}
