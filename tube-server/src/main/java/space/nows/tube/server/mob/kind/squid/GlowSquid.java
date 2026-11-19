package space.nows.tube.server.mob.kind.squid;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.water.GlowSquidMeta;
import space.nows.tube.server.mob.Creature;

public final class GlowSquid extends Squid {

    private static final int DARK_TICKS = 100;

    @Override
    public EntityType type() {
        return EntityType.GLOW_SQUID;
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        super.damaged(creature, damage);
        if (creature.getEntityMeta() instanceof GlowSquidMeta meta) {
            meta.setDarkTicksRemaining(DARK_TICKS);
        }
    }
}
