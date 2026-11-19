package space.nows.tube.server.mob.kind.monster.skeleton;

import net.minestom.server.entity.EntityType;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

public final class Stray extends Skeleton {

    @Override
    public EntityType type() {
        return EntityType.STRAY;
    }

    @Override
    protected Potion arrowEffect() {
        return new Potion(PotionEffect.SLOWNESS, 0, 600);
    }
}
