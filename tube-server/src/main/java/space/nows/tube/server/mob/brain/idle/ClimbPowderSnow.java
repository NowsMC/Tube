package space.nows.tube.server.mob.brain.idle;

import net.minestom.server.instance.block.Block;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

public final class ClimbPowderSnow extends Behavior {

    private final Creature creature;

    public ClimbPowderSnow(Creature creature) {
        super(Control.JUMP);
        this.creature = creature;
    }

    @Override
    public boolean canStart() {
        if (!creature.blockAt(creature.getPosition()).compare(Block.POWDER_SNOW)) {
            return false;
        }
        Block above = creature.blockAt(creature.getPosition().add(0, 1, 0));
        return above.compare(Block.POWDER_SNOW) || !above.solid();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        creature.jump();
    }
}
