package space.nows.tube.api.entity.brain;

import java.util.Objects;

public record Activity(String key) {

    public static final Activity CORE = new Activity("core");
    public static final Activity IDLE = new Activity("idle");

    public Activity {
        Objects.requireNonNull(key, "key");
    }

    public static Activity of(String key) {
        return new Activity(key);
    }
}
