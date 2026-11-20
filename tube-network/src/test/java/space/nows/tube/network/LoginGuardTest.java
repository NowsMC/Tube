package space.nows.tube.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void failuresOlderThanAMinuteAreForgotten() throws Exception {
        AtomicLong now = new AtomicLong();
        LoginGuard guard = new LoginGuard(now::get);

        guard.failed(ADDRESS);
        guard.failed(ADDRESS);
        now.set(60_000);
        guard.failed(ADDRESS);
        assertFalse(guard.locked(ADDRESS));

        for (int i = 0; i < 10_000; i++) {
            guard.failed(InetAddress.getByAddress(new byte[] {10, 0, (byte) (i >> 8), (byte) i}));
        }
        now.set(200_000);
        guard.failed(InetAddress.getByAddress(new byte[] {10, 1, 0, 0}));
        assertEquals(1, guard.tracked());
    }

    @Test
    void ipv6AddressesInOneNetworkShareTheirLock() throws Exception {
        LoginGuard guard = new LoginGuard(() -> 0);
        InetAddress first = InetAddress.getByName("2001:db8:1:2::1");
        InetAddress sameNetwork = InetAddress.getByName("2001:db8:1:2:ffff::9");
        InetAddress otherNetwork = InetAddress.getByName("2001:db8:1:3::1");

        guard.failed(first);
        guard.failed(sameNetwork);
        guard.failed(InetAddress.getByName("2001:db8:1:2:abcd::1"));

        assertTrue(guard.locked(first));
        assertTrue(guard.locked(sameNetwork));
        assertFalse(guard.locked(otherNetwork));
    }
}
