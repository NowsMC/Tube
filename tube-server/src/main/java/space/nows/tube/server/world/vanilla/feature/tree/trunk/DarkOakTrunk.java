package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class DarkOakTrunk extends Trunk {

    DarkOakTrunk(JsonObject json) {
        super(json);
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        soilUnderSquare(growth, x, y, z);
        int[] lean = randomDirection(random);
        int leanFrom = height - random.nextInt(4);
        int leanSteps = 2 - random.nextInt(3);
        int logX = x;
        int logZ = z;
        int top = y + height - 1;
        for (int i = 0; i < height; i++) {
            if (i >= leanFrom && leanSteps > 0) {
                logX += lean[0];
                logZ += lean[1];
                leanSteps--;
            }
            BlockState current = growth.area.get(logX, y + i, logZ);
            if (current.air || current.leaves) {
                growth.log(logX, y + i, logZ);
                growth.log(logX + 1, y + i, logZ);
                growth.log(logX, y + i, logZ + 1);
                growth.log(logX + 1, y + i, logZ + 1);
            }
        }
        List<Attachment> crowns = new ArrayList<>();
        crowns.add(new Attachment(logX, top, logZ, 0, true));
        addRootBranches(growth, random, crowns, top, x, z);
        return crowns;
    }

    private static void addRootBranches(TreeGrowth growth, Xoroshiro random, List<Attachment> crowns, int top, int x, int z) {
        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                boolean outsideCore = dx < 0 || dx > 1 || dz < 0 || dz > 1;
                if (outsideCore && random.nextInt(3) <= 0) {
                    int length = random.nextInt(3) + 2;
                    for (int i = 0; i < length; i++) {
                        growth.log(x + dx, top - i - 1, z + dz);
                    }
                    crowns.add(new Attachment(x + dx, top, z + dz, 0, false));
                }
            }
        }
    }
}
