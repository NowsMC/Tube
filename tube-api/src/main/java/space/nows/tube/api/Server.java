package space.nows.tube.api;

import java.util.Collection;

public interface Server {

    String name();

    String minecraftVersion();

    CommandRegistry commands();

    WorldManager worlds();

    Scheduler scheduler();

    Collection<Player> players();

    Collection<PluginDescriptor> plugins();

    void broadcast(String message);

    void stop();
}
