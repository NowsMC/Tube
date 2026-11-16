package space.nows.tube.server.world;

import ca.spottedleaf.concurrentutil.map.concurrent.ints.ConcurrentChainedInt2ReferenceHashTable;
import ca.spottedleaf.concurrentutil.map.concurrent.objects.ConcurrentChainedObject2ReferenceHashTable;
import space.nows.tube.api.world.Block;

public final class BlockMapper {

    private static final ConcurrentChainedInt2ReferenceHashTable<Block> FROM_MINESTOM = ConcurrentChainedInt2ReferenceHashTable.createWithExpected(4096);
    private static final ConcurrentChainedObject2ReferenceHashTable<Block, net.minestom.server.instance.block.Block> TO_MINESTOM =
            ConcurrentChainedObject2ReferenceHashTable.createWithExpected(4096);

    private BlockMapper() {
    }

    public static Block fromMinestom(net.minestom.server.instance.block.Block block) {
        int state = block.stateId();
        Block cached = FROM_MINESTOM.get(state);
        if (cached == null) {
            cached = new Block(block.key().asString(), block.properties());
            FROM_MINESTOM.putIfAbsent(state, cached);
        }
        return cached;
    }

    public static net.minestom.server.instance.block.Block toMinestom(Block block) {
        net.minestom.server.instance.block.Block cached = TO_MINESTOM.get(block);
        if (cached == null) {
            cached = resolve(block);
            TO_MINESTOM.putIfAbsent(block, cached);
        }
        return cached;
    }

    private static net.minestom.server.instance.block.Block resolve(Block block) {
        net.minestom.server.instance.block.Block resolved = net.minestom.server.instance.block.Block.fromKey(block.key());
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown block: " + block.key());
        }
        return block.properties().isEmpty() ? resolved : resolved.withProperties(block.properties());
    }
}
