package space.nows.tube.server.mob.ai;

final class GoalSlot {

    final int priority;
    final Goal goal;
    private boolean running;

    GoalSlot(int priority, Goal goal) {
        this.priority = priority;
        this.goal = goal;
    }

    boolean running() {
        return running;
    }

    boolean yieldsTo(GoalSlot other) {
        return goal.interruptible() && other.priority < priority;
    }

    void start() {
        if (!running) {
            running = true;
            goal.start();
        }
    }

    void stop() {
        if (running) {
            running = false;
            goal.stop();
        }
    }
}
