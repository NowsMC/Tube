package space.nows.tube.api.plugin;

import org.slf4j.Logger;
import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.network.Network;
import space.nows.tube.api.scheduler.Scheduler;

import java.nio.file.Path;

public interface PluginContext {

    Server server();

    PluginDescriptor descriptor();

    Path dataFolder();

    Logger logger();

    PluginConfig config();

    CommandRegistry commands();

    Scheduler scheduler();

    EventBus events();

    Network network();
}
