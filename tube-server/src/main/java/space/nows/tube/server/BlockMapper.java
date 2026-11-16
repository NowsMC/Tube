package space.nows.tube.server;

import space.nows.tube.api.Block;

final class BlockMapper {

    private BlockMapper() {
    }

    static Block fromMinestom(net.minestom.server.instance.block.Block block) {
        return new Block(block.key().asString(), block.properties());
    }

    static net.minestom.server.instance.block.Block toMinestom(Block block) {
        net.minestom.server.instance.block.Block resolved = net.minestom.server.instance.block.Block.fromKey(block.key());
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown block: " + block.key());
        }
        return block.properties().isEmpty() ? resolved : resolved.withProperties(block.properties());
    }
}
