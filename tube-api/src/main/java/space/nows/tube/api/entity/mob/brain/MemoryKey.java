package space.nows.tube.api.entity.mob.brain;

import java.util.Objects;

public record MemoryKey<T>(String key, Class<T> type) {

    public MemoryKey {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
    }

    public static <T> MemoryKey<T> of(String key, Class<T> type) {
        return new MemoryKey<>(key, type);
    }
}
