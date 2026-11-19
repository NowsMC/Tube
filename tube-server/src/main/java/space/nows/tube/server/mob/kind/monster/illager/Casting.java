package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;

final class Casting extends Behavior {

    private final Creature caster;
    private final Spellbook book;

    Casting(Creature caster, Spellbook book) {
        super(Control.MOVE, Control.LOOK);
        this.caster = caster;
        this.book = book;
    }

    @Override
    public boolean canStart() {
        return book.casting();
    }

    @Override
    public void start() {
        caster.navigation().stop();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = caster.target();
        if (target != null) {
            caster.gaze().lookAt(target, 10f, 10f);
        }
    }
}
