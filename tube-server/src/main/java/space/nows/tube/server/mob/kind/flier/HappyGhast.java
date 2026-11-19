package space.nows.tube.server.mob.kind.flier;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.FloatAround;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.animal.Animal;
import space.nows.tube.server.mob.move.FloatControl;

import java.util.Set;

public final class HappyGhast extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(20, 0.05).withFollowRange(16).withTemptRange(16);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:happy_ghast_food");
    private static final double FLYING_SPEED = 0.05;
    private static final int RANGE = 16;

    @Override
    public EntityType type() {
        return EntityType.HAPPY_GHAST;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.FLYING_SPEED).setBaseValue(FLYING_SPEED);
        FloatControl control = new FloatControl(creature);
        creature.addTrait(control);
        brain.add(3, new FloatAround(creature, control, RANGE));
        brain.add(4, WatchNearby.players(creature, 8f));
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
