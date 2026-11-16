package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class VersionTest {

    @Test
    void usesTheYearMonthCounterScheme() {
        String version = new DedicatedServer(() -> "26.2", Path.of("")).version();
        assertTrue(version.matches("\\d{4}\\.(0?[1-9]|1[0-2])\\.\\d+(-SNAPSHOT)?"), version);
    }
}
