package space.nows.tube.server.gameplay.throwing;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.world.effect.ParticleSpawner;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

final class Impacts {

    private static final float KNOCKBACK = 0.4f;
    private static final float PEARL_DAMAGE = 5;
    private static final int PORTAL_PARTICLES = 32;
    private static final long SHOOTER_GRACE_TICKS = 5;

    private Impacts() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            if (event.getEntity() instanceof EntityProjectile projectile && ThrownKind.of(projectile.getEntityType()).isPresent()) {
                onEntity(event, projectile);
            }
        });
        handler.addListener(ProjectileCollideWithBlockEvent.class, event -> {
            if (event.getEntity() instanceof EntityProjectile projectile && ThrownKind.of(projectile.getEntityType()).isPresent()) {
                land(projectile, event.getCollisionPosition());
            }
        });
    }

    private static void onEntity(ProjectileCollideWithEntityEvent event, EntityProjectile projectile) {
        Entity shooter = projectile.getShooter();
        if (event.getTarget() == shooter && projectile.getAliveTicks() < SHOOTER_GRACE_TICKS || !(event.getTarget() instanceof LivingEntity target)) {
            event.setCancelled(true);
            return;
        }
        target.damage(new Damage(DamageType.THROWN, projectile, shooter, event.getCollisionPosition(), 0f));
        Vec velocity = projectile.getVelocity();
        double length = Math.hypot(velocity.x(), velocity.z());
        if (length > 0.01) {
            target.takeKnockback(KNOCKBACK, -velocity.x() / length, -velocity.z() / length);
        }
        land(projectile, event.getCollisionPosition());
    }

    private static void land(EntityProjectile projectile, Pos at) {
        Instance instance = projectile.getInstance();
        ThrownKind kind = ThrownKind.of(projectile.getEntityType()).orElse(null);
        projectile.remove();
        if (instance == null || kind == null) {
            return;
        }
        if (kind.isEgg()) {
            EggHatching.hatch(instance, at, ThreadLocalRandom.current());
        } else if (kind == ThrownKind.ENDER_PEARL && projectile.getShooter() instanceof Player player && player.getInstance() == instance) {
            teleport(player, at);
        }
    }

    private static void teleport(Player player, Pos at) {
        Instance instance = player.getInstance();
        ParticleSpawner.spawn(instance, "minecraft:portal", player.getPosition(), PORTAL_PARTICLES, 0.5, 0.1);
        player.teleport(at.withView(player.getPosition().yaw(), player.getPosition().pitch()));
        ParticleSpawner.spawn(instance, "minecraft:portal", at, PORTAL_PARTICLES, 0.5, 0.1);
        SoundPlayer.play(instance, "minecraft:entity.player.teleport", Sound.Source.PLAYER, at, 1f, 1f);
        player.damage(new Damage(DamageType.FALL, null, null, null, PEARL_DAMAGE));
    }
}
