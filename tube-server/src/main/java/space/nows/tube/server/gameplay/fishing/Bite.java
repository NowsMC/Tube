package space.nows.tube.server.gameplay.fishing;

import java.util.random.RandomGenerator;

final class Bite {

    enum Phase {
        WAITING, LURING, NIBBLING
    }

    private static final int MIN_WAIT = 100;
    private static final int WAIT_SPREAD = 500;
    private static final int LURE_TICKS_PER_LEVEL = 100;
    private static final int LEAST_WAIT = 25;
    private static final int MIN_LURE = 20;
    private static final int LURE_SPREAD = 60;
    private static final int MIN_NIBBLE = 20;
    private static final int NIBBLE_SPREAD = 20;

    private final RandomGenerator random;
    private final int lureLevel;
    private Phase phase = Phase.WAITING;
    private int ticks;

    Bite(RandomGenerator random, int lureLevel) {
        this.random = random;
        this.lureLevel = lureLevel;
        this.ticks = nextWait();
    }

    Phase phase() {
        return phase;
    }

    boolean nibbling() {
        return phase == Phase.NIBBLING;
    }

    Phase tick() {
        if (--ticks > 0) {
            return null;
        }
        Phase finished = phase;
        switch (phase) {
            case WAITING -> {
                phase = Phase.LURING;
                ticks = MIN_LURE + random.nextInt(LURE_SPREAD);
            }
            case LURING -> {
                phase = Phase.NIBBLING;
                ticks = MIN_NIBBLE + random.nextInt(NIBBLE_SPREAD);
            }
            case NIBBLING -> {
                phase = Phase.WAITING;
                ticks = nextWait();
            }
        }
        return finished;
    }

    private int nextWait() {
        return Math.max(LEAST_WAIT, MIN_WAIT + random.nextInt(WAIT_SPREAD) - lureLevel * LURE_TICKS_PER_LEVEL);
    }
}
