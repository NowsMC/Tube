package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class BentTrunk extends Trunk {

    private final IntSampler bendLength;
    private final int leavesFrom;

    BentTrunk(JsonObject json) {
        super(json);
        this.bendLength = json.has("bend_length") ? IntSampler.read(json.get("bend_length")) : IntSampler.fixed(1);
        this.leavesFrom = json.has("min_height_for_leaves") ? json.get("min_height_for_leaves").getAsInt() : 1;
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        int[] bend = randomDirection(random);
        int last = height - 1;
        int logX = x;
        int logY = y;
        int logZ = z;
        growth.soil(x, y - 1, z);
        List<Attachment> crowns = new ArrayList<>();
        for (int i = 0; i <= last; i++, logY++) {
            if (i + 1 >= last + random.nextInt(2)) {
                logX += bend[0];
                logZ += bend[1];
            }
            if (growth.canGrowInto(logX, logY, logZ)) {
                growth.log(logX, logY, logZ);
            }
            if (i >= leavesFrom) {
                crowns.add(new Attachment(logX, logY, logZ, 0, false));
            }
        }
        int sideways = bendLength.sample(random);
        for (int i = 0; i <= sideways; i++) {
            if (growth.canGrowInto(logX, logY, logZ)) {
                growth.log(logX, logY, logZ);
            }
            crowns.add(new Attachment(logX, logY, logZ, 0, false));
            logX += bend[0];
            logZ += bend[1];
        }
        return crowns;
    }
}
