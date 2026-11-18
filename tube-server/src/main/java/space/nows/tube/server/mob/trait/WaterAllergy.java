package space.nows.tube.server.mob.trait;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sense.Daylight;

public final class WaterAllergy implements Trait {

    private static final float DAMAGE = 1f;

    private final Creature creature;

    public WaterAllergy(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void tick() {
        if (!creature.isDead() && (creature.inWater() || inRain())) {
            creature.damage(DamageType.DROWN, DAMAGE);
        }
    }

    private boolean inRain() {
        Instance instance = creature.getInstance();
        if (!instance.getWeather().isRaining()) {
            return false;
        }
        Point head = creature.getPosition().add(0, creature.getBoundingBox().height(), 0);
        return Daylight.seesSky(instance, creature.getPosition()) || Daylight.seesSky(instance, head);
    }
}
