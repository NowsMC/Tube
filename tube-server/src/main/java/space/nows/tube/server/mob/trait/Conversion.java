package space.nows.tube.server.mob.trait;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;

public final class Conversion implements Trait {

    private static final int DELAY = 300;
    private static final int NAUSEA_TICKS = 200;

    private final Creature creature;
    private final EntityType into;
    private int exposed;

    public Conversion(Creature creature, EntityType into) {
        this.creature = creature;
        this.into = into;
    }

    public static boolean zombifies(Instance instance) {
        return instance != null && !instance.getCachedDimensionType().hasCeiling();
    }

    @Override
    public void tick() {
        if (creature.isDead() || !zombifies(creature.getInstance())) {
            exposed = 0;
            return;
        }
        if (++exposed < DELAY) {
            return;
        }
        Creature converted = new Creature(CreatureKinds.of(into).orElseThrow());
        converted.setPersistent(creature.isPersistent());
        CreatureKinds.spawn(converted, creature.getInstance(), creature.getPosition());
        converted.addEffect(new Potion(PotionEffect.NAUSEA, 0, NAUSEA_TICKS));
        creature.remove();
    }
}
