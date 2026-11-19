package space.nows.tube.server.mob.kind.monster.zombie;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.ArmsRaisedAttack;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.sense.Grudge;
import space.nows.tube.server.mob.spawn.LocalDifficulty;

import java.util.random.RandomGenerator;

public final class ZombifiedPiglin extends Zombie {

    private static final CreatureStats STATS = CreatureStats.monster(20, 0.23).withFollowRange(35).withDamage(5).withArmor(2);
    private static final int MIN_ANGER = 400;
    private static final int MAX_ANGER = 780;
    private static final int SPEAR_ONE_IN = 20;

    @Override
    public EntityType type() {
        return EntityType.ZOMBIFIED_PIGLIN;
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
    protected boolean burnsInSun() {
        return false;
    }

    @Override
    protected void behaviors(Creature creature, Brain brain) {
        Grudge grudge = new Grudge(creature, MIN_ANGER, MAX_ANGER);
        creature.addTrait(grudge);
        creature.addTrait(new PiglinAlarm(creature, grudge));
        brain.add(2, new ArmsRaisedAttack(creature, 1.0, false));
        brain.add(7, Wander.onLand(creature, 1.0));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true, grudge::holds));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() instanceof LivingEntity attacker) {
            creature.trait(Grudge.class).ifPresent(grudge -> grudge.provoke(attacker));
        }
    }

    @Override
    protected void equip(Creature creature, LocalDifficulty difficulty, RandomGenerator random) {
        creature.setItemInMainHand(ItemStack.of(random.nextInt(SPEAR_ONE_IN) == 0 ? Material.GOLDEN_SPEAR : Material.GOLDEN_SWORD));
    }
}
