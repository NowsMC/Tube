package space.nows.tube.server.mob.kind.monster.guardian;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.monster.GuardianMeta;
import net.minestom.server.world.Difficulty;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

final class Beam extends Behavior {

    private static final double TOO_CLOSE = 9;
    private static final int WARMUP = -10;
    private static final byte BEAM_SOUND = 21;
    private static final float BASE_MAGIC = 1;
    private static final float HARD_MAGIC = 2;
    private static final float ELDER_MAGIC = 2;

    private final Creature guardian;
    private final boolean elder;
    private final int duration;
    private int charge;

    Beam(Creature guardian, boolean elder, int duration) {
        super(Control.MOVE, Control.LOOK);
        this.guardian = guardian;
        this.elder = elder;
        this.duration = duration;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = guardian.target();
        return target != null && !target.isDead();
    }

    @Override
    public boolean canContinue() {
        LivingEntity target = guardian.target();
        return canStart() && (elder || guardian.getPosition().distanceSquared(target.getPosition()) > TOO_CLOSE);
    }

    @Override
    public void start() {
        charge = WARMUP;
        guardian.navigation().stop();
        LivingEntity target = guardian.target();
        if (target != null) {
            guardian.gaze().lookAt(target, 90f, 90f);
        }
    }

    @Override
    public void stop() {
        aim(null);
        guardian.setTarget(null);
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = guardian.target();
        if (target == null) {
            return;
        }
        guardian.navigation().stop();
        guardian.gaze().lookAt(target, 90f, 90f);
        if (!guardian.sight().canSee(target)) {
            guardian.setTarget(null);
            return;
        }
        charge++;
        if (charge == 0) {
            aim(target);
            guardian.triggerStatus(BEAM_SOUND);
        } else if (charge >= duration) {
            float magic = BASE_MAGIC + (MinecraftServer.getDifficulty() == Difficulty.HARD ? HARD_MAGIC : 0) + (elder ? ELDER_MAGIC : 0);
            target.damage(new Damage(DamageType.INDIRECT_MAGIC, guardian, guardian, guardian.getPosition(), magic));
            guardian.strike(target);
            guardian.setTarget(null);
        }
    }

    private void aim(LivingEntity target) {
        if (guardian.getEntityMeta() instanceof GuardianMeta meta) {
            meta.setTarget(target);
        }
    }
}
