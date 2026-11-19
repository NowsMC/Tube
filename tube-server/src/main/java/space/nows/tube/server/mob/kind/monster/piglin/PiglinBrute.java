package space.nows.tube.server.mob.kind.monster.piglin;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

import java.util.random.RandomGenerator;

public final class PiglinBrute extends Piglin {

    private static final CreatureStats STATS = CreatureStats.monster(50, 0.35).withDamage(7).withFollowRange(12);

    @Override
    public EntityType type() {
        return EntityType.PIGLIN_BRUTE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected boolean spares(LivingEntity player) {
        return false;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.setItemInMainHand(ItemStack.of(Material.GOLDEN_AXE));
    }
}
