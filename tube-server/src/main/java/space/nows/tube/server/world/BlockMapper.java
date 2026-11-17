package space.nows.tube.server.world;

import space.nows.tube.api.world.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;

public final class BlockMapper {

    private static final AtomicReferenceArray<Block> FROM_MINESTOM = new AtomicReferenceArray<>(stateCount());
    private static final Map<Block, net.minestom.server.instance.block.Block> TO_MINESTOM = new ConcurrentHashMap<>(4096);

    private BlockMapper() {
    }

    public static Block fromMinestom(net.minestom.server.instance.block.Block block) {
        int state = block.stateId();
        Block cached = FROM_MINESTOM.get(state);
        if (cached == null) {
            cached = new Block(block.key().asString(), block.properties());
            FROM_MINESTOM.compareAndSet(state, null, cached);
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

    private static int stateCount() {
        int highest = 0;
        for (net.minestom.server.instance.block.Block block : net.minestom.server.instance.block.Block.values()) {
            for (net.minestom.server.instance.block.Block state : block.possibleStates()) {
                highest = Math.max(highest, state.stateId());
            }
        }
        return highest + 1;
    }

    private static net.minestom.server.instance.block.Block resolve(Block block) {
        net.minestom.server.instance.block.Block resolved = net.minestom.server.instance.block.Block.fromKey(block.key());
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown block: " + block.key());
        }
        return block.properties().isEmpty() ? resolved : resolved.withProperties(block.properties());
    }
}
