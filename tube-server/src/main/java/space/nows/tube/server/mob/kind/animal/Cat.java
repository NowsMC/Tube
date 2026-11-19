package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.Pounce;
import space.nows.tube.server.mob.brain.combat.Stalk;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowOwner;
import space.nows.tube.server.mob.brain.social.StaySeated;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.monster.Turtles;
import space.nows.tube.server.mob.life.Lifecycle;
import space.nows.tube.server.mob.life.Tamable;
import space.nows.tube.server.mob.life.Tameness;

import java.util.Set;
import java.util.random.RandomGenerator;

public final class Cat extends Animal implements Tamable {

    private static final CreatureStats STATS = CreatureStats.animal(10, 0.3).withDamage(3);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:cat_food");

    @Override
    public EntityType type() {
        return EntityType.CAT;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        Tameness tameness = new Tameness(creature);
        creature.addTrait(tameness);
        brain.add(1, new StayAfloat(creature));
        brain.add(1, new Panic(creature, 1.5));
        brain.add(2, new StaySeated(creature, tameness));
        brain.add(4, new Tempted(creature, 0.6, FOOD));
        brain.add(6, new FollowOwner(creature, tameness, 1.0, 10, 5));
        brain.add(8, new Pounce(creature, 0.3f));
        brain.add(9, new Stalk(creature));
        brain.add(10, new Breed(creature, life, 0.8));
        brain.add(11, Wander.onLandOnly(creature, 0.8));
        brain.add(12, WatchNearby.players(creature, 10f));
        brain.add(1, HuntNearest.of(creature, EntityType.RABBIT, false).when(cat -> !tameness.tamed()));
        brain.add(1, Turtles.hatchlingHunt(creature).when(cat -> !tameness.tamed()));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        pickVariant(creature, DataComponents.CAT_VARIANT, "cat_variant", random);
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.CAT_VARIANT, random);
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }

    @Override
    public Set<Material> tamingFood() {
        return FOOD;
    }

    @Override
    public int tameOneIn() {
        return 3;
    }
}
