package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.Pounce;
import space.nows.tube.server.mob.brain.combat.Stalk;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Tempted;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.Breed;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.interact.ItemTags;
import space.nows.tube.server.mob.kind.monster.Turtles;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;

public final class Ocelot extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(10, 0.3).withDamage(3);
    private static final Set<Material> FOOD = ItemTags.of("minecraft:ocelot_food");

    @Override
    public EntityType type() {
        return EntityType.OCELOT;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Lifecycle life = lifecycle(creature);
        brain.add(1, new StayAfloat(creature));
        brain.add(3, new Tempted(creature, 0.6, FOOD));
        brain.add(7, new Pounce(creature, 0.3f));
        brain.add(8, new Stalk(creature));
        brain.add(9, new Breed(creature, life, 0.8));
        brain.add(10, Wander.onLandOnly(creature, 0.8));
        brain.add(11, WatchNearby.players(creature, 10f));
        brain.add(1, HuntNearest.of(creature, EntityType.CHICKEN, false));
        brain.add(1, Turtles.hatchlingHunt(creature));
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }
}
