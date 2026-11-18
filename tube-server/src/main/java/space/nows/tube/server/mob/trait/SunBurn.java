package space.nows.tube.server.mob.trait;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sense.Daylight;

public final class SunBurn implements Trait {

    private static final int BURN_TICKS = 8 * 20;

    private final Creature creature;

    public SunBurn(Creature creature) {
        this.creature = creature;
    }

    @Override
    public void tick() {
        Instance instance = creature.getInstance();
        if (creature.isDead() || !Daylight.brightOutside(instance)) {
            return;
        }
        float brightness = Daylight.brightness(instance, creature.getPosition());
        if (brightness <= 0.5f || creature.random().nextFloat() * 30f >= (brightness - 0.4f) * 2f || creature.inWater()) {
            return;
        }
        Point eyes = creature.getPosition().add(0, creature.getEyeHeight(), 0);
        if (!Daylight.seesSky(instance, eyes) || !creature.getEquipment(EquipmentSlot.HELMET).isAir()) {
            return;
        }
        creature.setFireTicks(BURN_TICKS);
    }
}
