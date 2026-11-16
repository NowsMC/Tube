package space.nows.tube.server.remote;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.Player;
import space.nows.tube.api.Server;
import space.nows.tube.network.NettyNetwork;
import space.nows.tube.network.QueryServer;
import space.nows.tube.network.RconServer;
import space.nows.tube.server.command.CommandService;
import space.nows.tube.server.command.RconSender;
import space.nows.tube.server.config.Markup;
import space.nows.tube.server.config.ServerSettings;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class RemoteAccess implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final Server server;
    private final ServerSettings settings;
    private final CommandService commands;
    private RconServer rcon;
    private QueryServer query;

    public RemoteAccess(Server server, ServerSettings settings, CommandService commands) {
        this.server = server;
        this.settings = settings;
        this.commands = commands;
    }

    public void start(NettyNetwork network) {
        String bindAddress = settings.connection().bindAddress();
        ServerSettings.Rcon rconSettings = settings.rcon();
        if (rconSettings.enabled()) {
            if (rconSettings.password().isEmpty()) {
                LOGGER.warn("server.properties: enable-rcon is true but rcon.password is empty, RCON stays disabled");
            } else {
                try {
                    rcon = new RconServer(network, new InetSocketAddress(bindAddress, rconSettings.port()), rconSettings.password(), this::runRcon);
                    LOGGER.info("RCON running on {}:{}", bindAddress, rconSettings.port());
                } catch (RuntimeException e) {
                    LOGGER.error("Could not start RCON on port {}", rconSettings.port(), e);
                }
            }
        }
        if (settings.query().enabled()) {
            try {
                query = new QueryServer(network, new InetSocketAddress(bindAddress, settings.query().port()), this::queryInfo);
                LOGGER.info("Query running on {}:{}", bindAddress, settings.query().port());
            } catch (RuntimeException e) {
                LOGGER.error("Could not start Query on port {}", settings.query().port(), e);
            }
        }
    }

    @Override
    public void close() {
        if (rcon != null) {
            rcon.close();
        }
        if (query != null) {
            query.close();
        }
    }

    private CompletableFuture<String> runRcon(String line) {
        CompletableFuture<String> result = new CompletableFuture<>();
        server.scheduler().runSync(() -> {
            RconSender sender = new RconSender();
            try {
                if (settings.rcon().broadcastToOps()) {
                    server.players().stream().filter(player -> server.permissions().isOp(player.uuid()))
                            .forEach(player -> player.sendMessage(Component.text("[Rcon: " + line + "]", NamedTextColor.GRAY)));
                }
                commands.dispatch(sender, line);
                result.complete(sender.output());
            } catch (RuntimeException e) {
                LOGGER.error("RCON command failed: {}", line, e);
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    private QueryServer.Info queryInfo() {
        String pluginList = server.plugins().stream().map(plugin -> plugin.name() + " " + plugin.version()).collect(Collectors.joining("; "));
        String software = server.name() + " " + server.version();
        var online = server.players();
        return new QueryServer.Info(
                PlainTextComponentSerializer.plainText().serialize(Markup.parse(settings.status().motd())),
                "SMP",
                settings.level().name(),
                server.minecraftVersion(),
                pluginList.isEmpty() ? software : software + ": " + pluginList,
                online.size(),
                settings.connection().maxPlayers(),
                settings.connection().port(),
                settings.connection().bindAddress(),
                online.stream().map(Player::name).toList());
    }
}
