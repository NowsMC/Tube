package space.nows.tube.server.mob.brain;

import space.nows.tube.api.entity.brain.Behavior;

final class Slot {

    final int priority;
    final Behavior behavior;
    private boolean running;

    Slot(int priority, Behavior behavior) {
        this.priority = priority;
        this.behavior = behavior;
    }

    boolean running() {
        return running;
    }

    boolean yieldsTo(Slot other) {
        return behavior.interruptible() && other.priority < priority;
    }

    void start() {
        if (!running) {
            running = true;
            behavior.start();
        }
    }

    void stop() {
        if (running) {
            running = false;
            behavior.stop();
        }
    }
}
