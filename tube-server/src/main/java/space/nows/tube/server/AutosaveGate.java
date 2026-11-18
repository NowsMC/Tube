package space.nows.tube.server;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AutosaveGate {

    private final AtomicBoolean enabled = new AtomicBoolean(true);

    public boolean enabled() {
        return enabled.get();
    }

    public void set(boolean value) {
        enabled.set(value);
    }
}
