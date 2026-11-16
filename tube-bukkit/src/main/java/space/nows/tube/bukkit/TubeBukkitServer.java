package space.nows.tube.bukkit;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

final class TubeBukkitServer {

    private TubeBukkitServer() {
    }

    static Server create(BukkitRuntime runtime) {
        var tube = runtime.tube;
        return Proxies.of(Server.class, "Server")
                .on("getName", args -> tube.name())
                .on("getVersion", args -> tube.name() + " " + tube.version() + " (MC: " + tube.minecraftVersion() + ")")
                .on("getBukkitVersion", args -> tube.minecraftVersion() + "-R0.1-SNAPSHOT")
                .on("getMinecraftVersion", args -> tube.minecraftVersion())
                .on("getLogger", args -> runtime.julLogger)
                .on("getPluginManager", args -> runtime.pluginManager)
                .on("getScheduler", args -> runtime.scheduler)
                .on("getServicesManager", args -> runtime.services)
                .on("getCommandMap", args -> runtime.commandMap)
                .on("getPluginCommand", args -> {
                    Command command = runtime.commandMap.getCommand((String) args[0]);
                    return command instanceof PluginCommand pluginCommand ? pluginCommand : null;
                })
                .on("getConsoleSender", args -> runtime.console())
                .on("getOnlinePlayers", args -> runtime.wrappers.onlinePlayers())
                .on("getPlayerExact", args -> runtime.wrappers.onlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase((String) args[0])).findFirst().orElse(null))
                .on("getPlayer(String)", args -> findPlayer(runtime, (String) args[0]))
                .on("getPlayer(UUID)", args -> runtime.wrappers.onlinePlayers().stream().filter(player -> player.getUniqueId().equals(args[0])).findFirst().orElse(null))
                .on("getWorlds", args -> tube.worlds().all().stream().map(runtime.wrappers::world).toList())
                .on("getWorld(String)", args -> tube.worlds().get((String) args[0]).map(runtime.wrappers::world).orElse(null))
                .on("getWorld(UUID)", args -> tube.worlds().all().stream().map(runtime.wrappers::world).filter(world -> world.getUID().equals(args[0])).findFirst().orElse(null))
                .on("getWorldContainer", args -> new File("worlds"))
                .on("getUpdateFolder", args -> "update")
                .on("getUpdateFolderFile", args -> new File(runtime.pluginsDirectory, "update"))
                .on("getMaxPlayers", args -> tube.maxPlayers())
                .on("getOnlineMode", args -> tube.onlineMode())
                .on("getMotd", args -> tube.motd())
                .on("getPort", args -> tube.port())
                .on("getIp", args -> tube.bindAddress().equals("0.0.0.0") ? "" : tube.bindAddress())
                .on("getMessenger", args -> runtime.messenger)
                .on("getUnsafe", args -> runtime.unsafe)
                .on("getHelpMap", args -> runtime.helpMap)
                .on("isPrimaryThread", args -> runtime.isPrimaryThread())
                .on("isStopping", args -> false)
                .on("dispatchCommand", args -> runtime.commandMap.dispatch((CommandSender) args[0], (String) args[1]))
                .on("broadcastMessage", args -> {
                    tube.broadcast((String) args[0]);
                    return tube.players().size();
                })
                .on("broadcast/2", args -> {
                    tube.broadcast((String) args[0]);
                    return tube.players().size();
                })
                .on("shutdown", args -> {
                    tube.stop();
                    return null;
                })
                .on("toString", args -> "TubeServer{name=" + tube.name() + ",version=" + tube.version() + "}")
                .build();
    }

    private static Player findPlayer(BukkitRuntime runtime, String name) {
        String wanted = name.toLowerCase(Locale.ROOT);
        List<Player> online = runtime.wrappers.onlinePlayers();
        for (Player player : online) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        Player best = null;
        int delta = Integer.MAX_VALUE;
        for (Player player : online) {
            String lowered = player.getName().toLowerCase(Locale.ROOT);
            if (lowered.startsWith(wanted) && lowered.length() - wanted.length() < delta) {
                delta = lowered.length() - wanted.length();
                best = player;
            }
        }
        return best;
    }

    @SuppressWarnings("unused")
    private static UUID unused() {
        return null;
    }
}
