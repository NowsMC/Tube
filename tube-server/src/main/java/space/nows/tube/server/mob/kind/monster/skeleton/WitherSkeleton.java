package space.nows.tube.server.mob.kind.monster.skeleton;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.target.HuntNearest;

import java.util.random.RandomGenerator;

public final class WitherSkeleton extends Skeleton {

    private static final CreatureStats STATS = CreatureStats.monster(20, 0.25).withDamage(4);
    private static final int WITHER_TICKS = 200;

    @Override
    public EntityType type() {
        return EntityType.WITHER_SKELETON;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean fireproof() {
        return true;
    }

    @Override
    protected Material weapon() {
        return Material.STONE_SWORD;
    }

    @Override
    protected boolean burnsInSun() {
        return false;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(3, HuntNearest.of(creature, entity -> entity.getEntityType() == EntityType.PIGLIN
                || entity.getEntityType() == EntityType.PIGLIN_BRUTE, 10, true, target -> true));
        super.configure(creature, brain);
    }

    @Override
    public void landedHit(Creature creature, LivingEntity target) {
        target.addEffect(new Potion(PotionEffect.WITHER, 0, WITHER_TICKS));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        creature.setItemInMainHand(ItemStack.of(weapon()));
    }
}
