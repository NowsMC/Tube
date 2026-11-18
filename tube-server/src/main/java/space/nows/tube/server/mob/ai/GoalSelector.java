package space.nows.tube.server.mob.ai;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class GoalSelector {

    private final List<GoalSlot> slots = new ArrayList<>();
    private final Map<Control, GoalSlot> holders = new EnumMap<>(Control.class);
    private final Set<Control> disabled = EnumSet.noneOf(Control.class);

    public void add(int priority, Goal goal) {
        slots.add(new GoalSlot(priority, goal));
    }

    public void removeIf(Predicate<Goal> match) {
        for (GoalSlot slot : slots) {
            if (match.test(slot.goal)) {
                slot.stop();
            }
        }
        slots.removeIf(slot -> match.test(slot.goal));
        holders.values().removeIf(slot -> !slot.running());
    }

    public boolean running(Goal goal) {
        for (GoalSlot slot : slots) {
            if (slot.goal == goal) {
                return slot.running();
            }
        }
        return false;
    }

    public void setEnabled(Control control, boolean enabled) {
        if (enabled) {
            disabled.remove(control);
        } else {
            disabled.add(control);
        }
    }

    public void tick() {
        for (GoalSlot slot : slots) {
            if (slot.running() && (usesDisabled(slot) || !slot.goal.canContinue())) {
                slot.stop();
            }
        }
        holders.values().removeIf(slot -> !slot.running());
        for (GoalSlot slot : slots) {
            if (!slot.running() && !usesDisabled(slot) && canTakeOver(slot) && slot.goal.canStart()) {
                for (Control control : slot.goal.controls()) {
                    GoalSlot holder = holders.put(control, slot);
                    if (holder != null) {
                        holder.stop();
                    }
                }
                slot.start();
            }
        }
        tickRunning(true);
    }

    public void tickRunning(boolean all) {
        for (GoalSlot slot : slots) {
            if (slot.running() && (all || slot.goal.everyTick())) {
                slot.goal.tick();
            }
        }
    }

    private boolean usesDisabled(GoalSlot slot) {
        for (Control control : slot.goal.controls()) {
            if (disabled.contains(control)) {
                return true;
            }
        }
        return false;
    }

    private boolean canTakeOver(GoalSlot slot) {
        for (Control control : slot.goal.controls()) {
            GoalSlot holder = holders.get(control);
            if (holder != null && !holder.yieldsTo(slot)) {
                return false;
            }
        }
        return true;
    }
}
