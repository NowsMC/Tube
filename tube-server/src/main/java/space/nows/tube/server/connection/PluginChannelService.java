package space.nows.tube.server.connection;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerPluginMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Subscription;
import space.nows.tube.api.network.PluginChannels;
import space.nows.tube.server.player.MinestomPlayer;
import space.nows.tube.server.player.PlayerService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PluginChannelService implements PluginChannels {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final String BRAND = "minecraft:brand";

    private final PlayerService players;
    private final Map<String, List<ChannelHandler>> handlers = new ConcurrentHashMap<>();

    public PluginChannelService(PlayerService players) {
        this.players = players;
    }

    public void install(GlobalEventHandler events) {
        events.addListener(PlayerPluginMessageEvent.class, this::received);
    }

    private void received(PlayerPluginMessageEvent event) {
        String channel = normalize(event.getIdentifier());
        MinestomPlayer player = players.wrap(event.getPlayer());
        if (channel.equals(BRAND)) {
            player.setClientBrand(readBrand(event.getMessage()));
        }
        for (ChannelHandler handler : handlers.getOrDefault(channel, List.of())) {
            try {
                handler.received(player, channel, event.getMessage());
            } catch (RuntimeException e) {
                LOGGER.error("A handler of the channel {} failed", channel, e);
            }
        }
    }

    @Override
    public Subscription register(String channel, ChannelHandler handler) {
        String key = normalize(channel);
        handlers.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(handler);
        return () -> handlers.computeIfPresent(key, (ignored, list) -> {
            list.remove(handler);
            return list.isEmpty() ? null : list;
        });
    }

    @Override
    public void send(Player player, String channel, byte[] data) {
        if (!(player instanceof MinestomPlayer tube)) {
            throw new IllegalArgumentException("Not a player of this server");
        }
        ((net.minestom.server.entity.Player) tube.minestom()).sendPluginMessage(normalize(channel), data);
    }

    static String normalize(String channel) {
        String lowered = channel.toLowerCase(Locale.ROOT);
        return lowered.indexOf(':') < 0 ? "minecraft:" + lowered : lowered;
    }

    static String readBrand(byte[] message) {
        int length = 0;
        int shift = 0;
        int index = 0;
        while (index < message.length) {
            int next = message[index++];
            length |= (next & 0x7F) << shift;
            if ((next & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        int end = Math.min(message.length, index + length);
        return new String(message, index, Math.max(0, end - index), StandardCharsets.UTF_8);
    }
}
