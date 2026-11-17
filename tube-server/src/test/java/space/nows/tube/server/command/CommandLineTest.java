package space.nows.tube.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

class CommandLineTest {

    @Test
    void labelOnlyHasNoArguments() {
        CommandLine line = CommandLine.parse("/give");
        assertEquals("give", line.label());
        assertEquals(List.of(), line.arguments());
    }

    @Test
    void trailingSpaceStartsAnEmptyArgument() {
        CommandLine line = CommandLine.parse("give ");
        assertEquals(List.of(""), line.arguments());
        assertEquals(5, line.currentStart());
    }

    @Test
    void currentArgumentStartsAfterTheLastSpace() {
        CommandLine line = CommandLine.parse("/give Steve dia");
        assertEquals(List.of("Steve", "dia"), line.arguments());
        assertEquals("dia", line.current());
        assertEquals(12, line.currentStart());
    }
}
