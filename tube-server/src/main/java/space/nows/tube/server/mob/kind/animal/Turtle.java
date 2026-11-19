package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;

public final class Turtle extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(30, 0.25);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:turtle_food");
    private static final double SWIM_SPEED = 0.05;

    @Override
    public EntityType type() {
        return EntityType.TURTLE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        creature.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(1);
        brain.add(0, new Panic(creature, 1.2));
        brain.add(1, new Breed(creature, life, 1.0));
        brain.add(2, new Tempted(creature, 1.1, FOOD));
        brain.add(5, new SwimAbout(creature, SWIM_SPEED));
        brain.add(7, Wander.onLand(creature, 1.0));
        brain.add(8, WatchNearby.players(creature, 8f));
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
