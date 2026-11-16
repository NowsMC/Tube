package space.nows.tube.bukkit.command;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.permission.BukkitPermissible;

public final class BukkitCommandSender extends BukkitPermissible implements CommandSender {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final space.nows.tube.api.command.CommandSender tube;

    public BukkitCommandSender(BukkitRuntime runtime, space.nows.tube.api.command.CommandSender tube) {
        super(runtime);
        this.tube = tube;
    }

    @Override
    protected boolean granted(String node) {
        return tube.hasPermission(node);
    }

    @Override
    public boolean isOp() {
        return tube instanceof ConsoleCommandSender;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public void sendMessage(String message) {
        tube.sendMessage(LEGACY.deserialize(message));
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            tube.sendMessage(LEGACY.deserialize(message));
        }
    }

    @Override
    public Server getServer() {
        return runtime.server();
    }

    @Override
    public String getName() {
        return tube.name();
    }
}
