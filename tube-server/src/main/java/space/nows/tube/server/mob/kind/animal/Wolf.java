package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.combat.Pounce;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowOwner;
import space.nows.tube.server.mob.brain.social.StaySeated;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Loyalty;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.monster.Turtles;
import space.nows.tube.server.mob.life.Lifecycle;
import space.nows.tube.server.mob.life.Tamable;
import space.nows.tube.server.mob.life.Tameness;
import space.nows.tube.server.mob.sense.Grudge;

import java.util.Set;
import java.util.random.RandomGenerator;

public final class Wolf extends Animal implements Tamable {

    private static final CreatureStats STATS = CreatureStats.animal(8, 0.3).withDamage(4);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:wolf_food");
    private static final Set<Material> BONES = Set.of(Material.BONE);
    private static final Set<EntityType> PREY = Set.of(EntityType.SHEEP, EntityType.RABBIT, EntityType.FOX);
    private static final Set<EntityType> SKELETONS = Set.of(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON,
            EntityType.BOGGED, EntityType.PARCHED);
    private static final double TAMED_HEALTH = 40;
    private static final int MIN_ANGER = 400;
    private static final int MAX_ANGER = 780;

    @Override
    public EntityType type() {
        return EntityType.WOLF;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.NEUTRAL;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        Tameness tameness = new Tameness(creature);
        Grudge grudge = new Grudge(creature, MIN_ANGER, MAX_ANGER);
        creature.addTrait(tameness);
        creature.addTrait(grudge);
        brain.add(1, new StayAfloat(creature));
        brain.add(1, new Panic(creature, 1.5));
        brain.add(2, new StaySeated(creature, tameness));
        brain.add(3, new AvoidNearby(creature, entity -> entity.getEntityType() == EntityType.LLAMA, target -> !tameness.tamed(), 24f, 1.5, 1.5));
        brain.add(4, new Pounce(creature, 0.4f));
        brain.add(5, new MeleeAttack(creature, 1.0, true));
        brain.add(6, new FollowOwner(creature, tameness, 1.0, 10, 2));
        brain.add(7, new Breed(creature, life, 1.0));
        brain.add(8, Wander.onLand(creature, 1.0));
        brain.add(10, WatchNearby.players(creature, 8f));
        brain.add(10, new LookAround(creature));
        brain.add(1, Loyalty.defendOwner(creature, tameness));
        brain.add(2, Loyalty.joinOwner(creature, tameness));
        brain.add(3, new Retaliate(creature).alertingOthers());
        brain.add(4, HuntNearest.players(creature, true, grudge::holds));
        brain.add(5, HuntNearest.of(creature, entity -> PREY.contains(entity.getEntityType()), 10, false, target -> true)
                .when(wolf -> !tameness.tamed()));
        brain.add(6, Turtles.hatchlingHunt(creature).when(wolf -> !tameness.tamed()));
        brain.add(7, HuntNearest.of(creature, entity -> SKELETONS.contains(entity.getEntityType()), 10, false, target -> true));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() instanceof LivingEntity attacker) {
            creature.trait(Grudge.class).ifPresent(grudge -> grudge.provoke(attacker));
            creature.trait(Tameness.class).ifPresent(tameness -> tameness.sit(false));
        }
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        pickVariant(creature, DataComponents.WOLF_VARIANT, "wolf_variant", random);
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.WOLF_VARIANT, random);
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }

    @Override
    public Set<Material> tamingFood() {
        return BONES;
    }

    @Override
    public int tameOneIn() {
        return 3;
    }

    @Override
    public void tamed(Creature creature) {
        creature.getAttribute(Attribute.MAX_HEALTH).setBaseValue(TAMED_HEALTH);
        creature.setHealth((float) TAMED_HEALTH);
    }
}
