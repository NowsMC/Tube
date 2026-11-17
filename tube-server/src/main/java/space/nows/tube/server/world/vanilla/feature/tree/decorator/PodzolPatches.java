package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;

record PodzolPatches(BlockChoice ground) implements TreeDecorator {

    static PodzolPatches read(JsonObject json) {
        return new PodzolPatches(BlockChoice.read(json.get("provider")));
    }

    @Override
    public void decorate(TreeGrowth growth) {
        if (growth.logs().isEmpty()) {
            return;
        }
        int bottom = growth.logs().stream().mapToInt(log -> log[1]).min().orElseThrow();
        for (int[] log : growth.logs()) {
            if (log[1] != bottom) {
                continue;
            }
            int x = log[0];
            int y = log[1];
            int z = log[2];
            patch(growth, x - 1, y, z - 1);
            patch(growth, x + 2, y, z - 1);
            patch(growth, x - 1, y, z + 2);
            patch(growth, x + 2, y, z + 2);
            for (int i = 0; i < 5; i++) {
                int pick = growth.random.nextInt(64);
                int px = pick % 8;
                int pz = pick / 8;
                if (px == 0 || px == 7 || pz == 0 || pz == 7) {
                    patch(growth, x - 3 + px, y, z - 3 + pz);
                }
            }
        }
    }

    private void patch(TreeGrowth growth, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) != 2 || Math.abs(dz) != 2) {
                    resurface(growth, x + dx, y, z + dz);
                }
            }
        }
    }

    private void resurface(TreeGrowth growth, int x, int y, int z) {
        DecorationArea area = growth.area;
        for (int dy = 2; dy >= -3; dy--) {
            BlockState state = area.get(x, y + dy, z);
            if (isSoil(state)) {
                BlockState replacement = ground.choose(growth.random, area, x, y + dy, z);
                if (replacement != null) {
                    area.set(x, y + dy, z, replacement);
                }
                return;
            }
            if (!state.air && dy < 0) {
                return;
            }
        }
    }

    private static boolean isSoil(BlockState state) {
        return state.is("minecraft:dirt") || state.isBlock("minecraft:grass_block") || state.isBlock("minecraft:podzol")
                || state.isBlock("minecraft:mycelium");
    }
}
