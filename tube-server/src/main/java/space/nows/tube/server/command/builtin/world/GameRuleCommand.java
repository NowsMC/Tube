package space.nows.tube.server.command.builtin.world;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.api.world.World;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.Arrays;
import java.util.List;

final class GameRuleCommand extends ServerCommand {

    private final Targets targets;

    GameRuleCommand(Targets targets) {
        super("gamerule", Permissions.command("gamerule"), "gamerule <rule> [true|false]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        GameRule rule = GameRule.byKey(name).orElseThrow(() -> new CommandFailure("Unknown game rule " + name));
        World world = targets.origin(sender).world();
        if (!input.hasNext()) {
            sender.sendMessage("Game rule " + rule.key() + " is currently set to: " + world.gameRule(rule));
            return;
        }
        boolean value = switch (input.keyword()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw input.usage();
        };
        input.end();
        world.setGameRule(rule, value);
        sender.sendMessage("Game rule " + rule.key() + " has been updated to " + value);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, Arrays.stream(GameRule.values()).map(GameRule::key).toList());
            case 1 -> Suggestions.matching(typed, "false", "true");
            default -> List.of();
        };
    }
}
