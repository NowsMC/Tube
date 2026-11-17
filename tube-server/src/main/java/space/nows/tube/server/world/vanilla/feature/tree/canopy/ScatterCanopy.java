package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

final class ScatterCanopy extends Canopy {

    private final int attempts;

    ScatterCanopy(JsonObject json) {
        super(json);
        this.attempts = json.has("leaf_placement_attempts") ? json.get("leaf_placement_attempts").getAsInt() : 0;
    }

    @Override
    protected void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift) {
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(crownRadius) - random.nextInt(crownRadius);
            int dy = random.nextInt(crownHeight) - random.nextInt(crownHeight);
            int dz = random.nextInt(crownRadius) - random.nextInt(crownRadius);
            growth.leaf(at.x() + dx, at.y() + dy, at.z() + dz);
        }
    }

    @Override
    protected boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        return false;
    }
}
