package space.nows.tube.server.mob.kind.boss;

import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.FireDamage;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class EnderDragon extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(200, 0.7).withDamage(10).withKnockbackResistance(1);

    @Override
    public EntityType type() {
        return EntityType.ENDER_DRAGON;
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
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    @Override
    public boolean acceptsDamage(Creature creature, Damage damage) {
        return !FireDamage.is(damage);
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.addTrait(new Soar(creature));
        creature.addTrait(new Banner(creature, "entity.minecraft.ender_dragon", BossBar.Color.PINK));
    }
}
