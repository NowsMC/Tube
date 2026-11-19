package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

final class FreezeWhenStared extends Behavior {

    private static final double STARE_RANGE_SQUARED = 256.0;

    private final Creature enderman;
    private LivingEntity target;

    FreezeWhenStared(Creature enderman) {
        super(Control.JUMP, Control.MOVE);
        this.enderman = enderman;
    }

    @Override
    public boolean canStart() {
        target = enderman.target();
        return target instanceof Player player
                && player.getPosition().distanceSquared(enderman.getPosition()) <= STARE_RANGE_SQUARED
                && Gazing.staredAtBy(enderman, player);
    }

    @Override
    public void start() {
        enderman.navigation().stop();
    }

    @Override
    public void tick() {
        enderman.gaze().lookAt(target);
    }
}
