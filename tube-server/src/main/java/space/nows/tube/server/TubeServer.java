package space.nows.tube.server;

import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import space.nows.tube.api.Server;

import java.util.Objects;

public final class TubeServer implements Server {

    private final VersionProfile profile;
    private final ServerConfig config;

    public TubeServer(VersionProfile profile, ServerConfig config) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.config = Objects.requireNonNull(config, "config");
    }

    @Override
    public String name() {
        return "Tube";
    }

    @Override
    public String minecraftVersion() {
        return profile.minecraftVersion();
    }

    public void start() {
        MinecraftServer server = MinecraftServer.init(config.onlineMode() ? new Auth.Online() : new Auth.Offline());
        MinecraftServer.setBrandName(name());

        InstanceContainer world = MinecraftServer.getInstanceManager().createInstanceContainer();
        world.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            event.setSpawningInstance(world);
            event.getPlayer().setRespawnPoint(new Pos(0, 41, 0));
        });

        Runtime.getRuntime().addShutdownHook(new Thread(MinecraftServer::stopCleanly, "tube-shutdown"));
        server.start(config.host(), config.port());
    }
}
