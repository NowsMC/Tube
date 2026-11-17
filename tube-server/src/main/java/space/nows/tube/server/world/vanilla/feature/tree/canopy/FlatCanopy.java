package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class FlatCanopy extends Canopy {

    FlatCanopy(JsonObject json) {
        super(json);
    }

    @Override
    public int crownHeight(Xoroshiro random, int treeHeight) {
        return 0;
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        int y = at.y() + lift;
        layer(growth, random, at.x(), y, at.z(), crownRadius + at.radiusOffset(), -1 - crownHeight, at.wide());
        layer(growth, random, at.x(), y, at.z(), crownRadius - 1, -crownHeight, at.wide());
        layer(growth, random, at.x(), y, at.z(), crownRadius + at.radiusOffset() - 1, 0, at.wide());
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        if (y == 0) {
            return (dx > 1 || dz > 1) && dx != 0 && dz != 0;
        }
        return corner(dx, dz, radius) && radius > 0;
    }
}
