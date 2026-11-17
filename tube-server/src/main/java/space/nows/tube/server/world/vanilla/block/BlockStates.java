package space.nows.tube.server.world.vanilla.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.api.world.Block;
import space.nows.tube.server.world.BlockMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockStates {

    private static final Map<Block, BlockState> STATES = new ConcurrentHashMap<>();
    private static final List<BlockState> REGISTERED = new ArrayList<>();
    private static volatile BlockState[] byId = new BlockState[0];

    public static final BlockState AIR = of("air");
    public static final BlockState WATER = of("water");
    public static final BlockState LAVA = of("lava");

    private BlockStates() {
    }

    public static BlockState of(String key) {
        return of(Block.of(key));
    }

    public static BlockState of(JsonObject json) {
        Map<String, String> properties = new HashMap<>();
        JsonObject list = json.getAsJsonObject("Properties");
        if (list != null) {
            for (Map.Entry<String, JsonElement> entry : list.entrySet()) {
                properties.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return of(new Block(json.get("Name").getAsString(), properties));
    }

    public static BlockState of(Block block) {
        BlockState state = STATES.get(block);
        return state != null ? state : register(block);
    }

    public static BlockState byId(int id) {
        return byId[id];
    }

    private static BlockState register(Block block) {
        net.minestom.server.instance.block.Block minestom = BlockMapper.toMinestom(block);
        Block canonical = BlockMapper.fromMinestom(minestom);
        synchronized (REGISTERED) {
            BlockState state = STATES.get(canonical);
            if (state == null) {
                var registry = minestom.registry();
                state = new BlockState(REGISTERED.size(), canonical, registry.blocksMotion(), registry.isSolid(), registry.isReplaceable());
                REGISTERED.add(state);
                byId = REGISTERED.toArray(BlockState[]::new);
                STATES.put(canonical, state);
            }
            STATES.putIfAbsent(block, state);
            return state;
        }
    }
}
