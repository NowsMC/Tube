package space.nows.tube.server.command.builtin.world;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.world.World;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;
import java.util.Map;

final class TimeCommand extends ServerCommand {

    private static final long DAY_LENGTH = 24000;
    private static final Map<String, Long> NAMED = Map.of("day", 1000L, "noon", 6000L, "night", 13000L, "midnight", 18000L);
    private static final List<String> QUERIES = List.of("day", "daytime", "gametime");

    private final Targets targets;

    TimeCommand(Targets targets) {
        super("time", Permissions.command("time"), "time set <day|noon|night|midnight|ticks> | time add <ticks> | time query <daytime|gametime|day>");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        World world = targets.origin(sender).world();
        switch (input.keyword()) {
            case "set" -> {
                long time = named(input.keyword());
                input.end();
                world.setTime(time);
                sender.sendMessage("Set the time to " + time);
            }
            case "add" -> {
                int ticks = input.integer();
                input.end();
                world.setTime(world.time() + ticks);
                sender.sendMessage("Set the time to " + world.time());
            }
            case "query" -> {
                String what = input.keyword();
                input.end();
                sender.sendMessage(switch (what) {
                    case "daytime" -> "The time is " + world.time() % DAY_LENGTH;
                    case "gametime" -> "The time is " + world.time();
                    case "day" -> "The day is " + world.time() / DAY_LENGTH;
                    default -> throw input.usage();
                });
            }
            default -> throw input.usage();
        }
    }

    private static long named(String text) {
        Long named = NAMED.get(text);
        if (named != null) {
            return named;
        }
        try {
            long ticks = Long.parseLong(text);
            if (ticks < 0) {
                throw new CommandFailure("The time must not be negative");
            }
            return ticks;
        } catch (NumberFormatException e) {
            throw new CommandFailure("Invalid time " + text);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, "add", "query", "set");
            case 1 -> switch (Typing.word(args, 0).toLowerCase(java.util.Locale.ROOT)) {
                case "set" -> Suggestions.matching(typed, NAMED.keySet());
                case "query" -> Suggestions.matching(typed, QUERIES);
                default -> List.of();
            };
            default -> List.of();
        };
    }
}
