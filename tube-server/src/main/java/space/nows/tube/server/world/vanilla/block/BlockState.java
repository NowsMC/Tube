package space.nows.tube.server.world.vanilla.block;

import space.nows.tube.api.world.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BlockState {

    final int id;
    public final Block block;
    public final String key;
    public final boolean air;
    public final boolean water;
    public final boolean lava;
    public final boolean blocksMotion;
    public final boolean solid;
    public final boolean replaceable;
    public final boolean leaves;
    private final Set<String> tags;

    BlockState(int id, Block block, boolean blocksMotion, boolean solid, boolean replaceable) {
        this.id = id;
        this.block = block;
        this.key = block.key();
        this.tags = BlockTags.of(key);
        this.air = block.isAir();
        this.water = key.equals("minecraft:water") || "true".equals(block.properties().get("waterlogged"));
        this.lava = key.equals("minecraft:lava");
        this.blocksMotion = blocksMotion;
        this.solid = solid;
        this.replaceable = replaceable;
        this.leaves = tags.contains("minecraft:leaves");
    }

    public int id() {
        return id;
    }

    public boolean is(String tag) {
        return tags.contains(tag);
    }

    public boolean isBlock(String blockKey) {
        return key.equals(blockKey);
    }

    public boolean hasFluid() {
        return water || lava;
    }

    public boolean isSource() {
        boolean fluidBlock = key.equals("minecraft:water") || lava;
        return fluidBlock && "0".equals(block.properties().getOrDefault("level", "0")) || "true".equals(block.properties().get("waterlogged"));
    }

    public String property(String name) {
        return block.properties().get(name);
    }

    public boolean hasProperty(String name) {
        return block.properties().containsKey(name);
    }

    public BlockState with(String name, String value) {
        if (!block.properties().containsKey(name) || value.equals(block.properties().get(name))) {
            return this;
        }
        Map<String, String> properties = new HashMap<>(block.properties());
        properties.put(name, value);
        return BlockStates.of(new Block(key, properties));
    }

    @Override
    public String toString() {
        return block.properties().isEmpty() ? key : key + block.properties();
    }
}
