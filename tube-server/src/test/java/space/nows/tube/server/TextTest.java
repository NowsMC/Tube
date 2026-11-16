package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import space.nows.tube.api.Text;

class TextTest {

    @Test
    void miniMessageMarkupIsParsed() {
        assertEquals("hello world", PlainTextComponentSerializer.plainText().serialize(Text.mini("<red>hello</red> <bold>world")));
    }

    @Test
    void plainTextIsNotInterpreted() {
        assertEquals("<red>hi", PlainTextComponentSerializer.plainText().serialize(Text.plain("<red>hi")));
    }
}
