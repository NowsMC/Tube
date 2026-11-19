package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.effect.BlockBreakEffect;

import java.util.Set;

public final class Graze extends Behavior {

    private static final int ADULT_CHANCE = 1000;
    private static final int BABY_CHANCE = 50;
    private static final int EAT_TICKS = 40;
    private static final int BITE_AT = 4;
    private static final byte EAT_ANIMATION = 10;
    private static final Set<Block> EDIBLE = Set.of(Block.SHORT_GRASS, Block.SHORT_DRY_GRASS, Block.TALL_DRY_GRASS, Block.FERN);

    private final Creature creature;
    private final Runnable onEaten;
    private int remaining;

    public Graze(Creature creature, Runnable onEaten) {
        super(Control.MOVE, Control.LOOK, Control.JUMP);
        this.creature = creature;
        this.onEaten = onEaten;
    }

    @Override
    public boolean canStart() {
        if (creature.random().nextInt(ticks(creature.isBaby() ? BABY_CHANCE : ADULT_CHANCE)) != 0) {
            return false;
        }
        Instance instance = creature.getInstance();
        Point at = creature.getPosition();
        return edible(instance.getBlock(at)) || instance.getBlock(at.sub(0, 1, 0)).compare(Block.GRASS_BLOCK);
    }

    @Override
    public void start() {
        remaining = ticks(EAT_TICKS);
        creature.triggerStatus(EAT_ANIMATION);
        creature.navigation().stop();
    }

    @Override
    public void stop() {
        remaining = 0;
    }

    @Override
    public boolean canContinue() {
        return remaining > 0;
    }

    @Override
    public void tick() {
        remaining = Math.max(0, remaining - 1);
        if (remaining != ticks(BITE_AT)) {
            return;
        }
        Instance instance = creature.getInstance();
        Point at = creature.getPosition();
        if (edible(instance.getBlock(at))) {
            BlockBreakEffect.play(instance, at, instance.getBlock(at));
            instance.setBlock(at, Block.AIR);
            onEaten.run();
            return;
        }
        Point below = at.sub(0, 1, 0);
        if (instance.getBlock(below).compare(Block.GRASS_BLOCK)) {
            BlockBreakEffect.play(instance, below, Block.GRASS_BLOCK);
            instance.setBlock(below, Block.DIRT);
            onEaten.run();
        }
    }

    private static boolean edible(Block block) {
        for (Block food : EDIBLE) {
            if (block.compare(food)) {
                return true;
            }
        }
        return false;
    }
}
