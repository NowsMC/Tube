package space.nows.tube.server.mob.kind.monster.silverfish;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;

import java.util.HashMap;
import java.util.Map;

public final class Infestation {

    private static final Map<Block, Block> INFESTED_BY_HOST = new HashMap<>();
    private static final Map<Block, Block> HOST_BY_INFESTED = new HashMap<>();

    static {
        pair(Block.STONE, Block.INFESTED_STONE);
        pair(Block.COBBLESTONE, Block.INFESTED_COBBLESTONE);
        pair(Block.STONE_BRICKS, Block.INFESTED_STONE_BRICKS);
        pair(Block.MOSSY_STONE_BRICKS, Block.INFESTED_MOSSY_STONE_BRICKS);
        pair(Block.CRACKED_STONE_BRICKS, Block.INFESTED_CRACKED_STONE_BRICKS);
        pair(Block.CHISELED_STONE_BRICKS, Block.INFESTED_CHISELED_STONE_BRICKS);
        pair(Block.DEEPSLATE, Block.INFESTED_DEEPSLATE);
    }

    private Infestation() {
    }

    private static void pair(Block host, Block infested) {
        INFESTED_BY_HOST.put(host, infested);
        HOST_BY_INFESTED.put(infested, host);
    }

    public static boolean canHost(Block block) {
        return INFESTED_BY_HOST.containsKey(block.defaultState());
    }

    public static boolean infested(Block block) {
        return HOST_BY_INFESTED.containsKey(block.defaultState());
    }

    public static Block infest(Block host) {
        return INFESTED_BY_HOST.get(host.defaultState()).withProperties(host.properties());
    }

    public static Block restore(Block infested) {
        return HOST_BY_INFESTED.get(infested.defaultState()).withProperties(infested.properties());
    }

    public static void release(Instance instance, Point at) {
        Creature silverfish = new Creature(CreatureKinds.of(EntityType.SILVERFISH).orElseThrow());
        CreatureKinds.spawn(silverfish, instance, new Pos(at.blockX() + 0.5, at.blockY(), at.blockZ() + 0.5));
    }
}
