package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;

final class LimbLine {

    private LimbLine() {
    }

    static boolean draw(TreeGrowth growth, int fromX, int fromY, int fromZ, int toX, int toY, int toZ, boolean place) {
        if (!place && fromX == toX && fromY == toY && fromZ == toZ) {
            return true;
        }
        int dx = toX - fromX;
        int dy = toY - fromY;
        int dz = toZ - fromZ;
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (steps == 0) {
            if (place) {
                growth.log(fromX, fromY, fromZ, "y");
            }
            return true;
        }
        float stepX = (float) dx / steps;
        float stepY = (float) dy / steps;
        float stepZ = (float) dz / steps;
        for (int i = 0; i <= steps; i++) {
            int x = fromX + (int) Math.floor(0.5F + i * stepX);
            int y = fromY + (int) Math.floor(0.5F + i * stepY);
            int z = fromZ + (int) Math.floor(0.5F + i * stepZ);
            if (place) {
                growth.log(x, y, z, axisFrom(x - fromX, z - fromZ));
            } else if (!growth.isFree(x, y, z)) {
                return false;
            }
        }
        return true;
    }

    private static String axisFrom(int dx, int dz) {
        int along = Math.max(Math.abs(dx), Math.abs(dz));
        if (along == 0) {
            return "y";
        }
        return Math.abs(dx) == along ? "x" : "z";
    }
}
