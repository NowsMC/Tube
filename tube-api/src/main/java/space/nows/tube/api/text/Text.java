package space.nows.tube.api.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private Text() {
    }

    public static Component mini(String markup) {
        return MINI.deserialize(markup);
    }

    public static Component plain(String text) {
        return Component.text(text);
    }
}
