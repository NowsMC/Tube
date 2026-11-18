package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.coordinate.Vec;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Flutter implements Trait {

    private static final double FALL_DAMPING = 0.6;

    private final Creature creature;

    Flutter(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void tick() {
        Vec velocity = creature.getVelocity();
        if (!creature.isOnGround() && velocity.y() < 0) {
            creature.setVelocity(velocity.withY(velocity.y() * FALL_DAMPING));
        }
    }
}
