package space.nows.tube.network;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

final class LoginGuard {

    private static final int MAX_FAILURES = 3;
    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_TRACKED = 10_000;
    private static final int IPV6_NETWORK_BYTES = 8;

    private record Failures(int count, long last) {
    }

    private final Map<InetAddress, Failures> failures = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    LoginGuard(LongSupplier clock) {
        this.clock = clock;
    }

    boolean locked(InetAddress client) {
        InetAddress address = owner(client);
        Failures entry = failures.get(address);
        if (entry == null) {
            return false;
        }
        if (expired(entry, clock.getAsLong())) {
            failures.remove(address, entry);
            return false;
        }
        return entry.count() >= MAX_FAILURES;
    }

    void failed(InetAddress client) {
        InetAddress address = owner(client);
        long now = clock.getAsLong();
        if (failures.size() >= MAX_TRACKED) {
            failures.values().removeIf(entry -> expired(entry, now));
        }
        failures.compute(address, (key, entry) -> entry == null || expired(entry, now)
                ? new Failures(1, now) : new Failures(entry.count() + 1, now));
    }

    void succeeded(InetAddress client) {
        failures.remove(owner(client));
    }

    int tracked() {
        return failures.size();
    }

    private static InetAddress owner(InetAddress address) {
        if (!(address instanceof Inet6Address)) {
            return address;
        }
        byte[] network = address.getAddress();
        Arrays.fill(network, IPV6_NETWORK_BYTES, network.length, (byte) 0);
        try {
            return InetAddress.getByAddress(network);
        } catch (UnknownHostException e) {
            return address;
        }
    }

    private static boolean expired(Failures entry, long now) {
        return now - entry.last() >= WINDOW_MILLIS;
    }
}
