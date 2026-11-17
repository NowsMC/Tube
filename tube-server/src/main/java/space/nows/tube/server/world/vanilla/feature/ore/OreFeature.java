package space.nows.tube.server.world.vanilla.feature.ore;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.Heightmap;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.BitSet;

public final class OreFeature implements Feature {

    private record Box(int minX, int minY, int minZ, int width, int height) {

        int bit(int x, int y, int z) {
            return x - minX + (y - minY) * width + (z - minZ) * width * height;
        }
    }

    private final int size;
    private final OreTargets targets;

    public OreFeature(JsonObject config) {
        this.size = config.get("size").getAsInt();
        this.targets = new OreTargets(config);
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        float angle = random.nextFloat() * (float) Math.PI;
        float halfLength = size / 8.0F;
        int padding = (int) Math.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
        double x1 = x + Math.sin(angle) * halfLength;
        double x2 = x - Math.sin(angle) * halfLength;
        double z1 = z + Math.cos(angle) * halfLength;
        double z2 = z - Math.cos(angle) * halfLength;
        double y1 = y + random.nextInt(3) - 2;
        double y2 = y + random.nextInt(3) - 2;
        int reach = (int) Math.ceil(halfLength) + padding;
        Box box = new Box(x - reach, y - 2 - padding, z - reach, 2 * reach, 2 * (2 + padding));
        if (!belowSurfaceSomewhere(area, box)) {
            return false;
        }
        int placed = 0;
        BitSet visited = new BitSet(box.width * box.height * box.width);
        for (OreStreak.Ball ball : OreStreak.along(random, size, x1, y1, z1, x2, y2, z2)) {
            placed += fill(area, random, box, visited, ball);
        }
        return placed > 0;
    }

    private static boolean belowSurfaceSomewhere(DecorationArea area, Box box) {
        for (int x = box.minX; x <= box.minX + box.width; x++) {
            for (int z = box.minZ; z <= box.minZ + box.width; z++) {
                if (box.minY <= area.height(Heightmap.SOLID, x, z)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int fill(DecorationArea area, Xoroshiro random, Box box, BitSet visited, OreStreak.Ball ball) {
        double r = ball.radius();
        int fromX = Math.max((int) Math.floor(ball.x() - r), box.minX);
        int fromY = Math.max((int) Math.floor(ball.y() - r), box.minY);
        int fromZ = Math.max((int) Math.floor(ball.z() - r), box.minZ);
        int toX = Math.max((int) Math.floor(ball.x() + r), fromX);
        int toY = Math.max((int) Math.floor(ball.y() + r), fromY);
        int toZ = Math.max((int) Math.floor(ball.z() + r), fromZ);
        int placed = 0;
        for (int bx = fromX; bx <= toX; bx++) {
            double ex = (bx + 0.5 - ball.x()) / r;
            if (ex * ex >= 1) {
                continue;
            }
            for (int by = fromY; by <= toY; by++) {
                double ey = (by + 0.5 - ball.y()) / r;
                if (ex * ex + ey * ey >= 1) {
                    continue;
                }
                for (int bz = fromZ; bz <= toZ; bz++) {
                    double ez = (bz + 0.5 - ball.z()) / r;
                    if (ex * ex + ey * ey + ez * ez >= 1 || area.outsideHeight(by)) {
                        continue;
                    }
                    int bit = box.bit(bx, by, bz);
                    if (bit < 0 || visited.get(bit)) {
                        continue;
                    }
                    visited.set(bit);
                    if (targets.replace(area, random, bx, by, bz)) {
                        placed++;
                    }
                }
            }
        }
        return placed;
    }
}
