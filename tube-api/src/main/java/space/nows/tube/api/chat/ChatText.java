package space.nows.tube.api.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

public record ChatText(Component component) {

    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    public ChatText {
        Objects.requireNonNull(component, "component");
    }

    public static ChatText of(String text) {
        return new ChatText(Component.text(text));
    }

    public static ChatText of(Component component) {
        return new ChatText(component);
    }

    public ChatText color(TextColor color) {
        return new ChatText(component.color(color));
    }

    public ChatText color(String hexOrName) {
        TextColor color = hexOrName.startsWith("#") ? TextColor.fromHexString(hexOrName) : net.kyori.adventure.text.format.NamedTextColor.NAMES.value(hexOrName.toLowerCase(Locale.ROOT));
        if (color == null) {
            throw new IllegalArgumentException("Unknown colour " + hexOrName);
        }
        return color(color);
    }

    public ChatText bold() {
        return decorate(TextDecoration.BOLD);
    }

    public ChatText italic() {
        return decorate(TextDecoration.ITALIC);
    }

    public ChatText underlined() {
        return decorate(TextDecoration.UNDERLINED);
    }

    public ChatText strikethrough() {
        return decorate(TextDecoration.STRIKETHROUGH);
    }

    public ChatText obfuscated() {
        return decorate(TextDecoration.OBFUSCATED);
    }

    public ChatText hover(String text) {
        return hover(Component.text(text));
    }

    public ChatText hover(Component text) {
        return new ChatText(component.hoverEvent(HoverEvent.showText(text)));
    }

    public ChatText copyOnClick(String value) {
        return new ChatText(component.clickEvent(ClickEvent.copyToClipboard(value)));
    }

    public ChatText openUrl(String url) {
        URI uri = URI.create(url);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals(SCHEME_HTTP) && !scheme.equals(SCHEME_HTTPS)) {
            throw new IllegalArgumentException("Only http and https links can be opened: " + url);
        }
        return new ChatText(component.clickEvent(ClickEvent.openUrl(url)));
    }

    public ChatText runCommand(String command) {
        return new ChatText(component.clickEvent(ClickEvent.runCommand(withoutSlash(command))));
    }

    public ChatText suggestCommand(String command) {
        return new ChatText(component.clickEvent(ClickEvent.suggestCommand(command.startsWith("/") ? command : "/" + command)));
    }

    public ChatText suggest(String text) {
        return new ChatText(component.clickEvent(ClickEvent.suggestCommand(text)));
    }

    public ChatText insertOnShiftClick(String text) {
        return new ChatText(component.insertion(text));
    }

    public ChatText then(ChatText next) {
        return then(next.component);
    }

    public ChatText then(Component next) {
        return new ChatText(component.append(next));
    }

    public ChatText then(String text) {
        return then(Component.text(text));
    }

    public ChatText newline() {
        return then(Component.newline());
    }

    public Component build() {
        return component;
    }

    private ChatText decorate(TextDecoration decoration) {
        return new ChatText(component.decoration(decoration, true));
    }

    private static String withoutSlash(String command) {
        return command.startsWith("/") ? command.substring(1) : command;
    }
}
