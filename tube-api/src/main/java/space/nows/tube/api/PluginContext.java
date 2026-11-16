package space.nows.tube.api;

import org.slf4j.Logger;

import java.nio.file.Path;

public interface PluginContext {

    Server server();

    PluginDescriptor descriptor();

    Path dataFolder();

    Logger logger();

    PluginConfig config();

    CommandRegistry commands();

    Scheduler scheduler();
}
