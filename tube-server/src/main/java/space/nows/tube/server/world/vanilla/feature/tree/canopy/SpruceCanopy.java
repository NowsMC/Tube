package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class SpruceCanopy extends Canopy {

    private final IntSampler bareTrunk;

    SpruceCanopy(JsonObject json) {
        super(json);
        this.bareTrunk = IntSampler.read(json.get("trunk_height"));
    }

    @Override
    public int crownHeight(Xoroshiro random, int treeHeight) {
        return Math.max(4, treeHeight - bareTrunk.sample(random));
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        int size = random.nextInt(2);
        int widest = 1;
        int restart = 0;
        for (int dy = lift; dy >= -crownHeight; dy--) {
            layer(growth, random, at.x(), at.y(), at.z(), size, dy, at.wide());
            if (size >= widest) {
                size = restart;
                restart = 1;
                widest = Math.min(widest + 1, crownRadius + at.radiusOffset());
            } else {
                size++;
            }
        }
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        return corner(dx, dz, radius) && radius > 0;
    }
}
