package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.life.Lifecycle;

public final class Strider extends Grazer {

    private static final CreatureStats STATS = CreatureStats.animal(20, 0.175);

    @Override
    public EntityType type() {
        return EntityType.STRIDER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected String foodTag() {
        return "minecraft:strider_food";
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(1, new Panic(creature, 1.65));
        brain.add(2, new Breed(creature, life, 1.0));
        brain.add(5, new FollowParent(creature, life, 1.0));
        brain.add(7, Wander.anywhere(creature, 1.0, 60));
        brain.add(8, WatchNearby.players(creature, 8f));
        brain.add(8, new LookAround(creature));
        brain.add(9, WatchNearby.of(creature, entity -> entity.getEntityType() == EntityType.STRIDER, 8f));
    }

    @Override
    public boolean fireproof() {
        return true;
    }
}
