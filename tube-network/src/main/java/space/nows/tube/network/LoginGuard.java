package space.nows.tube.network;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

final class LoginGuard {

    private static final int MAX_FAILURES = 3;
    private static final long LOCK_MILLIS = 60_000;

    private record Failures(int count, long lockedUntil) {
    }

    private final Map<InetAddress, Failures> failures = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    LoginGuard(LongSupplier clock) {
        this.clock = clock;
    }

    boolean locked(InetAddress address) {
        Failures entry = failures.get(address);
        if (entry == null) {
            return false;
        }
        if (entry.lockedUntil() != 0 && entry.lockedUntil() <= clock.getAsLong()) {
            failures.remove(address, entry);
            return false;
        }
        return entry.lockedUntil() != 0;
    }

    void failed(InetAddress address) {
        failures.compute(address, (key, entry) -> {
            int count = entry == null ? 1 : entry.count() + 1;
            return new Failures(count, count >= MAX_FAILURES ? clock.getAsLong() + LOCK_MILLIS : 0);
        });
    }

    void succeeded(InetAddress address) {
        failures.remove(address);
    }
}
