package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;
import java.util.random.RandomGenerator;

public final class Cow extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(10, 0.2);
    private static final Set<Material> FOOD = Set.of(Material.WHEAT);

    @Override
    public EntityType type() {
        return EntityType.COW;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(0, new StayAfloat(creature));
        brain.add(1, new Panic(creature, 2.0));
        brain.add(2, new Breed(creature, life, 1.0));
        brain.add(3, new Tempted(creature, 1.25, FOOD));
        brain.add(4, new FollowParent(creature, life, 1.25));
        brain.add(5, Wander.onLand(creature, 1.0));
        brain.add(6, WatchNearby.players(creature, 6f));
        brain.add(7, new LookAround(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.set(DataComponents.COW_VARIANT, FarmClimate.at(creature.getInstance(), creature.getPosition()).variant());
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.COW_VARIANT, random);
    }
}
