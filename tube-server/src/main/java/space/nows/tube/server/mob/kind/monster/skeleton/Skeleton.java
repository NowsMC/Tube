package space.nows.tube.server.mob.kind.monster.skeleton;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.world.Difficulty;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.BowAttack;
import space.nows.tube.server.mob.brain.flee.AvoidNearby;
import space.nows.tube.server.mob.brain.flee.FleeSun;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.ArmorRoll;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.kind.monster.Turtles;
import space.nows.tube.server.mob.spawn.LocalDifficulty;
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
    public void configure(Creature creature, Brain brain) {
        int interval = MinecraftServer.getDifficulty() == Difficulty.HARD ? HARD_INTERVAL : INTERVAL;
        brain.add(3, new FleeSun(creature, 1.0));
        brain.add(3, AvoidNearby.type(creature, EntityType.WOLF, 6f, 1.0, 1.2));
        brain.add(4, new BowAttack(creature, 1.0, interval, BOW_RANGE, new SkeletonVolley()));
        brain.add(4, new SkeletonMelee(creature));
        brain.add(5, Wander.onLand(creature, 1.0));
        brain.add(6, WatchNearby.players(creature, 8f));
        brain.add(6, new LookAround(creature));
        brain.add(1, new Retaliate(creature));
        brain.add(2, HuntNearest.players(creature, true));
        brain.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
        brain.add(3, Turtles.hatchlingHunt(creature));
        creature.addTrait(new SunBurn(creature));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        ArmorRoll.dress(creature, LocalDifficulty.at(creature.getInstance(), 0), random);
        creature.setItemInMainHand(ItemStack.of(Material.BOW));
    }
}
