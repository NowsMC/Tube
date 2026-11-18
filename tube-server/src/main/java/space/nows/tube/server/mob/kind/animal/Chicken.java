package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.Panic;
import space.nows.tube.server.mob.ai.idle.StayAfloat;
import space.nows.tube.server.mob.ai.idle.Tempted;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;

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
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        goals.add(0, new StayAfloat(creature));
        goals.add(1, new Panic(creature, 1.4));
        goals.add(3, new Tempted(creature, 1.0, FOOD));
        goals.add(5, Wander.onLand(creature, 1.0));
        goals.add(6, WatchNearby.players(creature, 6f));
        goals.add(7, new LookAround(creature));
        creature.addTrait(new Flutter(creature));
        creature.addTrait(new EggLaying(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.set(DataComponents.CHICKEN_VARIANT, FarmClimate.at(creature.getInstance(), creature.getPosition()).variant());
    }
}
