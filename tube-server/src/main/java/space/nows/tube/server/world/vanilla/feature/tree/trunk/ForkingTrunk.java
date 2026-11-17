package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class ForkingTrunk extends Trunk {

    ForkingTrunk(JsonObject json) {
        super(json);
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        growth.soil(x, y - 1, z);
        List<Attachment> crowns = new ArrayList<>();
        int[] lean = randomDirection(random);
        int leanFrom = height - random.nextInt(4) - 1;
        int leanSteps = 3 - random.nextInt(3);
        int logX = x;
        int logZ = z;
        int crownY = Integer.MIN_VALUE;
        for (int i = 0; i < height; i++) {
            if (i >= leanFrom && leanSteps > 0) {
                logX += lean[0];
                logZ += lean[1];
                leanSteps--;
            }
            if (growth.log(logX, y + i, logZ)) {
                crownY = y + i + 1;
            }
        }
        if (crownY != Integer.MIN_VALUE) {
            crowns.add(new Attachment(logX, crownY, logZ, 1, false));
        }
        int[] branch = randomDirection(random);
        if (branch != lean) {
            growBranch(growth, random, crowns, branch, leanFrom, height, x, y, z);
        }
        return crowns;
    }

    private static void growBranch(TreeGrowth growth, Xoroshiro random, List<Attachment> crowns, int[] branch, int leanFrom,
                                   int height, int x, int y, int z) {
        int start = leanFrom - random.nextInt(2) - 1;
        int length = 1 + random.nextInt(3);
        int logX = x;
        int logZ = z;
        int crownY = Integer.MIN_VALUE;
        for (int i = start; i < height && length > 0; i++, length--) {
            if (i < 1) {
                continue;
            }
            logX += branch[0];
            logZ += branch[1];
            if (growth.log(logX, y + i, logZ)) {
                crownY = y + i + 1;
            }
        }
        if (crownY != Integer.MIN_VALUE) {
            crowns.add(new Attachment(logX, crownY, logZ, 0, false));
        }
    }
}
