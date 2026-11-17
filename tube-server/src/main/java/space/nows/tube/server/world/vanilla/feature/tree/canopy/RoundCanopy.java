package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class RoundCanopy extends Canopy {

    RoundCanopy(JsonObject json) {
        super(json);
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        for (int dy = lift; dy >= lift - crownHeight; dy--) {
            boolean middle = dy != lift && dy != lift - crownHeight;
            layer(growth, random, at.x(), at.y(), at.z(), crownRadius + (middle ? 1 : 0), dy, at.wide());
        }
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        return (dx + 0.5F) * (dx + 0.5F) + (dz + 0.5F) * (dz + 0.5F) > radius * radius;
    }
}
