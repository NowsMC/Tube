package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;

final class Ram extends Behavior {

    private static final int MIN_COOLDOWN = 600;
    private static final int MAX_COOLDOWN = 6000;
    private static final double MIN_RANGE = 4;
    private static final double MAX_RANGE = 7;
    private static final double CHARGE_SPEED = 3.0;
    private static final int MAX_CHARGE = 40;
    private static final double TOSS = 2.5 * 20;
    private static final float DAMAGE = 2;

    private final Creature goat;
    private LivingEntity mark;
    private int readyAt;
    private int charging;

    Ram(Creature goat) {
        super(Control.MOVE, Control.LOOK);
        this.goat = goat;
        this.readyAt = MIN_COOLDOWN + goat.random().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
    }

    @Override
    public boolean canStart() {
        if (goat.getAliveTicks() < readyAt || goat.isBaby()) {
            return false;
        }
        mark = null;
        for (Entity entity : goat.getInstance().getNearbyEntities(goat.getPosition(), MAX_RANGE)) {
            double distance = entity.getPosition().distance(goat.getPosition());
            if (entity != goat && entity instanceof LivingEntity living && !living.isDead() && distance >= MIN_RANGE
                    && entity.getEntityType() != goat.getEntityType()
                    && !(entity instanceof Player player && (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR))
                    && goat.sight().canSee(living)) {
                mark = living;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinue() {
        return mark != null && !mark.isDead() && charging < MAX_CHARGE;
    }

    @Override
    public void start() {
        charging = 0;
        MobVoice.play(goat, "prepare_ram");
    }

    @Override
    public void stop() {
        mark = null;
        readyAt = (int) goat.getAliveTicks() + MIN_COOLDOWN + goat.random().nextInt(MAX_COOLDOWN - MIN_COOLDOWN);
        goat.navigation().stop();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        charging++;
        goat.gaze().lookAt(mark, 30f, 30f);
        goat.navigation().moveTo(mark, CHARGE_SPEED);
        if (goat.getBoundingBox().expand(0.5, 0, 0.5).intersectEntity(goat.getPosition(), mark)) {
            mark.damage(Damage.fromEntity(goat, DAMAGE));
            Vec push = mark.getPosition().sub(goat.getPosition()).asVec().withY(0).normalize();
            mark.setVelocity(mark.getVelocity().add(push.x() * TOSS * 0.2, 0.4 * 20, push.z() * TOSS * 0.2));
            MobVoice.play(goat, "ram_impact");
            mark = null;
        }
    }
}
