package space.nows.tube.server.event;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.chat.Chat;
import space.nows.tube.api.entity.Player;

final class ChatFormat {

    private ChatFormat() {
    }

    static Component of(Player player, String message) {
        Component name = Chat.text(player.name()).hover("Click to message " + player.name()).suggestCommand("msg " + player.name() + " ").insertOnShiftClick(player.name()).build();
        return Component.text("<").append(name).append(Component.text("> " + message));
    }
}
