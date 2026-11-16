package space.nows.tube.server.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.command.CommandSender;

final class ConsoleSender implements CommandSender {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    @Override
    public String name() {
        return "Console";
    }

    @Override
    public boolean hasPermission(String node) {
        return true;
    }

    @Override
    public void sendMessage(Component message) {
        LOGGER.info(PlainTextComponentSerializer.plainText().serialize(message));
    }
}
