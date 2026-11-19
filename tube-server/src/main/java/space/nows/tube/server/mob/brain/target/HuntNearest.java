package space.nows.tube.server.mob.brain.target;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import space.nows.tube.server.mob.Creature;

import java.util.function.Predicate;

public final class HuntNearest extends TargetBehavior {

    private static final int DEFAULT_INTERVAL = 10;

    private final Predicate<Entity> kind;
    private final boolean players;
    private final int interval;
    private final Predicate<LivingEntity> condition;
    private Predicate<Creature> when = creature -> true;
    private LivingEntity found;

    private HuntNearest(Creature creature, Predicate<Entity> kind, boolean players, int interval, boolean mustSee, Predicate<LivingEntity> condition) {
        super(creature, mustSee);
        this.kind = kind;
        this.players = players;
        this.interval = halved(interval);
        this.condition = condition;
    }

    public static HuntNearest players(Creature creature, boolean mustSee) {
        return new HuntNearest(creature, entity -> entity instanceof Player, true, DEFAULT_INTERVAL, mustSee, entity -> true);
    }

    public static HuntNearest players(Creature creature, boolean mustSee, Predicate<LivingEntity> condition) {
        return new HuntNearest(creature, entity -> entity instanceof Player, true, DEFAULT_INTERVAL, mustSee, condition);
    }

    public static HuntNearest of(Creature creature, EntityType type, boolean mustSee) {
        return new HuntNearest(creature, entity -> entity.getEntityType() == type, false, DEFAULT_INTERVAL, mustSee, entity -> true);
    }

    public static HuntNearest of(Creature creature, Predicate<Entity> kind, int interval, boolean mustSee, Predicate<LivingEntity> condition) {
        return new HuntNearest(creature, kind, false, interval, mustSee, condition);
    }

    public HuntNearest when(Predicate<Creature> condition) {
        this.when = condition;
        return this;
    }

    @Override
    public boolean canStart() {
        if (!when.test(creature)) {
            return false;
        }
        if (interval > 0 && creature.random().nextInt(interval) != 0) {
            return false;
        }
        double range = followRange();
        Targeting targeting = Targeting.combat(range).only(condition);
        found = players ? targeting.nearestPlayer(creature) : targeting.nearest(creature, range, range, kind);
        return found != null;
    }

    @Override
    public void start() {
        creature.setTarget(found);
        super.start();
    }
}
