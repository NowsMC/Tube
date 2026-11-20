package space.nows.tube.server.gameplay.melee;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import space.nows.tube.server.item.Enchantments;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.KillCredit;
import space.nows.tube.server.mob.life.Tameness;
import space.nows.tube.server.registry.RegistryTags;
import space.nows.tube.server.world.effect.ParticleSpawner;
import space.nows.tube.server.world.effect.SoundPlayer;

public final class SweepAttack {

    private static final double FULL_CHARGE = 0.9;
    private static final double SPEED_LIMIT = 2.5;
    private static final double REACH_SQUARED = 9;
    private static final double REACH = 3;
    private static final double SPREAD = 1;
    private static final double SPREAD_HEIGHT = 0.25;
    private static final float KNOCKBACK = 0.4f;
    private static final double PARTICLE_HEIGHT = 0.5;

    private SweepAttack() {
    }

    public static boolean applies(Player attacker, ItemStack weapon, double charge, boolean critical, boolean sprintKnockback) {
        if (charge <= FULL_CHARGE || critical || sprintKnockback || !attacker.isOnGround() || !RegistryTags.has(weapon.material(), "minecraft:swords")) {
            return false;
        }
        double limit = attacker.getAttributeValue(Attribute.MOVEMENT_SPEED) * SPEED_LIMIT;
        return Pace.stepSquared(attacker) < limit * limit;
    }

    public static void strike(Player attacker, LivingEntity target, ItemStack weapon, float baseDamage, double charge) {
        if (attacker.getInstance() == null) {
            return;
        }
        double yaw = Math.toRadians(attacker.getPosition().yaw());
        double edge = ratio(weapon) * baseDamage + 1;
        for (Entity entity : attacker.getInstance().getNearbyEntities(attacker.getPosition(), REACH)) {
            if (entity instanceof LivingEntity victim && sweeps(attacker, target, victim)) {
                float amount = (float) ((edge + Sharpness.bonus(weapon)) * charge);
                if (victim.damage(Damage.fromPlayer(attacker, amount))) {
                    Invulnerability.start(victim);
                    KillCredit.hitBy(victim, attacker);
                    victim.takeKnockback(KNOCKBACK, Math.sin(yaw), -Math.cos(yaw));
                }
            }
        }
        show(attacker, yaw);
    }

    private static double ratio(ItemStack weapon) {
        int level = Enchantments.level(weapon, "minecraft:sweeping_edge");
        return level / (level + 1.0);
    }

    private static boolean sweeps(Player attacker, LivingEntity target, LivingEntity victim) {
        return victim != attacker
                && victim != target
                && Attackable.by(victim)
                && !Invulnerability.active(victim)
                && !ownedBy(victim, attacker)
                && attacker.getPosition().distanceSquared(victim.getPosition()) < REACH_SQUARED
                && withinSpread(target, victim);
    }

    private static boolean ownedBy(LivingEntity victim, Player attacker) {
        return victim instanceof Creature creature && creature.trait(Tameness.class).map(tameness -> tameness.ownedBy(attacker)).orElse(false);
    }

    private static boolean withinSpread(LivingEntity target, LivingEntity victim) {
        Pos at = target.getPosition();
        Pos other = victim.getPosition();
        double reachX = (target.getBoundingBox().width() + victim.getBoundingBox().width()) / 2 + SPREAD;
        double reachZ = (target.getBoundingBox().depth() + victim.getBoundingBox().depth()) / 2 + SPREAD;
        return Math.abs(at.x() - other.x()) < reachX
                && Math.abs(at.z() - other.z()) < reachZ
                && other.y() < at.y() + target.getBoundingBox().height() + SPREAD_HEIGHT
                && other.y() + victim.getBoundingBox().height() > at.y() - SPREAD_HEIGHT;
    }

    private static void show(Player attacker, double yaw) {
        SoundPlayer.play(attacker.getInstance(), SoundEvent.ENTITY_PLAYER_ATTACK_SWEEP, Sound.Source.PLAYER, attacker.getPosition(), 1f, 1f);
        double dx = -Math.sin(yaw);
        double dz = Math.cos(yaw);
        Pos at = attacker.getPosition();
        double height = attacker.getBoundingBox().height() * PARTICLE_HEIGHT;
        ParticleSpawner.spawn(attacker.getInstance(), "minecraft:sweep_attack", at.x() + dx, at.y() + height, at.z() + dz, 0, dx, 0, dz, 0);
    }
}
