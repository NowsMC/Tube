package space.nows.tube.server.world.vanilla.carver;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;
import space.nows.tube.server.world.vanilla.terrain.GroundWater;

import java.util.BitSet;

final class CarveVolume {

    private static final int TOP_MARGIN = 7;

    @FunctionalInterface
    interface Shape {
        boolean outside(double dx, double dy, double dz, int y);
    }

    private final ChunkDraft draft;
    private final GroundWater water;
    private final BitSet carved;

    CarveVolume(ChunkDraft draft, GroundWater water) {
        this.draft = draft;
        this.water = water;
        this.carved = new BitSet(16 * 16 * draft.height);
    }

    int minY() {
        return draft.minY;
    }

    int height() {
        return draft.height;
    }

    boolean reachable(double x, double z, int step, int length, float thickness) {
        double dx = x - (draft.minX() + 8);
        double dz = z - (draft.minZ() + 8);
        double remaining = length - step;
        double reach = thickness + 2.0F + 16.0F;
        return dx * dx + dz * dz - remaining * remaining <= reach * reach;
    }

    void carveEllipsoid(CarverSettings settings, double x, double y, double z, double radius, double radiusY, Shape shape) {
        int minX = draft.minX();
        int minZ = draft.minZ();
        double reach = 16.0 + radius * 2.0;
        if (Math.abs(x - (minX + 8)) > reach || Math.abs(z - (minZ + 8)) > reach) {
            return;
        }
        int fromX = Math.max((int) Math.floor(x - radius) - minX - 1, 0);
        int toX = Math.min((int) Math.floor(x + radius) - minX, 15);
        int fromY = Math.max((int) Math.floor(y - radiusY) - 1, draft.minY + 1);
        int toY = Math.min((int) Math.floor(y + radiusY) + 1, draft.maxY() - 1 - TOP_MARGIN);
        int fromZ = Math.max((int) Math.floor(z - radius) - minZ - 1, 0);
        int toZ = Math.min((int) Math.floor(z + radius) - minZ, 15);
        for (int localX = fromX; localX <= toX; localX++) {
            double dx = (minX + localX + 0.5 - x) / radius;
            for (int localZ = fromZ; localZ <= toZ; localZ++) {
                double dz = (minZ + localZ + 0.5 - z) / radius;
                if (dx * dx + dz * dz < 1.0) {
                    carveColumn(settings, localX, localZ, fromY, toY, y, radiusY, dx, dz, shape);
                }
            }
        }
    }

    private void carveColumn(CarverSettings settings, int localX, int localZ, int fromY, int toY, double centerY, double radiusY,
                             double dx, double dz, Shape shape) {
        BlockState topsoil = null;
        for (int y = toY; y > fromY; y--) {
            double dy = (y - 0.5 - centerY) / radiusY;
            if (shape.outside(dx, dy, dz, y) || !claim(localX, y, localZ)) {
                continue;
            }
            BlockState state = draft.get(localX, y, localZ);
            if (state.isBlock("minecraft:grass_block") || state.isBlock("minecraft:mycelium")) {
                topsoil = state;
            }
            if (!state.is(settings.carvable())) {
                continue;
            }
            BlockState opening = y <= settings.lavaBelow() ? BlockStates.LAVA
                    : water.fill(draft.minX() + localX, y, draft.minZ() + localZ, 0.0);
            if (opening == null) {
                continue;
            }
            draft.set(localX, y, localZ, opening);
            if (topsoil != null && !opening.hasFluid() && draft.get(localX, y - 1, localZ).isBlock("minecraft:dirt")) {
                draft.set(localX, y - 1, localZ, topsoil);
            }
        }
    }

    private boolean claim(int localX, int y, int localZ) {
        int bit = (y - draft.minY) << 8 | localZ << 4 | localX;
        if (carved.get(bit)) {
            return false;
        }
        carved.set(bit);
        return true;
    }
}
