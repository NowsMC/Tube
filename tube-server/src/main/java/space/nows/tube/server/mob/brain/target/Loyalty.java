package space.nows.tube.server.mob.brain.target;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.life.Tameness;
import space.nows.tube.server.mob.sense.Assault;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class Loyalty extends TargetBehavior {

    private final Tameness tameness;
    private final Function<LivingEntity, LivingEntity> pick;
    private final ToIntFunction<LivingEntity> count;
    private int handled = -1;
    private LivingEntity found;

    private Loyalty(Creature pet, Tameness tameness, Function<LivingEntity, LivingEntity> pick, ToIntFunction<LivingEntity> count) {
        super(pet, false);
        this.tameness = tameness;
        this.pick = pick;
        this.count = count;
    }

    public static Loyalty defendOwner(Creature pet, Tameness tameness) {
        return new Loyalty(pet, tameness, Assault::lastStruckBy, Assault::struckByCount);
    }

    public static Loyalty joinOwner(Creature pet, Tameness tameness) {
        return new Loyalty(pet, tameness, Assault::lastStruck, Assault::struckCount);
    }

    @Override
    public boolean canStart() {
        Player owner = tameness.owner();
        if (owner == null || tameness.sitting() || count.applyAsInt(owner) == handled) {
            return false;
        }
        found = pick.apply(owner);
        return found != null && found != creature && wanted(found, owner);
    }

    private boolean wanted(LivingEntity target, Player owner) {
        if (target.getEntityType() == EntityType.CREEPER || target.getEntityType() == EntityType.GHAST) {
            return false;
        }
        return !(target instanceof Creature other && other.trait(Tameness.class).map(t -> t.ownedBy(owner)).orElse(false));
    }

    @Override
    public void start() {
        Player owner = tameness.owner();
        if (owner != null) {
            handled = count.applyAsInt(owner);
        }
        creature.setTarget(found);
        remembered = found;
        super.start();
    }
}
