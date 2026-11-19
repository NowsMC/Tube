package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.combat.Pounce;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.idle.ClimbPowderSnow;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.monster.Turtles;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;

public final class Fox extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(10, 0.3).withDamage(2).withFollowRange(32);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:fox_food");
    private static final Set<EntityType> PREY = Set.of(EntityType.CHICKEN, EntityType.RABBIT);
    private static final Set<EntityType> FISH = Set.of(EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH);

    @Override
    public EntityType type() {
        return EntityType.FOX;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(0, new StayAfloat(creature));
        brain.add(0, new ClimbPowderSnow(creature));
        brain.add(1, new Panic(creature, 2.2));
        brain.add(2, new Breed(creature, life, 1.0));
        brain.add(3, new AvoidNearby(creature, entity -> entity instanceof Player, target -> !target.isSneaking(), 16f, 1.6, 1.4));
        brain.add(4, new AvoidNearby(creature, entity -> entity.getEntityType() == EntityType.WOLF
                || entity.getEntityType() == EntityType.POLAR_BEAR, target -> true, 8f, 1.6, 1.4));
        brain.add(5, new FollowParent(creature, life, 1.25));
        brain.add(6, new Pounce(creature, 0.4f));
        brain.add(7, new MeleeAttack(creature, 1.2, true));
        brain.add(11, Wander.onLand(creature, 1.0));
        brain.add(12, WatchNearby.players(creature, 24f));
        brain.add(13, new LookAround(creature));
        brain.add(4, HuntNearest.of(creature, entity -> PREY.contains(entity.getEntityType()), 10, false, target -> true));
        brain.add(4, Turtles.hatchlingHunt(creature));
        brain.add(6, HuntNearest.of(creature, entity -> FISH.contains(entity.getEntityType()), 20, false, target -> true));
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
