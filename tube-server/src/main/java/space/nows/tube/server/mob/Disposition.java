package space.nows.tube.server.mob;

public enum Disposition {
    HOSTILE,
    PASSIVE,
    NEUTRAL;

    public boolean hostile() {
        return this == HOSTILE;
    }
}
