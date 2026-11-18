package space.nows.tube.server.command.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

class SelectorTest {

    private static final List<String> BASES = List.of("@a", "@e", "@p", "@r", "@s");
    private static final List<String> TYPES = List.of("minecraft:cow", "minecraft:zombie");

    @Test
    void opensTheBracketAfterABareSelector() {
        assertEquals(List.of("@e["), Selector.suggest("@e", BASES, TYPES));
    }

    @Test
    void offersOptionKeysInsideTheBracket() {
        assertEquals(List.of("@e[distance=", "@e[limit=", "@e[name=", "@e[sort=", "@e[type="), Selector.suggest("@e[", BASES, TYPES));
        assertEquals(List.of("@e[type="), Selector.suggest("@e[ty", BASES, TYPES));
    }

    @Test
    void offersValuesForTypeAndKeepsEarlierOptions() {
        assertEquals(List.of("@e[limit=1,type=minecraft:zombie"), Selector.suggest("@e[limit=1,type=zo", BASES, TYPES));
        assertEquals(List.of("@e[type=!minecraft:cow"), Selector.suggest("@e[type=!co", BASES, TYPES));
    }

    @Test
    void offersSortValues() {
        assertEquals(List.of("@e[sort=nearest"), Selector.suggest("@e[sort=ne", BASES, TYPES));
    }

    @Test
    void rejectsUnknownOptionsAndUnclosedBrackets() {
        assertThrows(CommandFailure.class, () -> Selector.parse("@e[colour=red]"));
        assertThrows(CommandFailure.class, () -> Selector.parse("@e[type=cow"));
    }
}
