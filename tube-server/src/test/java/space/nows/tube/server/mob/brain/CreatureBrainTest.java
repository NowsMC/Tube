package space.nows.tube.server.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import space.nows.tube.api.entity.brain.Activity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.api.entity.brain.MemoryKey;
import space.nows.tube.api.entity.brain.Sense;

import java.util.Optional;

class CreatureBrainTest {

    private static final MemoryKey<String> SCENT = MemoryKey.of("test:scent", String.class);

    private static final class Toggle extends Behavior {

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

    private static void think(CreatureBrain brain, long ticks) {
        brain.tick(ticks);
    }

    @Test
    void higherPriorityTakesOverSharedControl() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle wander = new Toggle(Control.MOVE);
        Toggle flee = new Toggle(Control.MOVE);
        brain.add(5, wander);
        brain.add(1, flee);
        wander.wanted = true;
        think(brain, 0);
        assertTrue(brain.running(wander));
        flee.wanted = true;
        think(brain, 2);
        assertTrue(brain.running(flee));
        assertFalse(brain.running(wander));
    }

    @Test
    void equalPriorityDoesNotInterrupt() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle first = new Toggle(Control.LOOK);
        Toggle second = new Toggle(Control.LOOK);
        brain.add(6, first);
        brain.add(6, second);
        first.wanted = true;
        think(brain, 0);
        second.wanted = true;
        think(brain, 2);
        assertTrue(brain.running(first));
        assertFalse(brain.running(second));
    }

    @Test
    void uninterruptibleBehaviorKeepsItsControl() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle eat = new Toggle(Control.MOVE);
        Toggle flee = new Toggle(Control.MOVE);
        eat.interruptible = false;
        brain.add(5, eat);
        brain.add(1, flee);
        eat.wanted = true;
        think(brain, 0);
        flee.wanted = true;
        think(brain, 2);
        assertTrue(brain.running(eat));
        assertFalse(brain.running(flee));
    }

    @Test
    void behaviorsWithoutSharedControlsRunTogether() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle walk = new Toggle(Control.MOVE);
        Toggle look = new Toggle(Control.LOOK);
        brain.add(5, walk);
        brain.add(6, look);
        walk.wanted = true;
        look.wanted = true;
        think(brain, 0);
        assertTrue(brain.running(walk));
        assertTrue(brain.running(look));
    }

    @Test
    void stopsWhenItCanNoLongerContinue() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle wander = new Toggle(Control.MOVE);
        brain.add(5, wander);
        wander.wanted = true;
        think(brain, 0);
        wander.wanted = false;
        think(brain, 2);
        assertFalse(brain.running(wander));
    }

    @Test
    void targetingKeepsItsOwnPriorities() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle hunt = new Toggle(Control.TARGET);
        Toggle walk = new Toggle(Control.MOVE);
        brain.add(1, hunt);
        brain.add(1, walk);
        hunt.wanted = true;
        walk.wanted = true;
        think(brain, 0);
        assertTrue(brain.running(hunt));
        assertTrue(brain.running(walk));
    }

    @Test
    void disabledControlStopsItsBehaviors() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle walk = new Toggle(Control.MOVE);
        brain.add(5, walk);
        walk.wanted = true;
        think(brain, 0);
        brain.setEnabled(Control.MOVE, false);
        think(brain, 2);
        assertFalse(brain.running(walk));
    }

    @Test
    void removedBehaviorStops() {
        CreatureBrain brain = new CreatureBrain(0);
        Toggle walk = new Toggle(Control.MOVE);
        brain.add(5, walk);
        walk.wanted = true;
        think(brain, 0);
        brain.remove(walk);
        assertFalse(brain.running(walk));
        assertTrue(brain.behaviors().isEmpty());
    }

    @Test
    void memoriesExpire() {
        CreatureBrain brain = new CreatureBrain(0);
        think(brain, 10);
        brain.remember(SCENT, "smoke", 5);
        think(brain, 14);
        assertEquals(Optional.of("smoke"), brain.recall(SCENT));
        think(brain, 15);
        assertEquals(Optional.empty(), brain.recall(SCENT));
    }

    @Test
    void memoriesWithoutTimeStay() {
        CreatureBrain brain = new CreatureBrain(0);
        brain.remember(SCENT, "flowers");
        think(brain, 100_000);
        assertEquals(Optional.of("flowers"), brain.recall(SCENT));
        brain.forget(SCENT);
        assertEquals(Optional.empty(), brain.recall(SCENT));
    }

    @Test
    void sensesRunOnTheirInterval() {
        CreatureBrain brain = new CreatureBrain(0);
        int[] runs = new int[1];
        brain.addSense(new Sense() {
            @Override
            public int interval() {
                return 20;
            }

            @Override
            public void sense(Brain brain) {
                runs[0]++;
            }
        });
        for (long tick = 0; tick < 60; tick++) {
            think(brain, tick);
        }
        assertEquals(3, runs[0]);
    }

    @Test
    void activityFollowsItsCondition() {
        CreatureBrain brain = new CreatureBrain(0);
        Activity fight = Activity.of("test:fight");
        brain.addActivity(fight, mind -> mind.recall(SCENT).isPresent());
        think(brain, 0);
        assertEquals(Activity.IDLE, brain.activity());
        brain.remember(SCENT, "blood");
        think(brain, 2);
        assertEquals(fight, brain.activity());
    }

    @Test
    void onlyTheActiveActivityRunsBesideCore() {
        CreatureBrain brain = new CreatureBrain(0);
        Activity fight = Activity.of("test:fight");
        brain.addActivity(fight, mind -> mind.recall(SCENT).isPresent());
        Toggle breathe = new Toggle(Control.JUMP);
        Toggle stroll = new Toggle(Control.MOVE);
        Toggle charge = new Toggle(Control.LOOK);
        brain.add(0, breathe);
        brain.add(Activity.IDLE, 1, stroll);
        brain.add(fight, 1, charge);
        breathe.wanted = true;
        stroll.wanted = true;
        charge.wanted = true;
        think(brain, 0);
        assertTrue(brain.running(breathe));
        assertTrue(brain.running(stroll));
        assertFalse(brain.running(charge));
        brain.remember(SCENT, "blood");
        think(brain, 2);
        assertTrue(brain.running(breathe));
        assertFalse(brain.running(stroll));
        assertTrue(brain.running(charge));
    }
}
