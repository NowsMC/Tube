package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class WideBranchingTrunk extends WideTrunk {

    private static final int BRANCH_STEPS = 5;

    WideBranchingTrunk(JsonObject json) {
        super(json);
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        List<Attachment> crowns = new ArrayList<>(super.grow(growth, random, height, x, y, z));
        for (int level = height - 2 - random.nextInt(4); level > height / 2; level -= 2 + random.nextInt(4)) {
            float angle = random.nextFloat() * (float) (Math.PI * 2);
            int endX = 0;
            int endZ = 0;
            for (int step = 0; step < BRANCH_STEPS; step++) {
                endX = (int) (1.5F + Math.cos(angle) * step);
                endZ = (int) (1.5F + Math.sin(angle) * step);
                growth.log(x + endX, y + level - 3 + step / 2, z + endZ);
            }
            crowns.add(new Attachment(x + endX, y + level, z + endZ, -2, false));
        }
        return crowns;
    }
}
