package space.nows.tube.api;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.event.EventBus;

import java.util.Collection;

public interface Server {

    String name();

    String minecraftVersion();

    CommandRegistry commands();

    WorldManager worlds();

    Scheduler scheduler();

    EventBus events();

    Collection<Player> players();

    Collection<PluginDescriptor> plugins();

    void broadcast(Component message);

    default void broadcast(String message) {
        broadcast(Component.text(message));
    }

    void stop();
}
