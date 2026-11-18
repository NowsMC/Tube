package space.nows.tube.server.command.builtin.chat;

import space.nows.tube.api.Server;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

public final class ChatCommands {

    private ChatCommands() {
    }

    public static List<ServerCommand> create(Server server, Targets targets) {
        return List.of(new SayCommand(server), new MeCommand(server), new MessageCommand(targets));
    }
}
