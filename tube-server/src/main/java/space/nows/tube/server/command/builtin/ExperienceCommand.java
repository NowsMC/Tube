package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Player;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ExperiencePoints;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class ExperienceCommand extends ServerCommand {

    private static final List<String> ACTIONS = List.of("add", "query", "set");
    private static final List<String> UNITS = List.of("levels", "points");

    private final Targets targets;

    ExperienceCommand(Targets targets) {
        super("xp", Permissions.command("xp"), "xp <add|set> <player> <amount> [points|levels] | xp query <player> [points|levels]", "experience");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String action = input.keyword();
        if (action.equals("query")) {
            Player player = targets.player(sender, input.word());
            boolean levels = levels(input);
            sender.sendMessage(player.name() + " has " + (levels ? player.level() + " levels" : ExperiencePoints.total(player) + " experience points"));
            return;
        }
        if (!action.equals("add") && !action.equals("set")) {
            throw input.usage();
        }
        List<Player> players = targets.players(sender, input.word());
        int amount = input.integer();
        boolean levels = levels(input);
        for (Player player : players) {
            change(player, action.equals("add"), amount, levels);
        }
        String unit = levels ? " levels" : " experience points";
        sender.sendMessage((action.equals("add") ? "Gave " + amount + unit + " to " : "Set" + unit + " to " + amount + " for ")
                + (players.size() == 1 ? players.getFirst().name() : players.size() + " players"));
    }

    private static boolean levels(CommandInput input) {
        String unit = input.wordOr("points").toLowerCase(java.util.Locale.ROOT);
        input.end();
        if (!UNITS.contains(unit)) {
            throw input.usage();
        }
        return unit.equals("levels");
    }

    private static void change(Player player, boolean add, int amount, boolean levels) {
        if (levels) {
            player.setLevel(Math.max(0, add ? player.level() + amount : amount));
            return;
        }
        ExperiencePoints.setTotal(player, add ? ExperiencePoints.total(player) + amount : amount);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        boolean query = Typing.word(args, 0).equalsIgnoreCase("query");
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, ACTIONS);
            case 1 -> Suggestions.matching(typed, query ? targets.playerNames() : targets.playerOptions());
            case 2 -> query ? Suggestions.matching(typed, UNITS) : List.of();
            case 3 -> query ? List.of() : Suggestions.matching(typed, UNITS);
            default -> List.of();
        };
    }
}
