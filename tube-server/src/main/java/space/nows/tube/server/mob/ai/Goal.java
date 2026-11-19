package space.nows.tube.server.mob.ai;

import space.nows.tube.api.entity.brain.Behavior;

import java.util.Arrays;

public abstract class Goal extends Behavior {

    protected Goal(Control... controls) {
        super(Arrays.stream(controls)
                .map(control -> space.nows.tube.api.entity.brain.Control.valueOf(control.name()))
                .toArray(space.nows.tube.api.entity.brain.Control[]::new));
    }
}
