package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.WatchNearby;

import java.util.random.RandomGenerator;

public final class Vindicator extends Illager {

    private static final CreatureStats STATS = CreatureStats.monster(24, 0.35).withDamage(5).withFollowRange(12);

    @Override
    public EntityType type() {
        return EntityType.VINDICATOR;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected boolean seesVillagers() {
        return true;
    }

    @Override
    protected void behaviors(Creature creature, Brain brain) {
        brain.add(5, new MeleeAttack(creature, 1.0, false));
        brain.add(9, WatchNearby.of(creature, entity -> entity instanceof Player, 3f));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.setItemInMainHand(ItemStack.of(Material.IRON_AXE));
    }
}
