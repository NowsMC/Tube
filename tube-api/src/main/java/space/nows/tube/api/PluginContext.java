package space.nows.tube.api;

import org.slf4j.Logger;
import space.nows.tube.api.event.EventBus;

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
}
