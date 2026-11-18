package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sense.Daylight;
import space.nows.tube.server.mob.trait.Trait;

final class DaylightTeleport implements Trait {

    private static final long CALM_TICKS = 600;

    private final Creature enderman;
    private final Aggravation aggravation;

    DaylightTeleport(Creature enderman, Aggravation aggravation) {
        this.enderman = enderman;
        this.aggravation = aggravation;
    }

    @Override
    public void tick() {
        Instance instance = enderman.getInstance();
        if (enderman.isDead() || !Daylight.brightOutside(instance) || enderman.getAliveTicks() < aggravation.changedAt() + CALM_TICKS) {
            return;
        }
        float brightness = Daylight.brightness(instance, enderman.getPosition());
        if (brightness > 0.5f && Daylight.seesSky(instance, enderman.getPosition())
                && enderman.random().nextFloat() * 30f < (brightness - 0.4f) * 2f) {
            enderman.setTarget(null);
            EnderTeleport.randomly(enderman);
        }
    }
}
