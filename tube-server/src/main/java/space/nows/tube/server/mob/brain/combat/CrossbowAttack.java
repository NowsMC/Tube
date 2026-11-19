package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.raider.PillagerMeta;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

public final class CrossbowAttack extends Behavior {

    private static final int CHARGE_TICKS = 25;
    private static final int MIN_DELAY = 20;
    private static final int MIN_REPATH = 20;
    private static final int SIGHT_BEFORE_STANDING = 5;

    private enum State {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY
    }

    private final Creature creature;
    private final double speed;
    private final double radius;
    private final Volley volley;
    private State state = State.UNCHARGED;
    private int seen;
    private int delay;
    private int charging;
    private int repathIn;

    public CrossbowAttack(Creature creature, double speed, float radius, Volley volley) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
        this.speed = speed;
        this.radius = radius;
        this.volley = volley;
    }

    private boolean armed() {
        return creature.getItemInMainHand().material() == Material.CROSSBOW;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = creature.target();
        return target != null && !target.isDead() && armed();
    }

    @Override
    public boolean canContinue() {
        return canStart() || !creature.navigation().isDone() && armed();
    }

    @Override
    public void stop() {
        seen = 0;
        charging(false);
        state = State.UNCHARGED;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = creature.target();
        if (target == null) {
            return;
        }
        boolean visible = creature.sight().canSee(target);
        if (visible != seen > 0) {
            seen = 0;
        }
        seen += visible ? 1 : -1;
        boolean approach = (creature.getPosition().distanceSquared(target.getPosition()) > radius * radius || seen < SIGHT_BEFORE_STANDING)
                && delay == 0;
        if (approach) {
            if (--repathIn <= 0) {
                creature.navigation().moveTo(target, state == State.UNCHARGED ? speed : speed * 0.5);
                repathIn = MIN_REPATH + creature.random().nextInt(MIN_REPATH + 1);
            }
        } else {
            repathIn = 0;
            creature.navigation().stop();
        }
        creature.gaze().lookAt(target, 30f, 30f);
        switch (state) {
            case UNCHARGED -> {
                if (!approach) {
                    state = State.CHARGING;
                    charging = 0;
                    charging(true);
                }
            }
            case CHARGING -> {
                if (++charging >= CHARGE_TICKS) {
                    state = State.CHARGED;
                    delay = MIN_DELAY + creature.random().nextInt(MIN_DELAY);
                    charging(false);
                }
            }
            case CHARGED -> {
                if (--delay == 0) {
                    state = State.READY;
                }
            }
            case READY -> {
                if (visible) {
                    volley.fire(creature, target, 1f);
                    state = State.UNCHARGED;
                }
            }
        }
    }

    private void charging(boolean value) {
        if (creature.getEntityMeta() instanceof PillagerMeta meta) {
            meta.setChargingCrossbow(value);
        }
    }
}
