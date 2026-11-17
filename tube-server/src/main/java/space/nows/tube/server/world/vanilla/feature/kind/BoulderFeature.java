package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.test.BlockTest;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public record BoulderFeature(BlockState block, BlockTest restsOn) implements Feature {

    private static final int LUMPS = 3;

    public static BoulderFeature read(JsonObject config) {
        BlockTest restsOn = config.has("can_place_on") ? BlockTest.read(config.get("can_place_on"))
                : (area, x, y, z) -> area.get(x, y, z).is("minecraft:dirt") || area.get(x, y, z).is("minecraft:base_stone_overworld");
        return new BoulderFeature(BlockStates.of(config.getAsJsonObject("state")), restsOn);
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        int groundY = y;
        while (groundY > area.minY + 3 && (area.isAir(x, groundY - 1, z) || !restsOn.test(area, x, groundY - 1, z))) {
            groundY--;
        }
        if (groundY <= area.minY + 3) {
            return false;
        }
        int cx = x;
        int cy = groundY;
        int cz = z;
        for (int lump = 0; lump < LUMPS; lump++) {
            int rx = random.nextInt(2);
            int ry = random.nextInt(2);
            int rz = random.nextInt(2);
            float radius = (rx + ry + rz) * 0.333F + 0.5F;
            fillLump(area, cx, cy, cz, rx, ry, rz, radius);
            cx += -1 + random.nextInt(2);
            cy -= random.nextInt(2);
            cz += -1 + random.nextInt(2);
        }
        return true;
    }

    private void fillLump(DecorationArea area, int cx, int cy, int cz, int rx, int ry, int rz, float radius) {
        for (int bx = cx - rx; bx <= cx + rx; bx++) {
            for (int by = cy - ry; by <= cy + ry; by++) {
                for (int bz = cz - rz; bz <= cz + rz; bz++) {
                    int dx = bx - cx;
                    int dy = by - cy;
                    int dz = bz - cz;
                    if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                        area.set(bx, by, bz, block);
                    }
                }
            }
        }
    }
}
