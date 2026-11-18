package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.SheepMeta;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.idle.Graze;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.Panic;
import space.nows.tube.server.mob.ai.idle.StayAfloat;
import space.nows.tube.server.mob.ai.idle.Tempted;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;
import space.nows.tube.server.mob.ai.social.Breed;
import space.nows.tube.server.mob.ai.social.FollowParent;
import space.nows.tube.server.mob.life.Lifecycle;

import java.util.Set;
import java.util.random.RandomGenerator;

public final class Sheep extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(8, 0.23);
    private static final Set<Material> FOOD = Set.of(Material.WHEAT);
    private static final int GRAZE_GROWTH_SECONDS = 60;

    @Override
    public EntityType type() {
        return EntityType.SHEEP;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        Lifecycle life = lifecycle(creature);
        goals.add(0, new StayAfloat(creature));
        goals.add(1, new Panic(creature, 1.25));
        goals.add(2, new Breed(creature, life, 1.0));
        goals.add(3, new Tempted(creature, 1.1, FOOD));
        goals.add(4, new FollowParent(creature, life, 1.1));
        goals.add(5, new Graze(creature, () -> regrow(creature, life)));
        goals.add(6, Wander.onLand(creature, 1.0));
        goals.add(7, WatchNearby.players(creature, 6f));
        goals.add(8, new LookAround(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.set(DataComponents.SHEEP_COLOR, WoolPalette.pick(FarmClimate.at(creature.getInstance(), creature.getPosition()), random));
    }

    private static void regrow(Creature creature, Lifecycle life) {
        if (creature.getEntityMeta() instanceof SheepMeta sheep) {
            sheep.setSheared(false);
        }
        life.ageUp(GRAZE_GROWTH_SECONDS);
    }

    @Override
    public Set<Material> breedingFood() {
        return FOOD;
    }

    @Override
    public void inherit(Creature child, Creature mother, Creature father, RandomGenerator random) {
        inheritComponent(child, mother, father, DataComponents.SHEEP_COLOR, random);
    }
}
