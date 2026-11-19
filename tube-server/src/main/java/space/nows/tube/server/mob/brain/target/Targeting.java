package space.nows.tube.server.mob.brain.target;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;

import java.util.function.Predicate;

public record Targeting(boolean combat, double range, boolean lineOfSight, boolean visibility, Predicate<LivingEntity> filter) {

    private static final double MIN_RANGE = 2.0;
    private static final double SNEAK_FACTOR = 0.8;
    private static final double INVISIBLE_FACTOR = 0.7 * 0.1;

    public static Targeting combat(double range) {
        return new Targeting(true, range, true, true, entity -> true);
    }

    public static Targeting peaceful(double range) {
        return new Targeting(false, range, true, true, entity -> true);
    }

    public Targeting blind() {
        return new Targeting(combat, range, false, visibility, filter);
    }

    public Targeting ignoringInvisibility() {
        return new Targeting(combat, range, lineOfSight, false, filter);
    }

    public Targeting withRange(double value) {
        return new Targeting(combat, value, lineOfSight, visibility, filter);
    }

    public Targeting only(Predicate<LivingEntity> extra) {
        return new Targeting(combat, range, lineOfSight, visibility, filter.and(extra));
    }

    public boolean test(Creature seeker, LivingEntity target) {
        if (seeker == target || target.isDead() || target.isRemoved() || target.getInstance() != seeker.getInstance()) {
            return false;
        }
        if (target instanceof Player player && (player.getGameMode() == GameMode.SPECTATOR || combat && player.getGameMode() == GameMode.CREATIVE)) {
            return false;
        }
        if (!filter.test(target)) {
            return false;
        }
        if (range > 0) {
            double reach = Math.max(range * (visibility ? visibilityOf(target) : 1.0), MIN_RANGE);
            if (seeker.getPosition().distanceSquared(target.getPosition()) > reach * reach) {
                return false;
            }
        }
        return !lineOfSight || seeker.sight().canSee(target);
    }

    public Player nearestPlayer(Creature seeker) {
        Instance instance = seeker.getInstance();
        if (instance == null) {
            return null;
        }
        Point eye = seeker.getPosition().add(0, seeker.getEyeHeight(), 0);
        Player nearest = null;
        double best = Double.MAX_VALUE;
        for (Player player : instance.getPlayers()) {
            double distance = player.getPosition().distanceSquared(eye);
            if (distance < best && test(seeker, player)) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    public LivingEntity nearest(Creature seeker, double horizontal, double vertical, Predicate<Entity> kind) {
        Instance instance = seeker.getInstance();
        if (instance == null) {
            return null;
        }
        Point origin = seeker.getPosition();
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : instance.getNearbyEntities(origin, Math.max(horizontal, vertical) * 1.75)) {
            if (!(entity instanceof LivingEntity living) || !kind.test(entity)) {
                continue;
            }
            Point at = entity.getPosition();
            if (Math.abs(at.x() - origin.x()) > horizontal || Math.abs(at.z() - origin.z()) > horizontal || Math.abs(at.y() - origin.y()) > vertical) {
                continue;
            }
            double distance = at.distanceSquared(origin);
            if (distance < best && test(seeker, living)) {
                best = distance;
                nearest = living;
            }
        }
        return nearest;
    }

    private static double visibilityOf(LivingEntity target) {
        double factor = 1.0;
        if (target.isSneaking()) {
            factor *= SNEAK_FACTOR;
        }
        if (target.hasEffect(PotionEffect.INVISIBILITY) || target.isInvisible()) {
            factor *= INVISIBLE_FACTOR;
        }
        return factor;
    }
}
