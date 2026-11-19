package space.nows.tube.server.mob.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.server.world.effect.EntityEffect;

public final class Fangs extends Entity {

    private static final int BITE_AT = -8;
    private static final int LIFE = 22;
    private static final double REACH = 0.2;
    private static final float DAMAGE = 6;

    private final Entity owner;
    private int warmup;
    private int life = LIFE;
    private boolean spiked;

    public Fangs(Entity owner, int delay) {
        super(EntityType.EVOKER_FANGS);
        this.owner = owner;
        this.warmup = delay;
        setNoGravity(true);
    }

    @Override
    public void update(long time) {
        if (getInstance() == null || --warmup >= 0) {
            return;
        }
        if (warmup == BITE_AT) {
            double half = getBoundingBox().width() / 2 + REACH;
            for (Entity entity : getInstance().getNearbyEntities(getPosition(), half + 1)) {
                if (entity instanceof LivingEntity living && entity != owner && !living.isDead()
                        && Math.abs(entity.getPosition().x() - getPosition().x()) <= half + entity.getBoundingBox().width() / 2
                        && Math.abs(entity.getPosition().z() - getPosition().z()) <= half + entity.getBoundingBox().width() / 2
                        && entity.getPosition().y() < getPosition().y() + getBoundingBox().height()
                        && entity.getPosition().y() + entity.getBoundingBox().height() > getPosition().y()) {
                    living.damage(new Damage(DamageType.INDIRECT_MAGIC, this, owner, getPosition(), DAMAGE));
                }
            }
        }
        if (!spiked) {
            EntityEffect.FANGS_BITE.play(this);
            spiked = true;
        }
        if (--life < 0) {
            remove();
        }
    }
}
