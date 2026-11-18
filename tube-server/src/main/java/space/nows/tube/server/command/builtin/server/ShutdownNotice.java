package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.api.entity.Player;

import java.util.List;

final class ShutdownNotice {

    private ShutdownNotice() {
    }

    static void kickEveryone(Server server, String reason) {
        List<Player> online = List.copyOf(server.players());
        online.forEach(player -> player.kick(reason));
    }
}
