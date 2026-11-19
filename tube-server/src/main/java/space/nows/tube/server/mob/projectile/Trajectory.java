package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;

record Trajectory(LivingEntity entity, Point block, Point before) {

    private static final double STEP = 0.25;

    static Trajectory sweep(Entity self, Entity owner, Vec motion) {
        Instance instance = self.getInstance();
        Pos from = self.getPosition();
        int steps = Math.max(1, (int) Math.ceil(motion.length() / STEP));
        Point before = from;
        for (int i = 1; i <= steps; i++) {
            Point at = from.add(motion.mul((double) i / steps));
            LivingEntity struck = struck(self, owner, instance, at);
            if (struck != null) {
                return new Trajectory(struck, null, before);
            }
            if (!instance.isChunkLoaded(at) || instance.getBlock(at).solid()) {
                return new Trajectory(null, at, before);
            }
            before = at;
        }
        return new Trajectory(null, null, before);
    }

    private static LivingEntity struck(Entity self, Entity owner, Instance instance, Point at) {
        double reach = self.getBoundingBox().width() / 2;
        for (Entity entity : instance.getNearbyEntities(at, 2)) {
            if (entity == self || entity == owner || !(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }
            double half = entity.getBoundingBox().width() / 2 + reach;
            Pos p = entity.getPosition();
            if (Math.abs(at.x() - p.x()) <= half && Math.abs(at.z() - p.z()) <= half
                    && at.y() >= p.y() - reach && at.y() <= p.y() + entity.getBoundingBox().height() + reach) {
                return living;
            }
        }
        return null;
    }
}
