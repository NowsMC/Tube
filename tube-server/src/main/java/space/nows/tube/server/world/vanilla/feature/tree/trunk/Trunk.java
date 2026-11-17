package space.nows.tube.server.world.vanilla.feature.tree.trunk;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Trunk {

    private static final Map<String, Function<JsonObject, Trunk>> SHAPES = new HashMap<>();

    static {
        SHAPES.put("straight_trunk_placer", StraightTrunk::new);
        SHAPES.put("forking_trunk_placer", ForkingTrunk::new);
        SHAPES.put("fancy_trunk_placer", BranchingTrunk::new);
        SHAPES.put("giant_trunk_placer", WideTrunk::new);
        SHAPES.put("mega_jungle_trunk_placer", WideBranchingTrunk::new);
    }

    private final int baseHeight;
    private final int extraA;
    private final int extraB;

    protected Trunk(JsonObject json) {
        this.baseHeight = json.get("base_height").getAsInt();
        this.extraA = json.get("height_rand_a").getAsInt();
        this.extraB = json.get("height_rand_b").getAsInt();
    }

    public static Trunk read(JsonObject json) {
        return SHAPES.getOrDefault(WorldgenData.type(json), StraightTrunk::new).apply(json);
    }

    public int height(Xoroshiro random) {
        return baseHeight + random.nextInt(extraA + 1) + random.nextInt(extraB + 1);
    }

    public abstract List<Attachment> grow(TreeGrowth growth, Xoroshiro random, int height, int x, int y, int z);

    static final int[][] HORIZONTAL = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

    static int[] randomDirection(Xoroshiro random) {
        return HORIZONTAL[random.nextInt(HORIZONTAL.length)];
    }

    static void soilUnderSquare(TreeGrowth growth, int x, int y, int z) {
        growth.soil(x, y - 1, z);
        growth.soil(x + 1, y - 1, z);
        growth.soil(x, y - 1, z + 1);
        growth.soil(x + 1, y - 1, z + 1);
    }
}
