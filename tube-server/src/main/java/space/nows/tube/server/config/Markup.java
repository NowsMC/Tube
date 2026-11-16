package space.nows.tube.server.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Markup {

    private Markup() {
    }

    public static Component parse(String text) {
        return text.indexOf('§') >= 0
                ? LegacyComponentSerializer.legacySection().deserialize(text)
                : MiniMessage.miniMessage().deserialize(text);
    }
}
