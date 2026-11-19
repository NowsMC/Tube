package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

public final class Bullet extends Entity {

    private static final double SPEED = 0.15;
    private static final double TURN = 0.2;
    private static final double TICKS_PER_SECOND = 20;
    private static final float DAMAGE = 4;
    private static final int LEVITATION = 200;
    private static final int LIFETIME = 300;

    private final Entity owner;
    private final LivingEntity target;
    private Vec motion = Vec.ZERO;

    public Bullet(Entity owner, LivingEntity target) {
        super(EntityType.SHULKER_BULLET);
        this.owner = owner;
        this.target = target;
        setNoGravity(true);
    }

    @Override
    public void update(long time) {
        Instance instance = getInstance();
        if (instance == null || isRemoved()) {
            return;
        }
        if (getAliveTicks() > LIFETIME || target.isDead() || target.isRemoved() || target.getInstance() != instance) {
            remove();
            return;
        }
        Vec wanted = target.getPosition().add(0, target.getBoundingBox().height() / 2, 0).sub(getPosition()).asVec().normalize().mul(SPEED);
        motion = motion.add(wanted.sub(motion).mul(TURN));
        Trajectory path = Trajectory.sweep(this, owner, motion);
        if (path.entity() != null) {
            path.entity().damage(new Damage(DamageType.MOB_PROJECTILE, this, owner, getPosition(), DAMAGE));
            path.entity().addEffect(new Potion(PotionEffect.LEVITATION, 0, LEVITATION));
            remove();
            return;
        }
        if (path.block() != null) {
            remove();
            return;
        }
        refreshPosition(getPosition().add(motion));
        setVelocity(motion.mul(TICKS_PER_SECOND));
    }
}
