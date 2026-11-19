package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;

@FunctionalInterface
public interface Volley {

    void fire(Creature shooter, LivingEntity target, float power);
}
