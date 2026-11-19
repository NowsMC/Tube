package space.nows.tube.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

class ChatTextTest {

    private static String clickValue(Component component) {
        return ((ClickEvent.Payload.Text) component.clickEvent().payload()).value();
    }

    @Test
    void stylesAreAppliedToTheText() {
        Component text = Chat.text("hello").color("gold").bold().italic().underlined().strikethrough().obfuscated().build();

        assertEquals(NamedTextColor.GOLD, text.color());
        for (TextDecoration decoration : TextDecoration.values()) {
            assertEquals(TextDecoration.State.TRUE, text.decoration(decoration));
        }
        assertEquals(TextColor.color(0x12ab34), Chat.text("x").color("#12ab34").build().color());
        assertThrows(IllegalArgumentException.class, () -> Chat.text("x").color("not a colour"));
    }

    @Test
    void copyingRunningAndSuggestingUseTheRightActions() {
        Component copy = Chat.text("id").copyOnClick("abc").build();
        Component run = Chat.text("go").runCommand("/home set").build();
        Component suggest = Chat.text("type").suggestCommand("home").build();
        Component raw = Chat.text("say").suggest("hello").build();

        assertEquals(ClickEvent.Action.COPY_TO_CLIPBOARD, copy.clickEvent().action());
        assertEquals("abc", clickValue(copy));
        assertEquals(ClickEvent.Action.RUN_COMMAND, run.clickEvent().action());
        assertEquals("home set", clickValue(run));
        assertEquals("/home", clickValue(suggest));
        assertEquals("hello", clickValue(raw));
    }

    @Test
    void onlyWebLinksCanBeOpened() {
        Component link = Chat.text("site").openUrl("https://tube.nows.space/").build();

        assertEquals(ClickEvent.Action.OPEN_URL, link.clickEvent().action());
        assertEquals("https://tube.nows.space/", clickValue(link));
        assertThrows(IllegalArgumentException.class, () -> Chat.text("x").openUrl("file:///etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> Chat.text("x").openUrl("javascript:alert(1)"));
        assertThrows(IllegalArgumentException.class, () -> Chat.text("x").openUrl("example.com"));
    }

    @Test
    void hoverShowsTextAndShiftClickInserts() {
        Component text = Chat.text("name").hover("Click me").insertOnShiftClick("name").build();

        assertEquals(HoverEvent.Action.SHOW_TEXT, text.hoverEvent().action());
        assertEquals(Component.text("Click me"), text.hoverEvent().value());
        assertEquals("name", text.insertion());
    }

    @Test
    void miniMessageKeepsItsClickAndHoverTags() {
        Component text = Chat.mini("<click:copy_to_clipboard:secret><hover:show_text:'tip'><red>hi</red></hover></click>").build();

        Component clickable = firstWithClick(text);
        assertEquals(ClickEvent.Action.COPY_TO_CLIPBOARD, clickable.clickEvent().action());
        assertEquals("secret", clickValue(clickable));
        assertEquals(HoverEvent.Action.SHOW_TEXT, clickable.hoverEvent().action());
    }

    private static String plain(Component component) {
        StringBuilder text = new StringBuilder(component instanceof TextComponent literal ? literal.content() : "");
        component.children().forEach(child -> text.append(plain(child)));
        return text.toString();
    }

    private static Component firstWithClick(Component component) {
        if (component.clickEvent() != null) {
            return component;
        }
        for (Component child : component.children()) {
            Component found = firstWithClick(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @Test
    void shortcutsBuildTheUsualButtons() {
        assertEquals(ClickEvent.Action.COPY_TO_CLIPBOARD, Chat.copy("1.2.3.4", "1.2.3.4").build().clickEvent().action());
        assertEquals(ClickEvent.Action.OPEN_URL, Chat.link("docs", "https://nows.space").build().clickEvent().action());
        assertEquals(ClickEvent.Action.RUN_COMMAND, Chat.command("Spawn", "/spawn").build().clickEvent().action());
        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, Chat.suggestion("Home", "home").build().clickEvent().action());
    }

    @Test
    void joinedPartsKeepTheirOwnEvents() {
        Component line = Chat.join(Chat.text("[a]").copyOnClick("a").build(), Component.text(" and "), Chat.text("[b]").copyOnClick("b").build());

        assertEquals(null, line.clickEvent());
        assertEquals("a", clickValue(line.children().get(0)));
        assertEquals(null, line.children().get(1).clickEvent());
        assertEquals("b", clickValue(line.children().get(2)));
    }

    @Test
    void appendedTextInheritsTheClickLikeAdventureDoes() {
        Component line = Chat.text("[a]").copyOnClick("a").then(" tail").build();

        assertEquals("a", clickValue(line));
        assertEquals(" tail", ((TextComponent) line.children().get(0)).content());
    }

    @Test
    void linksInsideTextBecomeClickable() {
        Component line = Chat.linkify("see https://tube.nows.space/docs, then https://nows.space.");

        java.util.List<Component> parts = line.children();
        assertEquals(5, parts.size());
        assertEquals("see ", ((TextComponent) parts.get(0)).content());
        assertEquals("https://tube.nows.space/docs", clickValue(parts.get(1)));
        assertEquals(", then ", ((TextComponent) parts.get(2)).content());
        assertEquals("https://nows.space", clickValue(parts.get(3)));
        assertEquals(".", ((TextComponent) parts.get(4)).content());
    }

    @Test
    void textWithoutLinksIsUntouched() {
        Component line = Chat.linkify("no links here");

        assertEquals("no links here", plain(line));
        assertEquals(null, firstWithClick(line));
    }
}
