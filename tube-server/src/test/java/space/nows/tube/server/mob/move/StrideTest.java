package space.nows.tube.server.mob.move;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StrideTest {

    @Test
    void walkSettlesAtVanillaSpeed() {
        assertEquals(0.1165, Stride.perTick(0.23, false), 0.0005);
        assertEquals(0.0881, Stride.perTick(0.2, false), 0.0005);
        assertEquals(0.023, Stride.perTick(0.23, true), 0.0005);
    }
}
