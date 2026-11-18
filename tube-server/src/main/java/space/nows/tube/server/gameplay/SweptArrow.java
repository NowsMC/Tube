package space.nows.tube.server.gameplay;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

public final class SweptArrow extends SweptProjectile {

    public SweptArrow(Entity shooter) {
        super(shooter, EntityType.ARROW);
    }
}
