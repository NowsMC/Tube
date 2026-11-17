package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class MessageCommand extends ServerCommand {

    private final Targets targets;

    MessageCommand(Targets targets) {
        super("msg", null, "msg <player> <message>", "tell", "w");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        String text = input.rest();
        Player target = targets.online(name).orElseThrow(() -> new CommandFailure("Player " + name + " is not online"));
        sender.sendMessage("You whisper to " + target.name() + ": " + text);
        target.sendMessage(sender.name() + " whispers to you: " + text);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), targets.playerNames()) : List.of();
    }
}
