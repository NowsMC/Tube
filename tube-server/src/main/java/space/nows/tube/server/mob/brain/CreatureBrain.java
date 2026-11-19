package space.nows.tube.server.mob.brain;

import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.api.entity.brain.MemoryKey;
import space.nows.tube.api.entity.brain.Sense;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class CreatureBrain implements Brain {

    private static final int REMEMBER_FOREVER = -1;

    private final Lane targeting = new Lane();
    private final Lane acting = new Lane();
    private final Memories memories = new Memories();
    private final List<SenseClock> senses = new ArrayList<>();
    private final Set<Control> disabled = EnumSet.noneOf(Control.class);
    private final int offset;
    private long now;

    public CreatureBrain(int offset) {
        this.offset = offset;
    }

    @Override
    public void add(int priority, Behavior behavior) {
        laneOf(behavior).add(priority, behavior);
    }

    @Override
    public void remove(Behavior behavior) {
        laneOf(behavior).removeIf(existing -> existing == behavior);
    }

    @Override
    public boolean running(Behavior behavior) {
        return laneOf(behavior).running(behavior);
    }

    @Override
    public List<Behavior> behaviors() {
        List<Behavior> all = new ArrayList<>();
        targeting.collect(all);
        acting.collect(all);
        return List.copyOf(all);
    }

    @Override
    public void setEnabled(Control control, boolean enabled) {
        if (enabled) {
            disabled.remove(control);
        } else {
            disabled.add(control);
        }
    }

    @Override
    public <T> Optional<T> recall(MemoryKey<T> key) {
        return memories.recall(key);
    }

    @Override
    public <T> void remember(MemoryKey<T> key, T value) {
        memories.remember(key, value, now, REMEMBER_FOREVER);
    }

    @Override
    public <T> void remember(MemoryKey<T> key, T value, int ticks) {
        memories.remember(key, value, now, ticks);
    }

    @Override
    public void forget(MemoryKey<?> key) {
        memories.forget(key);
    }

    @Override
    public void addSense(Sense sense) {
        senses.add(new SenseClock(sense, offset));
    }

    public void tick(long aliveTicks) {
        now = aliveTicks;
        memories.expire(now);
        for (SenseClock sense : senses) {
            sense.tick(this);
        }
        if ((aliveTicks + offset) % 2 != 0 && aliveTicks > 1) {
            targeting.tickRunning(false);
            acting.tickRunning(false);
        } else {
            targeting.tick(disabled);
            acting.tick(disabled);
        }
    }

    private Lane laneOf(Behavior behavior) {
        return behavior.controls().contains(Control.TARGET) ? targeting : acting;
    }
}
