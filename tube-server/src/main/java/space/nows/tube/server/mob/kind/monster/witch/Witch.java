package space.nows.tube.server.mob.kind.monster.witch;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.FireDamage;
import space.nows.tube.server.mob.brain.combat.RangedAttack;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.kind.monster.Raiders;

import java.util.Set;

public final class Witch extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(26, 0.25);
    private static final Set<RegistryKey<DamageType>> RESISTED = Set.of(DamageType.MAGIC, DamageType.INDIRECT_MAGIC,
            DamageType.SONIC_BOOM, DamageType.THORNS);
    private static final float RESISTANCE = 0.15f;

    @Override
    public EntityType type() {
        return EntityType.WITCH;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Sipping sipping = new Sipping(creature);
        creature.addTrait(sipping);
        brain.add(1, new StayAfloat(creature));
        brain.add(2, new RangedAttack(creature, 1.0, 60, 10f, new PotionThrow(sipping)));
        brain.add(2, Wander.onLand(creature, 1.0));
        brain.add(3, WatchNearby.players(creature, 8f));
        brain.add(3, new LookAround(creature));
        brain.add(1, new Retaliate(creature, Raiders.types()));
        brain.add(2, HuntNearest.players(creature, true));
    }

    @Override
    public void absorb(Creature creature, Damage damage) {
        if (damage.getAttacker() == creature || damage.getSource() == creature) {
            damage.setAmount(0);
        } else if (RESISTED.contains(damage.getType())) {
            damage.setAmount(damage.getAmount() * RESISTANCE);
        }
        if (FireDamage.is(damage)) {
            creature.trait(Sipping.class).ifPresent(Sipping::burned);
        }
    }
}
