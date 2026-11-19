package space.nows.tube.server.mob.kind.flier;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.FloatAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.FollowOwner;
import space.nows.tube.server.mob.brain.social.StaySeated;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.animal.Animal;
import space.nows.tube.server.mob.life.Tamable;
import space.nows.tube.server.mob.life.Tameness;
import space.nows.tube.server.mob.move.FloatControl;

import java.util.Set;

public final class Parrot extends Animal implements Tamable {

    private static final CreatureStats STATS = CreatureStats.animal(6, 0.2).withDamage(3);
    private static final Set<Material> SEEDS = ItemTags.of("minecraft:parrot_food");
    private static final double FLYING_SPEED = 0.05;
    private static final int RANGE = 6;

    @Override
    public EntityType type() {
        return EntityType.PARROT;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.FLYING_SPEED).setBaseValue(FLYING_SPEED);
        FloatControl control = new FloatControl(creature);
        Tameness tameness = new Tameness(creature);
        creature.addTrait(control);
        creature.addTrait(tameness);
        brain.add(0, new Panic(creature, 1.25));
        brain.add(1, WatchNearby.players(creature, 8f));
        brain.add(2, new StaySeated(creature, tameness));
        brain.add(2, new FollowOwner(creature, tameness, 1.0, 5, 1));
        brain.add(2, new FloatAround(creature, control, RANGE));
    }

    @Override
    public Set<Material> breedingFood() {
        return Set.of();
    }

    @Override
    public Set<Material> tamingFood() {
        return SEEDS;
    }

    @Override
    public int tameOneIn() {
        return 10;
    }
}
