package space.nows.tube.server.mob.projectile;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;

public interface Impact {

    void hitEntity(Fireball fireball, LivingEntity target);

    void hitBlock(Fireball fireball, Point block, Point before);
}
