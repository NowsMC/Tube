package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;

import java.util.Set;

public final class Raiders {

    private static final EntityType[] TYPES = {EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER, EntityType.ILLUSIONER,
            EntityType.RAVAGER, EntityType.WITCH};
    private static final Set<EntityType> SET = Set.of(TYPES);

    private Raiders() {
    }

    public static EntityType[] types() {
        return TYPES.clone();
    }

    public static boolean is(Entity entity) {
        return SET.contains(entity.getEntityType());
    }
}
