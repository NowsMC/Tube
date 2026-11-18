package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.inventory.Inventory;
import space.nows.tube.api.inventory.ItemStack;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.RegistryNames;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class ClearCommand extends ServerCommand {

    private final Targets targets;

    ClearCommand(Targets targets) {
        super("clear", Permissions.command("clear"), "clear [player] [item] [max count]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        List<Player> players = input.hasNext() ? targets.players(sender, input.word()) : List.of(Targets.self(sender));
        String item = input.hasNext() ? ItemStack.of(input.word(), 1).type() : null;
        int limit = input.integerOr(Integer.MAX_VALUE);
        input.end();
        for (Player player : players) {
            int removed = remove(player.inventory(), item, limit);
            sender.sendMessage("Removed " + removed + " item(s) from " + player.name());
        }
    }

    private static int remove(Inventory inventory, String item, int limit) {
        int removed = 0;
        for (int slot = 0; slot < inventory.size() && removed < limit; slot++) {
            ItemStack stack = inventory.item(slot);
            if (stack.isAir() || item != null && !stack.type().equals(item)) {
                continue;
            }
            int taken = Math.min(stack.amount(), limit - removed);
            inventory.setItem(slot, taken == stack.amount() ? ItemStack.AIR : stack.withAmount(stack.amount() - taken));
            removed += taken;
        }
        return removed;
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
