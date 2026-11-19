package space.nows.tube.server.command.builtin.chat;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.text.Chat;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

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
        Component from = Chat.text(sender.name()).hover("Click to reply to " + sender.name()).suggestCommand("msg " + sender.name() + " ").insertOnShiftClick(sender.name()).build();
        target.sendMessage(Chat.join(from, Component.text(" whispers to you: " + text)));
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), targets.playerNames()) : List.of();
    }
}
