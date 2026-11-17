package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.feature.choice.BlockChoice;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

public record FallenLogFeature(BlockChoice wood, IntSampler length, boolean stumpVines, List<Topping> toppings) implements Feature {

    private static final BlockState VINE = BlockStates.of("vine");
    private static final int MAX_DROP = 6;
    private static final int MAX_GAP = 2;

    public record Topping(float chance, BlockChoice block) {
    }

    public static FallenLogFeature read(JsonObject config) {
        boolean vines = false;
        if (config.has("stump_decorators")) {
            for (JsonElement element : config.getAsJsonArray("stump_decorators")) {
                vines |= WorldgenData.type(element.getAsJsonObject()).equals("trunk_vine");
            }
        }
        List<Topping> toppings = new ArrayList<>();
        if (config.has("log_decorators")) {
            for (JsonElement element : config.getAsJsonArray("log_decorators")) {
                JsonObject decorator = element.getAsJsonObject();
                if (WorldgenData.type(decorator).equals("attached_to_logs")) {
                    toppings.add(new Topping(decorator.get("probability").getAsFloat(), BlockChoice.read(decorator.get("block_provider"))));
                }
            }
        }
        return new FallenLogFeature(BlockChoice.read(config.get("trunk_provider")), IntSampler.read(config.get("log_length")), vines,
                List.copyOf(toppings));
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        DecorationArea area = pass.area;
        BlockState stump = wood.choose(random, area, x, y, z);
        if (stump == null) {
            return false;
        }
        area.set(x, y, z, stump);
        if (stumpVines) {
            hangVines(area, random, x, y, z);
        }
        WallFace direction = WallFace.values()[random.nextInt(4)];
        int logLength = length.sample(random) - 2;
        int distance = 2 + random.nextInt(2);
        int startX = x + direction.dx * distance;
        int startZ = z + direction.dz * distance;
        int startY = y + 1;
        for (int i = 0; i < MAX_DROP && !restsHere(area, startX, startY, startZ); i++) {
            startY--;
        }
        if (fits(area, logLength, startX, startY, startZ, direction)) {
            layLog(area, random, logLength, startX, startY, startZ, direction);
        }
        return true;
    }

    private static void hangVines(DecorationArea area, Xoroshiro random, int x, int y, int z) {
        for (WallFace face : WallFace.values()) {
            int vx = x + face.dx;
            int vz = z + face.dz;
            if (random.nextInt(3) > 0 && area.isAir(vx, y, vz)) {
                area.set(vx, y, vz, VINE.with(face.opposite().property, "true"));
            }
        }
    }

    private void layLog(DecorationArea area, Xoroshiro random, int logLength, int x, int y, int z, WallFace direction) {
        List<int[]> logs = new ArrayList<>();
        for (int i = 0; i < logLength; i++) {
            int px = x + direction.dx * i;
            int pz = z + direction.dz * i;
            BlockState log = wood.choose(random, area, px, y, pz);
            if (log != null) {
                area.set(px, y, pz, log.with("axis", direction.axis()));
                logs.add(new int[]{px, y, pz});
            }
        }
        for (Topping topping : toppings) {
            for (int[] log : logs) {
                if (random.nextFloat() <= topping.chance && area.isAir(log[0], log[1] + 1, log[2])) {
                    BlockState state = topping.block.choose(random, area, log[0], log[1] + 1, log[2]);
                    if (state != null) {
                        area.set(log[0], log[1] + 1, log[2], state);
                    }
                }
            }
        }
    }

    private static boolean restsHere(DecorationArea area, int x, int y, int z) {
        BlockState state = area.get(x, y, z);
        return (state.air || state.replaceable) && area.get(x, y - 1, z).blocksMotion;
    }

    private static boolean fits(DecorationArea area, int logLength, int x, int y, int z, WallFace direction) {
        int unsupported = 0;
        for (int i = 0; i < logLength; i++) {
            int px = x + direction.dx * i;
            int pz = z + direction.dz * i;
            BlockState state = area.get(px, y, pz);
            if (!state.air && !state.is("minecraft:replaceable_by_trees")) {
                return false;
            }
            unsupported = area.get(px, y - 1, pz).blocksMotion ? 0 : unsupported + 1;
            if (unsupported > MAX_GAP) {
                return false;
            }
        }
        return true;
    }
}
