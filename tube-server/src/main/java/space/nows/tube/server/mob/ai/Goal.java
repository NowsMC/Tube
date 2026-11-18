package space.nows.tube.server.mob.ai;

import java.util.EnumSet;
import java.util.Set;

public abstract class Goal {

    private final Set<Control> controls;

    protected Goal(Control... controls) {
        this.controls = controls.length == 0 ? EnumSet.noneOf(Control.class) : EnumSet.of(controls[0], controls);
    }

    public abstract boolean canStart();

    public boolean canContinue() {
        return canStart();
    }

    public boolean interruptible() {
        return true;
    }

    public boolean everyTick() {
        return false;
    }

    public void start() {
    }

    public void stop() {
    }

    public void tick() {
    }

    public final Set<Control> controls() {
        return controls;
    }

    protected final int ticks(int ticks) {
        return everyTick() ? ticks : halved(ticks);
    }

    protected static int halved(int ticks) {
        return Math.floorDiv(ticks + 1, 2);
    }
}
