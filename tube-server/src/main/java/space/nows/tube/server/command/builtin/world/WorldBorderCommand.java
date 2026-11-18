package space.nows.tube.server.command.builtin.world;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.world.World;
import space.nows.tube.api.world.WorldBorder;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;
import java.util.Locale;

final class WorldBorderCommand extends ServerCommand {

    private static final double MAX_DIAMETER = WorldBorder.DEFAULT.diameter();

    private final Targets targets;

    WorldBorderCommand(Targets targets) {
        super("worldborder", Permissions.command("worldborder"),
                "worldborder get | set <diameter> [seconds] | add <distance> [seconds] | center <x> <z> | warning <distance|time> <value>");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        World world = targets.origin(sender).world();
        WorldBorder border = world.border();
        switch (input.keyword()) {
            case "get" -> {
                input.end();
                sender.sendMessage(String.format(Locale.ROOT, "The world border is %.1f blocks wide", border.diameter()));
            }
            case "set" -> resize(sender, input, world, border, input.decimal());
            case "add" -> resize(sender, input, world, border, border.diameter() + input.decimal());
            case "center" -> {
                double x = input.decimal();
                double z = input.decimal();
                input.end();
                world.setBorder(border.withCenter(x, z));
                sender.sendMessage(String.format(Locale.ROOT, "Set the center of the world border to %.1f, %.1f", x, z));
            }
            case "warning" -> warning(sender, input, world, border);
            default -> throw input.usage();
        }
    }

    private static void resize(CommandSender sender, CommandInput input, World world, WorldBorder border, double diameter) {
        int seconds = input.integerOr(0);
        input.end();
        if (diameter < 1 || diameter > MAX_DIAMETER) {
            throw new CommandFailure("The world border must be between 1 and " + (long) MAX_DIAMETER + " blocks wide");
        }
        world.setBorder(border.withDiameter(diameter), Math.max(0, seconds));
        sender.sendMessage(String.format(Locale.ROOT, seconds > 0 ? "Moving the world border to %.1f blocks wide over " + seconds + " seconds"
                : "Set the world border to %.1f blocks wide", diameter));
    }

    private static void warning(CommandSender sender, CommandInput input, World world, WorldBorder border) {
        String kind = input.keyword();
        int value = input.integer();
        input.end();
        switch (kind) {
            case "distance" -> world.setBorder(border.withWarningDistance(value));
            case "time" -> world.setBorder(border.withWarningTime(value));
            default -> throw input.usage();
        }
        sender.sendMessage("Set the world border warning " + kind + " to " + value);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, "add", "center", "get", "set", "warning");
            case 1 -> Typing.word(args, 0).equalsIgnoreCase("warning") ? Suggestions.matching(typed, "distance", "time") : List.of();
            default -> List.of();
        };
    }
}
