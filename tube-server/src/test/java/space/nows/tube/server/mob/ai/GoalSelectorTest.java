package space.nows.tube.server.mob.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GoalSelectorTest {

    private static final class Toggle extends Goal {

        boolean wanted;
        boolean interruptible = true;

        Toggle(Control... controls) {
            super(controls);
        }

        @Override
        public boolean canStart() {
            return wanted;
        }

        @Override
        public boolean interruptible() {
            return interruptible;
        }
    }

    @Test
    void higherPriorityTakesOverSharedControl() {
        GoalSelector selector = new GoalSelector();
        Toggle wander = new Toggle(Control.MOVE);
        Toggle flee = new Toggle(Control.MOVE);
        selector.add(5, wander);
        selector.add(1, flee);
        wander.wanted = true;
        selector.tick();
        assertTrue(selector.running(wander));
        flee.wanted = true;
        selector.tick();
        assertTrue(selector.running(flee));
        assertFalse(selector.running(wander));
    }

    @Test
    void equalPriorityDoesNotInterrupt() {
        GoalSelector selector = new GoalSelector();
        Toggle first = new Toggle(Control.LOOK);
        Toggle second = new Toggle(Control.LOOK);
        selector.add(6, first);
        selector.add(6, second);
        first.wanted = true;
        selector.tick();
        second.wanted = true;
        selector.tick();
        assertTrue(selector.running(first));
        assertFalse(selector.running(second));
    }

    @Test
    void uninterruptibleGoalKeepsItsControl() {
        GoalSelector selector = new GoalSelector();
        Toggle eat = new Toggle(Control.MOVE);
        Toggle flee = new Toggle(Control.MOVE);
        eat.interruptible = false;
        selector.add(5, eat);
        selector.add(1, flee);
        eat.wanted = true;
        selector.tick();
        flee.wanted = true;
        selector.tick();
        assertTrue(selector.running(eat));
        assertFalse(selector.running(flee));
    }

    @Test
    void goalsWithoutSharedControlsRunTogether() {
        GoalSelector selector = new GoalSelector();
        Toggle walk = new Toggle(Control.MOVE);
        Toggle look = new Toggle(Control.LOOK);
        selector.add(5, walk);
        selector.add(6, look);
        walk.wanted = true;
        look.wanted = true;
        selector.tick();
        assertTrue(selector.running(walk));
        assertTrue(selector.running(look));
    }

    @Test
    void stopsWhenItCanNoLongerContinue() {
        GoalSelector selector = new GoalSelector();
        Toggle wander = new Toggle(Control.MOVE);
        selector.add(5, wander);
        wander.wanted = true;
        selector.tick();
        wander.wanted = false;
        selector.tick();
        assertFalse(selector.running(wander));
    }
}
