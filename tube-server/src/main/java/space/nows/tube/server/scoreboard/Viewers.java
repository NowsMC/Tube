package space.nows.tube.server.scoreboard;

import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.SendablePacket;
import space.nows.tube.server.player.MinestomPlayer;
import space.nows.tube.server.player.PlayerService;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class Viewers {

    private final Set<Player> players = ConcurrentHashMap.newKeySet();

    static Player handle(space.nows.tube.api.entity.player.Player player) {
        return ((MinestomPlayer) player).handle();
    }

    boolean add(Player player) {
        return players.add(player);
    }

    boolean remove(Player player) {
        return players.remove(player);
    }

    boolean contains(Player player) {
        return players.contains(player);
    }

    void forget(UUID id) {
        players.removeIf(player -> player.getUuid().equals(id));
    }

    Set<Player> online() {
        Set<Player> online = new LinkedHashSet<>();
        for (Player player : players) {
            if (player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    Set<space.nows.tube.api.entity.player.Player> wrapped(PlayerService service) {
        Set<space.nows.tube.api.entity.player.Player> wrapped = new LinkedHashSet<>();
        online().forEach(player -> wrapped.add(service.wrap(player)));
        return wrapped;
    }

    void send(SendablePacket packet) {
        online().forEach(player -> player.sendPacket(packet));
    }

    void clear() {
        players.clear();
    }
}
