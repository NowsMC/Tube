package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class CherryTrunk extends Trunk {

    private final IntSampler branchCount;
    private final IntSampler branchLength;

    CherryTrunk(JsonObject json) {
        super(json);
        this.branchCount = json.has("branch_count") ? IntSampler.read(json.get("branch_count")) : IntSampler.fixed(1);
        this.branchLength = json.has("branch_horizontal_length") ? IntSampler.read(json.get("branch_horizontal_length")) : IntSampler.fixed(2);
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        growth.soil(x, y - 1, z);
        List<Attachment> crowns = new ArrayList<>();
        int branches = Math.clamp(branchCount.sample(random), 1, 3);
        int trunkTop = branches == 3 ? height - 1 : height;
        for (int i = 0; i < trunkTop; i++) {
            growth.log(x, y + i, z);
        }
        if (branches != 2) {
            crowns.add(new Attachment(x, y + trunkTop, z, 0, false));
        }
        int firstSide = random.nextInt(4);
        for (int branch = 0; branch < Math.min(branches, 2); branch++) {
            crowns.add(growBranch(growth, random, HORIZONTAL[(firstSide + branch * 2) % 4], height, x, y, z));
        }
        return crowns;
    }

    private Attachment growBranch(TreeGrowth growth, Xoroshiro random, int[] side, int height, int x, int y, int z) {
        int length = branchLength.sample(random);
        int startY = y + Math.max(1, height - 4 + random.nextInt(2));
        int endY = y + height - random.nextInt(2);
        String axis = side[0] != 0 ? "x" : "z";
        int logX = x;
        int logZ = z;
        for (int i = 1; i <= length; i++) {
            logX = x + side[0] * i;
            logZ = z + side[1] * i;
            int logY = startY + (endY - startY) * i / length;
            growth.log(logX, logY, logZ, i == length ? "y" : axis);
        }
        return new Attachment(logX, endY + 1, logZ, 0, false);
    }
}
