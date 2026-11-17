package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class DarkOakCanopy extends Canopy {

    DarkOakCanopy(JsonObject json) {
        super(json);
    }

    @Override
    public int crownHeight(Xoroshiro random, int treeHeight) {
        return 4;
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        int y = at.y() + lift;
        boolean wide = at.wide();
        if (!wide) {
            layer(growth, random, at.x(), y, at.z(), crownRadius + 2, -1, false);
            layer(growth, random, at.x(), y, at.z(), crownRadius + 1, 0, false);
            return;
        }
        layer(growth, random, at.x(), y, at.z(), crownRadius + 2, -1, true);
        layer(growth, random, at.x(), y, at.z(), crownRadius + 3, 0, true);
        layer(growth, random, at.x(), y, at.z(), crownRadius + 2, 1, true);
        if (random.nextBoolean()) {
            layer(growth, random, at.x(), y, at.z(), crownRadius, 2, true);
        }
    }

    @Override
    protected boolean leaveOutSigned(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        boolean outerCorner = (dx == -radius || dx >= radius) && (dz == -radius || dz >= radius);
        if (y == 0 && wide && outerCorner) {
            return true;
        }
        return super.leaveOutSigned(random, dx, y, dz, radius, wide);
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        if (y == -1 && !wide) {
            return corner(dx, dz, radius);
        }
        return y == 1 && dx + dz > radius * 2 - 2;
    }
}
