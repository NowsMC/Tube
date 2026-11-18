package space.nows.tube.server.mob.kind.monster.skeleton;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.world.Difficulty;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.combat.BowAttack;
import space.nows.tube.server.mob.ai.flee.AvoidNearby;
import space.nows.tube.server.mob.ai.flee.FleeSun;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;
import space.nows.tube.server.mob.ai.target.HuntNearest;
import space.nows.tube.server.mob.ai.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.kind.monster.Turtles;
import space.nows.tube.server.mob.trait.SunBurn;

import java.util.random.RandomGenerator;

public final class Skeleton extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(20, 0.25);
    private static final int HARD_INTERVAL = 20;
    private static final int INTERVAL = 40;
    private static final float BOW_RANGE = 15f;

    @Override
    public EntityType type() {
        return EntityType.SKELETON;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        int interval = MinecraftServer.getDifficulty() == Difficulty.HARD ? HARD_INTERVAL : INTERVAL;
        goals.add(3, new FleeSun(creature, 1.0));
        goals.add(3, AvoidNearby.type(creature, EntityType.WOLF, 6f, 1.0, 1.2));
        goals.add(4, new BowAttack(creature, 1.0, interval, BOW_RANGE, new SkeletonVolley()));
        goals.add(4, new SkeletonMelee(creature));
        goals.add(5, Wander.onLand(creature, 1.0));
        goals.add(6, WatchNearby.players(creature, 8f));
        goals.add(6, new LookAround(creature));
        targets.add(1, new Retaliate(creature));
        targets.add(2, HuntNearest.players(creature, true));
        targets.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
        targets.add(3, Turtles.hatchlingHunt(creature));
        creature.addTrait(new SunBurn(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.setItemInMainHand(ItemStack.of(Material.BOW));
    }
}
