package space.nows.tube.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

class LoginGuardTest {

    private static final InetAddress ADDRESS = InetAddress.getLoopbackAddress();

    @Test
    void anAddressIsLockedAfterThreeFailuresForOneMinute() {
        AtomicLong now = new AtomicLong();
        LoginGuard guard = new LoginGuard(now::get);

        guard.failed(ADDRESS);
        guard.failed(ADDRESS);
        assertFalse(guard.locked(ADDRESS));
        guard.failed(ADDRESS);
        assertTrue(guard.locked(ADDRESS));

        now.set(59_999);
        assertTrue(guard.locked(ADDRESS));
        now.set(60_000);
        assertFalse(guard.locked(ADDRESS));
    }

    @Test
    void aSuccessfulLoginClearsEarlierFailures() {
        LoginGuard guard = new LoginGuard(() -> 0);

        guard.failed(ADDRESS);
        guard.failed(ADDRESS);
        guard.succeeded(ADDRESS);
        guard.failed(ADDRESS);

        assertFalse(guard.locked(ADDRESS));
    }
}
