package space.nows.tube.server.connection;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import space.nows.tube.api.ResourcePack;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.access.AccessControl;
import space.nows.tube.server.access.UserCache;
import space.nows.tube.server.config.ServerSettings;
import space.nows.tube.server.player.PlayerDataStore;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LoginHandler {

    private final ServerSettings settings;
    private final Supplier<space.nows.tube.api.entity.GameMode> defaultGameMode;
    private final AccessControl access;
    private final UserCache userCache;
    private final PlayerService players;
    private final PlayerDataStore playerData;
    private final WorldService worlds;
    private final Optional<ResourcePack> resourcePack;
    private final Consumer<Player> savePlayer;

    public LoginHandler(ServerSettings settings, Supplier<space.nows.tube.api.entity.GameMode> defaultGameMode, AccessControl access, UserCache userCache, PlayerService players, PlayerDataStore playerData,
                 WorldService worlds, Optional<ResourcePack> resourcePack, Consumer<Player> savePlayer) {
        this.settings = settings;
        this.defaultGameMode = defaultGameMode;
        this.access = access;
        this.userCache = userCache;
        this.players = players;
        this.playerData = playerData;
        this.worlds = worlds;
        this.resourcePack = resourcePack;
        this.savePlayer = savePlayer;
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(AsyncPlayerConfigurationEvent.class, this::configure);
        handler.addListener(PlayerSpawnEvent.class, this::firstSpawn);
        handler.addListener(PlayerDisconnectEvent.class, event -> savePlayer.accept(event.getPlayer()));
    }

    private void configure(AsyncPlayerConfigurationEvent event) {
        Player player = event.getPlayer();
        event.setHardcore(settings.gameplay().hardcore());
        String ip = player.getPlayerConnection().getRemoteAddress() instanceof InetSocketAddress inet ? AccessControl.host(inet) : null;
        var denied = access.deny(player.getUuid(), ip);
        if (denied.isPresent()) {
            player.kick(denied.get());
            return;
        }
        userCache.remember(player.getUuid(), player.getUsername());
        if (MinecraftServer.getConnectionManager().getOnlinePlayers().size() >= settings.connection().maxPlayers()) {
            player.kick(Component.text("The server is full!"));
            return;
        }
        var wrapped = players.wrap(player);
        resourcePack.ifPresent(wrapped::sendResourcePack);
        var saved = playerData.spawn(player.getUuid()).flatMap(spawn -> worlds.loadExisting(spawn.world()).map(world -> Map.entry(world, spawn.position())));
        if (saved.isPresent()) {
            event.setSpawningInstance(worlds.instanceOf(saved.get().getKey()));
            player.setRespawnPoint(saved.get().getValue());
            return;
        }
        Location location = worlds.defaultWorld().spawn();
        event.setSpawningInstance(worlds.instanceOf(location.world()));
        player.setRespawnPoint(new Pos(location.x(), location.y(), location.z()));
    }

    private void firstSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) {
            return;
        }
        boolean force = settings.gameplay().forceGamemode();
        var data = playerData.read(event.getPlayer().getUuid());
        data.ifPresent(tag -> playerData.apply(tag, event.getPlayer(), !force));
        if (data.isEmpty() || force) {
            event.getPlayer().setGameMode(GameMode.valueOf(defaultGameMode.get().name()));
        }
    }
}
