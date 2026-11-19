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

public final class Chicken extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(4, 0.25);
    private static final Set<Material> FOOD = Set.of(Material.WHEAT_SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS,
            Material.BEETROOT_SEEDS, Material.TORCHFLOWER_SEEDS, Material.PITCHER_POD);

    @Override
    public EntityType type() {
        return EntityType.CHICKEN;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(0, new StayAfloat(creature));
        brain.add(1, new Panic(creature, 1.4));
        brain.add(2, new Breed(creature, life, 1.0));
        brain.add(3, new Tempted(creature, 1.0, FOOD));
        brain.add(4, new FollowParent(creature, life, 1.1));
        brain.add(5, Wander.onLand(creature, 1.0));
        brain.add(6, WatchNearby.players(creature, 6f));
        brain.add(7, new LookAround(creature));
        creature.addTrait(new Flutter(creature));
        creature.addTrait(new EggLaying(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        pickVariant(creature, DataComponents.CHICKEN_VARIANT, "chicken_variant", random);
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.CHICKEN_VARIANT, random);
    }
}
