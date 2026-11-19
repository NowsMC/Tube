package space.nows.tube.server.mob.trait;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;

import java.util.function.Predicate;

public final class Conversion implements Trait {

    private static final int DELAY = 300;
    private static final int NAUSEA_TICKS = 200;

    private final Creature creature;
    private final EntityType into;
    private final int delay;
    private final Predicate<Creature> when;
    private int exposed;

    public Conversion(Creature creature, EntityType into, int delay, Predicate<Creature> when) {
        this.creature = creature;
        this.into = into;
        this.delay = delay;
        this.when = when;
    }

    public static Conversion zombify(Creature creature, EntityType into) {
        return new Conversion(creature, into, DELAY, mob -> zombifies(mob.getInstance()));
    }

    public static boolean zombifies(Instance instance) {
        return instance != null && !instance.getCachedDimensionType().hasCeiling();
    }

    @Override
    public void tick() {
        if (creature.isDead() || !when.test(creature)) {
            exposed = 0;
            return;
        }
        if (++exposed < delay) {
            return;
        }
        Creature converted = new Creature(CreatureKinds.of(into).orElseThrow());
        converted.setPersistent(creature.isPersistent());
        CreatureKinds.spawn(converted, creature.getInstance(), creature.getPosition());
        converted.addEffect(new Potion(PotionEffect.NAUSEA, 0, NAUSEA_TICKS));
        creature.remove();
    }
}
