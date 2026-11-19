package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.life.Lifecycle;

public final class Panda extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(20, 0.15).withDamage(6);

    @Override
    public EntityType type() {
        return EntityType.PANDA;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(0, new StayAfloat(creature));
        brain.add(2, new Panic(creature, 2.0));
        brain.add(2, new Breed(creature, life, 1.0));
        brain.add(4, new Tempted(creature, 1.0, breedingFood()));
        brain.add(6, new AvoidNearby(creature, entity -> entity instanceof Creature other && other.kind().disposition().hostile(),
                target -> true, 4f, 2.0, 2.0));
        brain.add(10, new LookAround(creature));
        brain.add(13, new FollowParent(creature, life, 1.25));
        brain.add(14, Wander.onLand(creature, 1.0));
    }

    @Override
    protected String foodTag() {
        return "minecraft:panda_food";
    }
}
