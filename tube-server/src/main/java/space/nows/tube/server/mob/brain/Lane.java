package space.nows.tube.server.mob.brain;

import space.nows.tube.api.entity.brain.Activity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

final class Lane {

    private final List<Slot> slots = new ArrayList<>();
    private final Map<Control, Slot> holders = new EnumMap<>(Control.class);

    void add(Activity activity, int priority, Behavior behavior) {
        slots.add(new Slot(activity, priority, behavior));
    }

    void removeIf(Predicate<Behavior> match) {
        for (Slot slot : slots) {
            if (match.test(slot.behavior)) {
                slot.stop();
            }
        }
        slots.removeIf(slot -> match.test(slot.behavior));
        holders.values().removeIf(slot -> !slot.running());
    }

    boolean running(Behavior behavior) {
        for (Slot slot : slots) {
            if (slot.behavior == behavior) {
                return slot.running();
            }
        }
        return false;
    }

    void collect(List<Behavior> into) {
        for (Slot slot : slots) {
            into.add(slot.behavior);
        }
    }

    void tick(Set<Control> disabled, Predicate<Activity> active) {
        for (Slot slot : slots) {
            if (slot.running() && (blocked(slot, disabled, active) || !slot.behavior.canContinue())) {
                slot.stop();
            }
        }
        holders.values().removeIf(slot -> !slot.running());
        for (Slot slot : slots) {
            if (!slot.running() && !blocked(slot, disabled, active) && canTakeOver(slot) && slot.behavior.canStart()) {
                for (Control control : slot.behavior.controls()) {
                    Slot holder = holders.put(control, slot);
                    if (holder != null) {
                        holder.stop();
                    }
                }
                slot.start();
            }
        }
        tickRunning(true);
    }

    void tickRunning(boolean all) {
        for (Slot slot : slots) {
            if (slot.running() && (all || slot.behavior.everyTick())) {
                slot.behavior.tick();
            }
        }
    }

    private static boolean blocked(Slot slot, Set<Control> disabled, Predicate<Activity> active) {
        return !active.test(slot.activity) || uses(slot, disabled);
    }

    private static boolean uses(Slot slot, Set<Control> controls) {
        for (Control control : slot.behavior.controls()) {
            if (controls.contains(control)) {
                return true;
            }
        }
        return false;
    }

    private boolean canTakeOver(Slot slot) {
        for (Control control : slot.behavior.controls()) {
            Slot holder = holders.get(control);
            if (holder != null && !holder.yieldsTo(slot)) {
                return false;
            }
        }
        return true;
    }
}
