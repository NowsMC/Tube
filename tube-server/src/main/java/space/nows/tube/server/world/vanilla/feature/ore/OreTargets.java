package space.nows.tube.server.world.vanilla.feature.ore;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class OreTargets {

    private interface Host {
        boolean accepts(BlockState state, Xoroshiro random);
    }

    private record Target(Host host, BlockState ore) {
    }

    private static final int[][] NEIGHBOURS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    private final List<Target> targets = new ArrayList<>();
    private final float hiddenOnly;

    OreTargets(JsonObject config) {
        this.hiddenOnly = config.has("discard_chance_on_air_exposure") ? config.get("discard_chance_on_air_exposure").getAsFloat() : 0;
        for (JsonElement element : config.getAsJsonArray("targets")) {
            JsonObject target = element.getAsJsonObject();
            targets.add(new Target(host(target.getAsJsonObject("target")), BlockStates.of(target.getAsJsonObject("state"))));
        }
    }

    private static Host host(JsonObject json) {
        String type = WorldgenData.path(json.get("predicate_type").getAsString());
        return switch (type) {
            case "always_true" -> (state, random) -> true;
            case "tag_match" -> {
                String tag = WorldgenData.key(json.get("tag").getAsString());
                yield (state, random) -> state.is(tag);
            }
            case "block_match" -> {
                String block = WorldgenData.key(json.get("block").getAsString());
                yield (state, random) -> state.isBlock(block);
            }
            case "blockstate_match" -> {
                BlockState match = BlockStates.of(json.getAsJsonObject("block_state"));
                yield (state, random) -> state == match;
            }
            case "random_block_match" -> {
                String block = WorldgenData.key(json.get("block").getAsString());
                float chance = json.get("probability").getAsFloat();
                yield (state, random) -> state.isBlock(block) && random.nextFloat() < chance;
            }
            default -> throw new IllegalArgumentException("Unsupported rule test " + type);
        };
    }

    boolean replace(DecorationArea area, Xoroshiro random, int x, int y, int z) {
        BlockState current = area.get(x, y, z);
        for (Target target : targets) {
            if (target.host.accepts(current, random) && (ignoresExposure(random) || !exposed(area, x, y, z))) {
                area.set(x, y, z, target.ore);
                return true;
            }
        }
        return false;
    }

    private boolean ignoresExposure(Xoroshiro random) {
        if (hiddenOnly <= 0) {
            return true;
        }
        return hiddenOnly < 1 && random.nextFloat() >= hiddenOnly;
    }

    private static boolean exposed(DecorationArea area, int x, int y, int z) {
        for (int[] offset : NEIGHBOURS) {
            if (area.isAir(x + offset[0], y + offset[1], z + offset[2])) {
                return true;
            }
        }
        return false;
    }
}
