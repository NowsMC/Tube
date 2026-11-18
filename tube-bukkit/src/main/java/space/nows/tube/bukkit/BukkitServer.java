package space.nows.tube.bukkit;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import space.nows.tube.bukkit.boss.BukkitBossBar;
import space.nows.tube.bukkit.world.BukkitBlockData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

final class BukkitServer implements Server {

    private static final org.bukkit.packs.DataPackManager NO_DATA_PACKS = new org.bukkit.packs.DataPackManager() {
        @Override
        public java.util.Collection<org.bukkit.packs.DataPack> getDataPacks() {
            return java.util.List.of();
        }

        @Override
        public java.util.Collection<org.bukkit.packs.DataPack> getEnabledDataPacks(org.bukkit.World world) {
            return java.util.List.of();
        }

        @Override
        public java.util.Collection<org.bukkit.packs.DataPack> getDisabledDataPacks(org.bukkit.World world) {
            return java.util.List.of();
        }
    };

    private final BukkitRuntime runtime;

    BukkitServer(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public String getName() {
        return runtime.tube().name();
    }

    @Override
    public String getVersion() {
        return runtime.tube().name() + " " + runtime.tube().version() + " (MC: " + runtime.tube().minecraftVersion() + ")";
    }

    @Override
    public String getBukkitVersion() {
        return runtime.tube().minecraftVersion() + "-R0.1-SNAPSHOT";
    }

    @Override
    public Logger getLogger() {
        return runtime.julLogger();
    }

    @Override
    public PluginManager getPluginManager() {
        return runtime.pluginManager();
    }

    @Override
    public BukkitScheduler getScheduler() {
        return runtime.scheduler();
    }

    @Override
    public ServicesManager getServicesManager() {
        return runtime.services();
    }

    @Override
    public org.bukkit.boss.BossBar createBossBar(String title, org.bukkit.boss.BarColor color, org.bukkit.boss.BarStyle style, org.bukkit.boss.BarFlag... flags) {
        return new BukkitBossBar(runtime, title, color, style, flags);
    }

    @Override
    public org.bukkit.block.data.BlockData createBlockData(String data) {
        return BukkitBlockData.parse(data);
    }

    @Override
    public org.bukkit.plugin.messaging.Messenger getMessenger() {
        return runtime.messenger();
    }

    @Override
    public org.bukkit.help.HelpMap getHelpMap() {
        return runtime.helpMap();
    }

    @Override
    public org.bukkit.UnsafeValues getUnsafe() {
        return new org.bukkit.UnsafeValues() {
            @Override
            public int getDataVersion() {
                return runtime.tube().dataVersion();
            }

            @Override
            public int getProtocolVersion() {
                return runtime.tube().protocolVersion();
            }
        };
    }

    @Override
    public org.bukkit.inventory.ItemFactory getItemFactory() {
        return type -> org.bukkit.Material.matchMaterial(type.getKey().getKey() + "_spawn_egg");
    }

    @Override
    public org.bukkit.packs.DataPackManager getDataPackManager() {
        return NO_DATA_PACKS;
    }

    @Override
    public PluginCommand getPluginCommand(String name) {
        return runtime.commands().get(name);
    }

    @Override
    public ConsoleCommandSender getConsoleSender() {
        return runtime.adapters().console();
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return runtime.adapters().onlinePlayers();
    }

    @Override
    public int getMaxPlayers() {
        return runtime.tube().maxPlayers();
    }

    @Override
    public int getPort() {
        return runtime.tube().port();
    }

    @Override
    public String getIp() {
        return runtime.tube().bindAddress().equals("0.0.0.0") ? "" : runtime.tube().bindAddress();
    }

    @Override
    public String getMotd() {
        return runtime.tube().motd();
    }

    @Override
    public boolean getOnlineMode() {
        return runtime.tube().onlineMode();
    }

    @Override
    public Player getPlayer(String name) {
        Player exact = getPlayerExact(name);
        if (exact != null) {
            return exact;
        }
        String wanted = name.toLowerCase(Locale.ROOT);
        Player best = null;
        int delta = Integer.MAX_VALUE;
        for (Player player : runtime.adapters().onlinePlayers()) {
            String lowered = player.getName().toLowerCase(Locale.ROOT);
            if (lowered.startsWith(wanted) && lowered.length() - wanted.length() < delta) {
                delta = lowered.length() - wanted.length();
                best = player;
            }
        }
        return best;
    }

    @Override
    public Player getPlayer(UUID id) {
        return runtime.adapters().onlinePlayers().stream().filter(player -> player.getUniqueId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Player getPlayerExact(String name) {
        return runtime.adapters().onlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Override
    public List<Player> matchPlayer(String name) {
        String wanted = name.toLowerCase(Locale.ROOT);
        List<Player> matches = new ArrayList<>();
        for (Player player : runtime.adapters().onlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return List.of(player);
            }
            if (player.getName().toLowerCase(Locale.ROOT).contains(wanted)) {
                matches.add(player);
            }
        }
        return matches;
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        Player online = getPlayerExact(name);
        return online != null ? online : runtime.adapters().offline(Adapters.offlineUuid(name), name);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(UUID id) {
        Player online = getPlayer(id);
        return online != null ? online : runtime.adapters().offline(id, null);
    }

    @Override
    public List<World> getWorlds() {
        return runtime.tube().worlds().all().stream().map(runtime.adapters()::world).toList();
    }

    @Override
    public World getWorld(String name) {
        return runtime.tube().worlds().get(name).map(runtime.adapters()::world).orElse(null);
    }

    @Override
    public World createWorld(org.bukkit.WorldCreator creator) {
        var worlds = runtime.tube().worlds();
        boolean loaded = worlds.get(creator.name()).isPresent();
        var tubeWorld = worlds.create(creator.name(), worldOptions(creator));
        World world = runtime.adapters().world(tubeWorld);
        if (!loaded) {
            runtime.pluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(world));
        }
        return world;
    }

    private space.nows.tube.api.world.WorldOptions worldOptions(org.bukkit.WorldCreator creator) {
        String generator = creator.generator();
        if (generator == null || !runtime.tube().worlds().generators().contains(generator)) {
            if (generator != null) {
                runtime.julLogger().warning("World " + creator.name() + " asks for the generator " + generator + ", which Tube does not know; using the world type instead");
            }
            generator = creator.type() == org.bukkit.WorldType.FLAT ? space.nows.tube.api.world.WorldOptions.FLAT : space.nows.tube.api.world.WorldOptions.NORMAL;
        }
        var environment = switch (creator.environment()) {
            case NETHER -> space.nows.tube.api.world.Environment.NETHER;
            case THE_END -> space.nows.tube.api.world.Environment.END;
            default -> space.nows.tube.api.world.Environment.OVERWORLD;
        };
        return new space.nows.tube.api.world.WorldOptions(generator, creator.seed(), environment);
    }

    @Override
    public boolean unloadWorld(World world, boolean save) {
        return world != null && unloadWorld(world.getName(), save);
    }

    @Override
    public boolean unloadWorld(String name, boolean save) {
        World world = getWorld(name);
        if (world == null) {
            return false;
        }
        var event = new org.bukkit.event.world.WorldUnloadEvent(world);
        runtime.pluginManager().callEvent(event);
        if (event.isCancelled() || !runtime.tube().worlds().unload(name, save)) {
            return false;
        }
        runtime.adapters().forgetWorld(name);
        return true;
    }

    @Override
    public World getWorld(UUID uid) {
        return getWorlds().stream().filter(world -> world.getUID().equals(uid)).findFirst().orElse(null);
    }

    @Override
    public File getWorldContainer() {
        return new File("worlds");
    }

    @Override
    public boolean dispatchCommand(CommandSender sender, String commandLine) {
        return runtime.commands().dispatch(sender, commandLine);
    }

    @Override
    public int broadcastMessage(String message) {
        runtime.tube().broadcast(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(message));
        return runtime.tube().players().size();
    }

    @Override
    public int broadcast(String message, String permission) {
        int delivered = 0;
        for (Player player : runtime.adapters().onlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(message);
                delivered++;
            }
        }
        return delivered;
    }

    @Override
    public boolean isPrimaryThread() {
        return runtime.isPrimaryThread();
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public void shutdown() {
        runtime.tube().stop();
    }

    @Override
    public String getUpdateFolder() {
        return "update";
    }

    @Override
    public File getUpdateFolderFile() {
        return new File(runtime.pluginsDirectory(), "update");
    }

    @Override
    public String toString() {
        return "TubeServer{name=" + runtime.tube().name() + ",version=" + runtime.tube().version() + "}";
    }
}
