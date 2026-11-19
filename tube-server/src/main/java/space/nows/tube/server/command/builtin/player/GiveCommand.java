package space.nows.tube.server.command.builtin.player;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.inventory.ItemStack;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.RegistryNames;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class GiveCommand extends ServerCommand {

    private final Targets targets;

    GiveCommand(Targets targets) {
        super("give", Permissions.command("give"), "give <player> <item> [amount]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        List<Player> players = targets.players(sender, input.word());
        String item = input.word();
        int amount = input.integerOr(1);
        input.end();
        if (amount < 1) {
            throw new CommandFailure("Amount must be at least 1");
        }
        for (Player player : players) {
            ItemStack stack = ItemStack.of(item, amount);
            boolean fitted = player.inventory().add(stack);
            sender.sendMessage((fitted ? "Gave " : "Gave what fit of ") + amount + " " + stack.type() + " to " + player.name());
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> targets.suggestPlayers(typed);
            case 1 -> Suggestions.matching(typed, RegistryNames.items());
            default -> List.of();
        };
    }
}
