package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Strafing;

public final class BowAttack extends Behavior {

    private static final int FULL_DRAW = 20;
    private static final int LOST_SIGHT_LIMIT = -60;
    private static final float STRAFE = 0.5f;

    private final Creature creature;
    private final double speed;
    private final float radiusSquared;
    private final Volley volley;
    private int minInterval;
    private int cooldown = -1;
    private int seeTime;
    private int drawTicks = -1;
    private int strafeTime = -1;
    private boolean clockwise;
    private boolean backwards;

    public BowAttack(Creature creature, double speed, int minInterval, float radius, Volley volley) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
        this.speed = speed;
        this.minInterval = minInterval;
        this.radiusSquared = radius * radius;
        this.volley = volley;
    }

    public void setMinInterval(int ticks) {
        this.minInterval = ticks;
    }

    private boolean holdingBow() {
        return creature.getItemInMainHand().material() == Material.BOW || creature.getItemInOffHand().material() == Material.BOW;
    }

    @Override
    public boolean canStart() {
        return creature.target() != null && holdingBow();
    }

    @Override
    public boolean canContinue() {
        return (canStart() || !creature.navigation().isDone()) && holdingBow();
    }

    @Override
    public void start() {
        creature.setAggressive(true);
    }

    @Override
    public void stop() {
        creature.setAggressive(false);
        seeTime = 0;
        cooldown = -1;
        drawTicks = -1;
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
        double distance = creature.getPosition().distanceSquared(target.getPosition());
        boolean sees = creature.sight().canSee(target);
        if (sees != seeTime > 0) {
            seeTime = 0;
        }
        seeTime += sees ? 1 : -1;
        if (distance <= radiusSquared && seeTime >= 20) {
            creature.navigation().stop();
            strafeTime++;
        } else {
            creature.navigation().moveTo(target, speed);
            strafeTime = -1;
        }
        if (strafeTime >= 20) {
            if (creature.random().nextFloat() < 0.3) {
                clockwise = !clockwise;
            }
            if (creature.random().nextFloat() < 0.3) {
                backwards = !backwards;
            }
            strafeTime = 0;
        }
        if (strafeTime > -1) {
            if (distance > radiusSquared * 0.75f) {
                backwards = false;
            } else if (distance < radiusSquared * 0.25f) {
                backwards = true;
            }
            Strafing.step(creature, backwards ? -STRAFE : STRAFE, clockwise ? STRAFE : -STRAFE);
        }
        creature.gaze().lookAt(target, 30f, 30f);
        if (drawTicks >= 0) {
            if (!sees && seeTime < LOST_SIGHT_LIMIT) {
                drawTicks = -1;
            } else if (sees && ++drawTicks >= FULL_DRAW) {
                drawTicks = -1;
                volley.fire(creature, target, 1.0f);
                cooldown = minInterval;
            }
        } else if (--cooldown <= 0 && seeTime >= LOST_SIGHT_LIMIT) {
            drawTicks = 0;
        }
    }
}
