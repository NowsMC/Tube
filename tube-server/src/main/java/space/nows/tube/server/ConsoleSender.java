package space.nows.tube.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.CommandSender;

final class ConsoleSender implements CommandSender {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    @Override
    public String name() {
        return "Console";
    }

    @Override
    public void sendMessage(String message) {
        LOGGER.info(message);
    }
}
