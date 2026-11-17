package space.nows.tube.server.world.vanilla.feature.tree.canopy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.feature.sample.IntSampler;
import space.nows.tube.server.world.vanilla.feature.tree.Attachment;
import space.nows.tube.server.world.vanilla.feature.tree.TreeGrowth;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Canopy {

    private static final Map<String, Function<JsonObject, Canopy>> SHAPES = new HashMap<>();

    static {
        SHAPES.put("blob_foliage_placer", BlobCanopy::new);
        SHAPES.put("fancy_foliage_placer", RoundCanopy::new);
        SHAPES.put("bush_foliage_placer", BushCanopy::new);
        SHAPES.put("spruce_foliage_placer", SpruceCanopy::new);
        SHAPES.put("pine_foliage_placer", PineCanopy::new);
        SHAPES.put("mega_pine_foliage_placer", TallPineCanopy::new);
        SHAPES.put("acacia_foliage_placer", FlatCanopy::new);
        SHAPES.put("dark_oak_foliage_placer", DarkOakCanopy::new);
    }

    protected final IntSampler radius;
    protected final IntSampler offset;
    protected final IntSampler height;

    protected Canopy(JsonObject json) {
        this.radius = IntSampler.read(json.get("radius"));
        this.offset = IntSampler.read(json.get("offset"));
        JsonElement heightJson = json.has("height") ? json.get("height") : json.has("crown_height") ? json.get("crown_height")
                : json.get("foliage_height");
        this.height = heightJson == null ? IntSampler.fixed(0) : IntSampler.read(heightJson);
    }

    public static Canopy read(JsonObject json) {
        return SHAPES.getOrDefault(WorldgenData.type(json), BlobCanopy::new).apply(json);
    }

    public int crownHeight(Xoroshiro random, int treeHeight) {
        return height.sample(random);
    }

    public int crownRadius(Xoroshiro random, int trunkLength) {
        return radius.sample(random);
    }

    public final void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius) {
        grow(growth, random, at, crownHeight, crownRadius, offset.sample(random));
    }

    protected abstract void grow(TreeGrowth growth, Xoroshiro random, Attachment at, int crownHeight, int crownRadius, int lift);

    protected abstract boolean leaveOut(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide);

    protected boolean leaveOutSigned(Xoroshiro random, int dx, int y, int dz, int radius, boolean wide) {
        int ax = wide ? Math.min(Math.abs(dx), Math.abs(dx - 1)) : Math.abs(dx);
        int az = wide ? Math.min(Math.abs(dz), Math.abs(dz - 1)) : Math.abs(dz);
        return leaveOut(random, ax, y, az, radius, wide);
    }

    protected final void layer(TreeGrowth growth, Xoroshiro random, int x, int y, int z, int radius, int dy, boolean wide) {
        int extra = wide ? 1 : 0;
        for (int dx = -radius; dx <= radius + extra; dx++) {
            for (int dz = -radius; dz <= radius + extra; dz++) {
                if (!leaveOutSigned(random, dx, dy, dz, radius, wide)) {
                    growth.leaf(x + dx, y + dy, z + dz);
                }
            }
        }
    }

    protected static boolean corner(int dx, int dz, int radius) {
        return dx == radius && dz == radius;
    }
}
