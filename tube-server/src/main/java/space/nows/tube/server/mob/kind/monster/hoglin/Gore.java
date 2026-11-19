package space.nows.tube.server.mob.kind.monster.hoglin;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.server.mob.Creature;

final class Gore {

    private static final double TICKS_PER_SECOND = 20;

    private Gore() {
    }

    static float damage(Creature beast) {
        float base = (float) beast.getAttributeValue(Attribute.ATTACK_DAMAGE);
        return !beast.isBaby() && (int) base > 0 ? base / 2 + beast.random().nextInt((int) base) : base;
    }

    static void toss(Creature beast, LivingEntity target) {
        if (beast.isBaby()) {
            return;
        }
        double power = beast.getAttributeValue(Attribute.ATTACK_KNOCKBACK) - target.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
        if (power <= 0) {
            return;
        }
        double dx = target.getPosition().x() - beast.getPosition().x();
        double dz = target.getPosition().z() - beast.getPosition().z();
        double angle = Math.toRadians(beast.random().nextInt(21) - 10);
        Vec flat = new Vec(dx, 0, dz).normalize().mul(power * (beast.random().nextFloat() * 0.5 + 0.2));
        Vec turned = new Vec(flat.x() * Math.cos(angle) + flat.z() * Math.sin(angle), 0, flat.z() * Math.cos(angle) - flat.x() * Math.sin(angle));
        double up = power * beast.random().nextFloat() * 0.5;
        target.setVelocity(target.getVelocity().add(turned.x() * TICKS_PER_SECOND, up * TICKS_PER_SECOND, turned.z() * TICKS_PER_SECOND));
    }
}
