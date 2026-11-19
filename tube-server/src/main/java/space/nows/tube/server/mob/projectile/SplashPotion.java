package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.item.SplashPotionMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import space.nows.tube.server.world.effect.WorldEffect;

public final class SplashPotion extends Entity {

    private static final double GRAVITY = 0.05;
    private static final double DRAG = 0.99;
    private static final double TICKS_PER_SECOND = 20;
    private static final double SPLASH_RANGE = 4;
    private static final int LIFETIME = 1200;

    private final Entity owner;
    private final Brew brew;
    private Vec motion;

    public SplashPotion(Entity owner, Brew brew, Vec motion) {
        super(EntityType.SPLASH_POTION);
        this.owner = owner;
        this.brew = brew;
        this.motion = motion;
        setNoGravity(true);
        if (getEntityMeta() instanceof SplashPotionMeta meta) {
            meta.setItem(brew.item(Material.SPLASH_POTION));
        }
    }

    @Override
    public void update(long time) {
        Instance instance = getInstance();
        if (instance == null || isRemoved()) {
            return;
        }
        if (getAliveTicks() > LIFETIME || !instance.isChunkLoaded(getPosition())) {
            remove();
            return;
        }
        Trajectory path = Trajectory.sweep(this, owner, motion);
        if (path.entity() != null || path.block() != null) {
            shatter(path.entity());
            return;
        }
        Pos from = getPosition();
        refreshPosition(from.add(motion));
        motion = motion.mul(DRAG).sub(0, GRAVITY, 0);
        setVelocity(motion.mul(TICKS_PER_SECOND));
    }

    private void shatter(LivingEntity direct) {
        Instance instance = getInstance();
        for (Entity entity : instance.getNearbyEntities(getPosition(), SPLASH_RANGE)) {
            if (!(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }
            double distance = entity.getPosition().distance(getPosition());
            double strength = entity == direct ? 1.0 : 1.0 - distance / SPLASH_RANGE;
            if (strength > 0) {
                brew.apply(living, strength);
            }
        }
        (brew.instant() ? WorldEffect.INSTANT_SPLASH_POTION : WorldEffect.SPLASH_POTION).play(instance, getPosition(), brew.color());
        remove();
    }
}
