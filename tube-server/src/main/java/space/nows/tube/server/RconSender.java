package space.nows.tube.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import space.nows.tube.api.CommandSender;

final class RconSender implements CommandSender {

    private final StringBuilder output = new StringBuilder();

    @Override
    public String name() {
        return "Rcon";
    }

    @Override
    public boolean hasPermission(String node) {
        return true;
    }

    @Override
    public synchronized void sendMessage(Component message) {
        if (!output.isEmpty()) {
            output.append('\n');
        }
        output.append(PlainTextComponentSerializer.plainText().serialize(message));
    }

    synchronized String output() {
        return output.toString();
    }
}
