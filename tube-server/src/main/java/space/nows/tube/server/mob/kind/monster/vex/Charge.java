package space.nows.tube.server.mob.kind.monster.vex;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.monster.VexMeta;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;

final class Charge extends Behavior {

    private static final int CHANCE = 7;
    private static final double MIN_DISTANCE = 4;
    private static final double RETARGET_WITHIN = 9;

    private final Creature vex;
    private final Glide glide;
    private boolean charging;

    Charge(Creature vex, Glide glide) {
        super(Control.MOVE);
        this.vex = vex;
        this.glide = glide;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = vex.target();
        return target != null && !glide.busy() && vex.random().nextInt(halved(CHANCE)) == 0
                && vex.getPosition().distanceSquared(target.getPosition()) > MIN_DISTANCE;
    }

    @Override
    public boolean canContinue() {
        return glide.busy() && charging && vex.target() != null;
    }

    @Override
    public void start() {
        LivingEntity target = vex.target();
        if (target != null) {
            glide.moveTo(target.getPosition().add(0, target.getEyeHeight(), 0), 1.0);
        }
        charging(true);
        MobVoice.play(vex, "charge");
    }

    @Override
    public void stop() {
        charging(false);
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = vex.target();
        if (target == null) {
            return;
        }
        if (vex.getBoundingBox().intersectEntity(vex.getPosition(), target)) {
            vex.strike(target);
            charging(false);
        } else if (vex.getPosition().distanceSquared(target.getPosition()) < RETARGET_WITHIN) {
            glide.moveTo(target.getPosition().add(0, target.getEyeHeight(), 0), 1.0);
        }
    }

    private void charging(boolean value) {
        charging = value;
        if (vex.getEntityMeta() instanceof VexMeta meta) {
            meta.setAttacking(value);
        }
    }
}
