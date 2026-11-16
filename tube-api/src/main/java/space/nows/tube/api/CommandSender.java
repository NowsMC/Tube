package space.nows.tube.api;

import net.kyori.adventure.text.Component;

public interface CommandSender {

    String name();

    void sendMessage(Component message);

    default void sendMessage(String message) {
        sendMessage(Component.text(message));
    }
}
