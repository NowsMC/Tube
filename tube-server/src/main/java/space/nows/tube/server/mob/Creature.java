package space.nows.tube.server.mob;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.entity.metadata.MobMeta;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.mob.brain.CreatureBrain;
import space.nows.tube.server.mob.look.Gaze;
import space.nows.tube.server.mob.move.Liquids;
import space.nows.tube.server.mob.move.Navigation;
import space.nows.tube.server.mob.sense.Harm;
import space.nows.tube.server.mob.sense.Sight;
import space.nows.tube.server.mob.sound.AmbientSound;
import space.nows.tube.server.mob.sound.DeathEffect;
import space.nows.tube.server.mob.trait.Crowding;
import space.nows.tube.server.mob.trait.Despawn;
import space.nows.tube.server.mob.trait.Trait;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class Creature extends EntityCreature {

    private static final double JUMP_SPEED = 0.42 * 20;
    private static final double SWIM_UP_SPEED = 0.04 * 20;
    private static final double MAX_SWIM_UP_SPEED = 0.2 * 20;
    private static final int REMOVAL_DELAY_MILLIS = 1000;

    private final CreatureKind kind;
    private final CreatureBrain brain;
    private final Navigation navigation;
    private final Gaze gaze;
    private final Sight sight;
    private final Harm harm = new Harm();
    private final List<Trait> traits = new ArrayList<>();
    private boolean jumping;
    private boolean persistent;
    private int idleTicks;

    public Creature(CreatureKind kind) {
        super(kind.type());
        this.kind = kind;
        kind.stats().applyTo(this);
        this.navigation = new Navigation(this);
        this.gaze = new Gaze(this, kind.headTurnSpeed(), kind.maxHeadPitch(), kind.maxHeadTurn());
        this.sight = new Sight(this);
        this.brain = new CreatureBrain(getEntityId());
        kind.configure(this, brain);
        traits.add(new Crowding(this));
        traits.add(new Despawn(this));
        if (kind.ambientSounds()) {
            traits.add(new AmbientSound(this));
        }
        setRemovalAnimationDelay(REMOVAL_DELAY_MILLIS);
    }

    public CreatureKind kind() {
        return kind;
    }

    public Navigation navigation() {
        return navigation;
    }

    public Gaze gaze() {
        return gaze;
    }

    public Sight sight() {
        return sight;
    }

    public Harm harm() {
        return harm;
    }

    public CreatureBrain brain() {
        return brain;
    }

    public RandomGenerator random() {
        return ThreadLocalRandom.current();
    }

    public LivingEntity target() {
        return getTarget() instanceof LivingEntity living && !living.isDead() && !living.isRemoved() ? living : null;
    }

    public void addTrait(Trait trait) {
        traits.add(trait);
    }

    public <T extends Trait> Optional<T> trait(Class<T> type) {
        for (Trait trait : traits) {
            if (type.isInstance(trait)) {
                return Optional.of(type.cast(trait));
            }
        }
        return Optional.empty();
    }

    public boolean isBaby() {
        return getEntityMeta() instanceof AgeableMobMeta ageable && ageable.isBaby()
                || getEntityMeta() instanceof ZombieMeta zombie && zombie.isBaby();
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public int idleTicks() {
        return idleTicks;
    }

    public void resetIdle() {
        idleTicks = 0;
    }

    public void jump() {
        jumping = true;
    }

    public void setAggressive(boolean aggressive) {
        if (getEntityMeta() instanceof MobMeta meta) {
            meta.setAggressive(aggressive);
        }
    }

    public void strike(Entity target) {
        attack(target, true);
    }

    public float walkPreference(Point at) {
        return kind.walkPreference(this, at);
    }

    public boolean inWater() {
        Instance instance = getInstance();
        return instance != null && Liquids.isWater(instance.getBlock(getPosition()));
    }

    public boolean inLava() {
        Instance instance = getInstance();
        return instance != null && Liquids.isLava(instance.getBlock(getPosition()));
    }

    public Block blockAt(Point at) {
        Instance instance = getInstance();
        return instance == null ? Block.AIR : instance.getBlock(at);
    }

    @Override
    public void kill() {
        super.kill();
        DeathEffect.play(this);
    }

    @Override
    public boolean damage(Damage damage) {
        if (!kind.acceptsDamage(this, damage)) {
            return false;
        }
        boolean hurt = super.damage(damage);
        if (hurt) {
            harm.record(damage, getAliveTicks());
            idleTicks = 0;
            kind.damaged(this, damage);
        }
        return hurt;
    }

    @Override
    public void update(long time) {
        if (!isDead() && getInstance() != null) {
            think();
        }
        super.update(time);
        if (!isDead() && getInstance() != null) {
            gaze.tick(!navigation.isDone());
            applyJump();
        }
    }

    private void think() {
        sight.forget();
        idleTicks++;
        brain.tick(getAliveTicks());
        for (Trait trait : traits) {
            trait.tick();
        }
    }

    private void applyJump() {
        if (!jumping) {
            return;
        }
        jumping = false;
        Vec velocity = getVelocity();
        if (inWater() || inLava()) {
            setVelocity(velocity.withY(Math.min(velocity.y() + SWIM_UP_SPEED, MAX_SWIM_UP_SPEED)));
        } else if (isOnGround()) {
            setVelocity(velocity.withY(JUMP_SPEED));
        }
    }
}
