package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class CherryCanopy extends Canopy {

    private static final float HOLE_CHANCE = 0.25F;

    CherryCanopy(JsonObject json) {
        super(json);
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        int y = at.y() + lift;
        int size = crownRadius + at.radiusOffset() - 1;
        boolean wide = at.wide();
        layer(growth, random, at.x(), y, at.z(), size - 2, crownHeight - 3, wide);
        layer(growth, random, at.x(), y, at.z(), size - 1, crownHeight - 4, wide);
        for (int dy = crownHeight - 5; dy >= 0; dy--) {
            layer(growth, random, at.x(), y, at.z(), size, dy, wide);
        }
        layer(growth, random, at.x(), y, at.z(), size, -1, wide);
        layer(growth, random, at.x(), y, at.z(), size - 1, -2, wide);
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        if (y == -1 && (dx == radius || dz == radius) && random.nextFloat() < HOLE_CHANCE) {
            return true;
        }
        boolean corner = corner(dx, dz, radius);
        if (radius > 2) {
            return corner || dx + dz > radius * 2 - 2 && random.nextFloat() < HOLE_CHANCE;
        }
        return corner && random.nextFloat() < HOLE_CHANCE;
    }
}
