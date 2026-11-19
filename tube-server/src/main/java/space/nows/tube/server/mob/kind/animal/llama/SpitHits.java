package space.nows.tube.server.mob.kind.animal.llama;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;

public final class SpitHits {

    private static final float DAMAGE = 1;

    private SpitHits() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            if (!(event.getEntity() instanceof EntityProjectile spit) || spit.getEntityType() != EntityType.LLAMA_SPIT) {
                return;
            }
            if (event.getTarget() == spit.getShooter() || !(event.getTarget() instanceof LivingEntity target)) {
                event.setCancelled(true);
                return;
            }
            target.damage(new Damage(DamageType.SPIT, spit, spit.getShooter(), event.getCollisionPosition(), DAMAGE));
            spit.remove();
        });
        handler.addListener(ProjectileCollideWithBlockEvent.class, event -> {
            if (event.getEntity().getEntityType() == EntityType.LLAMA_SPIT) {
                event.getEntity().remove();
            }
        });
    }
}
