package space.nows.tube.server.mob.ai;

import space.nows.tube.api.entity.brain.Brain;

public final class GoalSelector {

    private final Brain brain;

    public GoalSelector(Brain brain) {
        this.brain = brain;
    }

    public void add(int priority, Goal goal) {
        brain.add(priority, goal);
    }
}
