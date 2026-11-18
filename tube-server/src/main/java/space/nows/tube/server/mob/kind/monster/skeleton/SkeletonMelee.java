package space.nows.tube.server.mob.kind.monster.skeleton;

import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.combat.MeleeAttack;

final class SkeletonMelee extends MeleeAttack {

    SkeletonMelee(Creature skeleton) {
        super(skeleton, 1.2, false);
    }

    @Override
    public boolean canStart() {
        return !holdingBow() && super.canStart();
    }

    @Override
    public boolean canContinue() {
        return !holdingBow() && super.canContinue();
    }

    private boolean holdingBow() {
        return creature.getItemInMainHand().material() == Material.BOW || creature.getItemInOffHand().material() == Material.BOW;
    }
}
