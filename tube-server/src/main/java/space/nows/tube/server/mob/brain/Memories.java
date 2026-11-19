package space.nows.tube.server.mob.brain;

import space.nows.tube.api.entity.brain.MemoryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class Memories {

    private static final long FOREVER = Long.MAX_VALUE;

    private final Map<MemoryKey<?>, Memory> entries = new HashMap<>();

    <T> Optional<T> recall(MemoryKey<T> key) {
        Memory memory = entries.get(key);
        return memory == null ? Optional.empty() : Optional.of(key.type().cast(memory.value()));
    }

    <T> void remember(MemoryKey<T> key, T value, long now, int ticks) {
        if (value == null) {
            entries.remove(key);
            return;
        }
        entries.put(key, new Memory(key.type().cast(value), ticks < 0 ? FOREVER : now + ticks));
    }

    void forget(MemoryKey<?> key) {
        entries.remove(key);
    }

    void expire(long now) {
        entries.values().removeIf(memory -> memory.expiresAt() <= now);
    }

    private record Memory(Object value, long expiresAt) {
    }
}
