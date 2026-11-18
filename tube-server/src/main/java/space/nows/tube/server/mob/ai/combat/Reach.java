package space.nows.tube.server.mob.ai.combat;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;

public final class Reach {

    private static final double MELEE = Math.sqrt(2.04f) - 0.6f;

    private Reach() {
    }

    public static boolean melee(Entity attacker, Entity target) {
        BoundingBox own = attacker.getBoundingBox();
        BoundingBox other = target.getBoundingBox();
        Pos a = attacker.getPosition();
        Pos b = target.getPosition();
        double minX = a.x() + own.minX() - MELEE;
        double maxX = a.x() + own.maxX() + MELEE;
        double minZ = a.z() + own.minZ() - MELEE;
        double maxZ = a.z() + own.maxZ() + MELEE;
        double minY = a.y() + own.minY();
        double maxY = a.y() + own.maxY();
        return b.x() + other.minX() < maxX && b.x() + other.maxX() > minX
                && b.y() + other.minY() < maxY && b.y() + other.maxY() > minY
                && b.z() + other.minZ() < maxZ && b.z() + other.maxZ() > minZ;
    }
}
