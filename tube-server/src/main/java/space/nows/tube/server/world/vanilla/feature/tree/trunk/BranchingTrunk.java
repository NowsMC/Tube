package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class BranchingTrunk extends Trunk {

    private record Bough(int x, int y, int z, int baseY) {
    }

    BranchingTrunk(JsonObject json) {
        super(json);
    }

    @Override
    public List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z) {
        int treeHeight = height + 2;
        int trunkTop = (int) Math.floor(treeHeight * 0.618);
        growth.soil(x, y - 1, z);
        int perLayer = Math.min(1, (int) Math.floor(1.382 + Math.pow(treeHeight / 13.0, 2.0)));
        int boughCeiling = y + trunkTop;
        List<Bough> boughs = new ArrayList<>();
        boughs.add(new Bough(x, y + treeHeight - 5, z, boughCeiling));
        for (int layer = treeHeight - 5; layer >= 0; layer--) {
            float spread = crownProfile(treeHeight, layer);
            if (spread < 0) {
                continue;
            }
            for (int i = 0; i < perLayer; i++) {
                Bough bough = tryBough(growth, random, spread, boughCeiling, x, y + layer - 1, z);
                if (bough != null) {
                    boughs.add(bough);
                }
            }
        }
        LimbLine.draw(growth, x, y, z, x, y + trunkTop, z, true);
        List<Attachment> crowns = new ArrayList<>();
        for (Bough bough : boughs) {
            boolean highEnough = bough.baseY - y >= treeHeight * 0.2;
            boolean offTrunk = bough.x != x || bough.y != bough.baseY || bough.z != z;
            if (highEnough && offTrunk) {
                LimbLine.draw(growth, x, bough.baseY, z, bough.x, bough.y, bough.z, true);
            }
        }
        for (Bough bough : boughs) {
            if (bough.baseY - y >= treeHeight * 0.2) {
                crowns.add(new Attachment(bough.x, bough.y, bough.z, 0, false));
            }
        }
        return crowns;
    }

    private static Bough tryBough(TreeGrowth growth, Xoroshiro random, float spread, int ceiling, int x, int endY, int z) {
        double distance = spread * (random.nextFloat() + 0.328);
        double angle = random.nextFloat() * 2.0F * Math.PI;
        int endX = x + (int) Math.floor(distance * Math.sin(angle) + 0.5);
        int endZ = z + (int) Math.floor(distance * Math.cos(angle) + 0.5);
        if (!LimbLine.draw(growth, endX, endY, endZ, endX, endY + 5, endZ, false)) {
            return null;
        }
        int dx = x - endX;
        int dz = z - endZ;
        double slopedBase = endY - Math.sqrt(dx * dx + dz * dz) * 0.381;
        int baseY = slopedBase > ceiling ? ceiling : (int) slopedBase;
        if (!LimbLine.draw(growth, x, baseY, z, endX, endY, endZ, false)) {
            return null;
        }
        return new Bough(endX, endY, endZ, baseY);
    }

    private static float crownProfile(int height, int y) {
        if (y < height * 0.3F) {
            return -1.0F;
        }
        float half = height / 2.0F;
        float fromMiddle = half - y;
        if (Math.abs(fromMiddle) >= half && fromMiddle != 0) {
            return 0.0F;
        }
        float radius = fromMiddle == 0 ? half : (float) Math.sqrt(half * half - fromMiddle * fromMiddle);
        return radius * 0.5F;
    }
}
