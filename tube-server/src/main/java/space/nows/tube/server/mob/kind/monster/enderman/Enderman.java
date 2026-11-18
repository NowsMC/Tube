package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.ai.GoalSelector;
import space.nows.tube.server.mob.ai.combat.MeleeAttack;
import space.nows.tube.server.mob.ai.idle.LookAround;
import space.nows.tube.server.mob.ai.idle.StayAfloat;
import space.nows.tube.server.mob.ai.idle.Wander;
import space.nows.tube.server.mob.ai.idle.WatchNearby;
import space.nows.tube.server.mob.ai.target.HuntNearest;
import space.nows.tube.server.mob.ai.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.trait.WaterAllergy;

import java.util.Set;

public final class Enderman extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(40, 0.3).withDamage(7).withFollowRange(64);
    private static final int PROJECTILE_ESCAPES = 64;
    private static final int STAY_ONE_IN = 10;
    private static final Set<String> PROJECTILE_DAMAGE = Set.of("minecraft:arrow", "minecraft:trident", "minecraft:mob_projectile",
            "minecraft:fireball", "minecraft:unattributed_fireball", "minecraft:wither_skull", "minecraft:thrown",
            "minecraft:wind_charge", "minecraft:spit", "minecraft:fireworks");

    @Override
    public EntityType type() {
        return EntityType.ENDERMAN;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, GoalSelector goals, GoalSelector targets) {
        Aggravation aggravation = new Aggravation(creature);
        goals.add(0, new StayAfloat(creature));
        goals.add(1, new FreezeWhenStared(creature));
        goals.add(2, new MeleeAttack(creature, 1.0, false));
        goals.add(7, Wander.onLandOnly(creature, 1.0));
        goals.add(8, WatchNearby.players(creature, 8f));
        goals.add(8, new LookAround(creature));
        goals.add(10, new PlaceBlock(creature));
        goals.add(11, new TakeBlock(creature));
        targets.add(1, new StareAggro(creature, aggravation));
        targets.add(2, new Retaliate(creature));
        targets.add(3, HuntNearest.of(creature, EntityType.ENDERMITE, true));
        creature.addTrait(aggravation);
        creature.addTrait(new DaylightTeleport(creature, aggravation));
        creature.addTrait(new WaterAllergy(creature));
    }

    @Override
    public boolean acceptsDamage(Creature creature, Damage damage) {
        if (!projectile(damage)) {
            return true;
        }
        for (int i = 0; i < PROJECTILE_ESCAPES; i++) {
            if (EnderTeleport.randomly(creature)) {
                break;
            }
        }
        return false;
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (!(damage.getAttacker() instanceof LivingEntity) && creature.random().nextInt(STAY_ONE_IN) != 0) {
            EnderTeleport.randomly(creature);
        }
    }

    private static boolean projectile(Damage damage) {
        return damage.getSource() instanceof EntityProjectile || PROJECTILE_DAMAGE.contains(damage.getType().key().asString());
    }
}
