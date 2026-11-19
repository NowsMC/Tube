package space.nows.tube.server.mob.kind.animal.llama;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;

final class SpitBack extends Behavior {

    private final Creature llama;
    private int handledAttack = -1;
    private LivingEntity offender;

    SpitBack(Creature llama) {
        super(Control.TARGET);
        this.llama = llama;
    }

    @Override
    public boolean canStart() {
        LivingEntity attacker = llama.harm().attacker(llama.getAliveTicks());
        return attacker != null && llama.harm().attackCount() != handledAttack && !attacker.isDead();
    }

    @Override
    public boolean canContinue() {
        if (llama.brain().recall(Spit.SPAT).isPresent()) {
            llama.brain().forget(Spit.SPAT);
            return false;
        }
        double range = llama.getAttributeValue(Attribute.FOLLOW_RANGE);
        return offender != null && !offender.isDead() && llama.getPosition().distanceSquared(offender.getPosition()) <= range * range;
    }

    @Override
    public void start() {
        offender = llama.harm().attacker(llama.getAliveTicks());
        handledAttack = llama.harm().attackCount();
        llama.brain().forget(Spit.SPAT);
        llama.setTarget(offender);
    }

    @Override
    public void stop() {
        offender = null;
        llama.setTarget(null);
    }
}
