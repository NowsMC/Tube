package space.nows.tube.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Chat {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"]+");
    private static final String TRAILING_PUNCTUATION = ".,;:!?)]}'\"";

    private Chat() {
    }

    public static ChatText text(String text) {
        return ChatText.of(text);
    }

    public static ChatText mini(String markup) {
        return ChatText.of(MINI.deserialize(markup));
    }

    public static ChatText copy(String label, String value) {
        return text(label).color(NamedTextColor.AQUA).hover("Click to copy " + value).copyOnClick(value);
    }

    public static ChatText link(String label, String url) {
        return text(label).color(NamedTextColor.BLUE).underlined().hover("Click to open " + url).openUrl(url);
    }

    public static ChatText command(String label, String command) {
        return text(label).color(NamedTextColor.GREEN).hover("Click to run /" + stripSlash(command)).runCommand(command);
    }

    public static ChatText suggestion(String label, String command) {
        return text(label).color(NamedTextColor.YELLOW).hover("Click to type " + (command.startsWith("/") ? command : "/" + command)).suggestCommand(command);
    }

    public static Component join(Component... parts) {
        Component joined = Component.empty();
        for (Component part : parts) {
            joined = joined.append(part);
        }
        return joined;
    }

    public static Component linkify(String text) {
        Matcher matcher = URL.matcher(text);
        Component result = Component.empty();
        int last = 0;
        while (matcher.find()) {
            String url = trimmed(matcher.group());
            result = result.append(Component.text(text.substring(last, matcher.start())));
            result = result.append(link(url, url).build());
            last = matcher.start() + url.length();
            matcher.region(last, text.length());
        }
        return result.append(Component.text(text.substring(last)));
    }

    private static String trimmed(String url) {
        int end = url.length();
        while (end > 0 && TRAILING_PUNCTUATION.indexOf(url.charAt(end - 1)) >= 0) {
            end--;
        }
        return url.substring(0, end);
    }

    private static String stripSlash(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }
}
