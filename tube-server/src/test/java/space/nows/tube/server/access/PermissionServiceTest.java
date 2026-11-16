package space.nows.tube.server.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

class PermissionServiceTest {

    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @TempDir
    Path directory;

    @Test
    void operatorsHaveEveryPermission() {
        PermissionService service = new PermissionService(directory);
        assertFalse(service.has(ALICE, "any.thing"));

        service.setOp(ALICE, "Alice", true);

        assertTrue(service.isOp(ALICE));
        assertTrue(service.has(ALICE, "any.thing"));
        assertFalse(service.has(BOB, "any.thing"));
    }

    @Test
    void grantsSupportExactAndWildcardNodes() {
        PermissionService service = new PermissionService(directory);
        service.grant(ALICE, "shop.buy");
        service.grant(BOB, "shop.*");

        assertTrue(service.has(ALICE, "shop.buy"));
        assertFalse(service.has(ALICE, "shop.sell"));
        assertTrue(service.has(BOB, "shop.sell"));
        assertFalse(service.has(BOB, "other.sell"));

        service.revoke(ALICE, "shop.buy");
        assertFalse(service.has(ALICE, "shop.buy"));
    }

    @Test
    void survivesARestart() {
        PermissionService first = new PermissionService(directory);
        first.setOp(ALICE, "Alice", true);
        first.grant(BOB, "shop.buy");

        PermissionService second = new PermissionService(directory);

        assertTrue(second.isOp(ALICE));
        assertTrue(second.has(BOB, "shop.buy"));
        second.setOp(ALICE, "Alice", false);
        assertFalse(new PermissionService(directory).isOp(ALICE));
    }
}
