package space.nows.tube.bukkit.command;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.permission.BukkitPermissible;

public final class BukkitConsoleSender extends BukkitPermissible implements ConsoleCommandSender {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public BukkitConsoleSender(BukkitRuntime runtime) {
        super(runtime);
    }

    @Override
    protected boolean granted(String node) {
        return true;
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public void sendMessage(String message) {
        org.slf4j.LoggerFactory.getLogger("tube").info(PlainTextComponentSerializer.plainText().serialize(LEGACY.deserialize(message)));
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public Server getServer() {
        return runtime.server();
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }
}
