package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

public class MeleeAttack extends Behavior {

    private static final long START_CHECK_COOLDOWN = 20;
    private static final int DEFAULT_INTERVAL = 20;

    protected final Creature creature;
    private final double speed;
    private final boolean persistent;
    private final int interval;
    private Point pathedTo;
    private int repathIn;
    private int attackIn;
    private long lastStartCheck = -START_CHECK_COOLDOWN;

    public MeleeAttack(Creature creature, double speed, boolean persistent) {
        this(creature, speed, persistent, DEFAULT_INTERVAL);
    }

    public MeleeAttack(Creature creature, double speed, boolean persistent, int interval) {
        super(Control.MOVE, Control.LOOK);
        this.creature = creature;
        this.speed = speed;
        this.persistent = persistent;
        this.interval = interval;
    }

    @Override
    public boolean canStart() {
        long now = creature.getAliveTicks();
        if (now - lastStartCheck < START_CHECK_COOLDOWN) {
            return false;
        }
        lastStartCheck = now;
        return creature.target() != null;
    }

    @Override
    public boolean canContinue() {
        LivingEntity target = creature.target();
        if (target == null) {
            return false;
        }
        if (!persistent) {
            return !creature.navigation().isDone();
        }
        return !(target instanceof Player player && (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE));
    }

    @Override
    public void start() {
        LivingEntity target = creature.target();
        if (target != null) {
            creature.navigation().moveTo(target, speed);
        }
        creature.setAggressive(true);
        repathIn = 0;
        attackIn = 0;
        pathedTo = null;
    }

    @Override
    public void stop() {
        LivingEntity target = creature.target();
        if (target instanceof Player player && (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE)) {
            creature.setTarget(null);
        }
        creature.setAggressive(false);
        creature.navigation().stop();
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
        creature.gaze().lookAt(target, 30f, 30f);
        repathIn = Math.max(repathIn - 1, 0);
        if ((persistent || creature.sight().canSee(target)) && repathIn <= 0 && targetMoved(target)) {
            pathedTo = target.getPosition();
            repathIn = 4 + creature.random().nextInt(7);
            double distance = creature.getPosition().distanceSquared(target.getPosition());
            if (distance > 1024) {
                repathIn += 10;
            } else if (distance > 256) {
                repathIn += 5;
            }
            if (!creature.navigation().moveTo(target, speed)) {
                repathIn += 15;
            }
            repathIn = ticks(repathIn);
        }
        attackIn = Math.max(attackIn - 1, 0);
        if (attackIn <= 0 && Reach.melee(creature, target) && creature.sight().canSee(target)) {
            attackIn = ticks(interval);
            creature.strike(target);
        }
    }

    protected final int attackIn() {
        return attackIn;
    }

    protected final int attackInterval() {
        return ticks(interval);
    }

    private boolean targetMoved(LivingEntity target) {
        return pathedTo == null || target.getPosition().distanceSquared(pathedTo) >= 1.0 || creature.random().nextFloat() < 0.05f;
    }
}
