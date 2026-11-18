package space.nows.tube.server.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReleaseNumberTest {

    @Test
    void comparesEachNumber() {
        assertTrue(ReleaseNumber.atLeast("0.2611.2", "0.2611"));
        assertTrue(ReleaseNumber.atLeast("0.2611.2-SNAPSHOT", "0.2611.2"));
        assertTrue(ReleaseNumber.atLeast("1.2601.0", "0.2612"));
        assertFalse(ReleaseNumber.atLeast("0.2611.2", "0.2612"));
        assertFalse(ReleaseNumber.atLeast("0.2611.2", "0.2611.3"));
    }
}
