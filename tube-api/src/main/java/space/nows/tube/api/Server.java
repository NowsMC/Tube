package space.nows.tube.api;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.inventory.Inventory;
import space.nows.tube.api.network.Network;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.api.scheduler.Scheduler;
import space.nows.tube.api.world.WorldManager;

import java.util.Collection;

public interface Server {

    String name();

    String version();

    String minecraftVersion();

    int dataVersion();

    int maxPlayers();

    boolean onlineMode();

    String motd();

    int port();

    String bindAddress();

    CommandRegistry commands();

    PermissionManager permissions();

    WorldManager worlds();

    Scheduler scheduler();

    EventBus events();

    Network network();

    Collection<Player> players();

    Collection<PluginDescriptor> plugins();

    Difficulty difficulty();

    void setDifficulty(Difficulty difficulty);

    Inventory createInventory(Component title, int rows);

    void broadcast(Component message);

    default void broadcast(String message) {
        broadcast(Component.text(message));
    }

    void saveAll();

    void stop();
}
