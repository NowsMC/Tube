package space.nows.tube.server.command.builtin.world;

import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.world.Weather;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.Durations;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;
import java.util.Locale;

final class WeatherCommand extends ServerCommand {

    private static final List<String> KINDS = List.of("clear", "rain", "thunder");

    private final Targets targets;

    WeatherCommand(Targets targets) {
        super("weather", Permissions.command("weather"), "weather <clear|rain|thunder> [duration]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        Weather weather = switch (input.keyword()) {
            case "clear" -> Weather.CLEAR;
            case "rain" -> Weather.RAIN;
            case "thunder" -> Weather.THUNDER;
            default -> throw input.usage();
        };
        int duration = input.hasNext() ? Durations.ticks(input.word()) : 0;
        input.end();
        targets.origin(sender).world().setWeather(weather, duration);
        sender.sendMessage("Set the weather to " + weather.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), KINDS) : List.of();
    }
}
