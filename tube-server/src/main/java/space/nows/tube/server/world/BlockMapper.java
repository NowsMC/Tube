package space.nows.tube.server.world;

import space.nows.tube.api.Block;

public final class BlockMapper {

    private BlockMapper() {
    }

    public static Block fromMinestom(net.minestom.server.instance.block.Block block) {
        return new Block(block.key().asString(), block.properties());
    }

    public static net.minestom.server.instance.block.Block toMinestom(Block block) {
        net.minestom.server.instance.block.Block resolved = net.minestom.server.instance.block.Block.fromKey(block.key());
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown block: " + block.key());
        }
        return block.properties().isEmpty() ? resolved : resolved.withProperties(block.properties());
    }
}
