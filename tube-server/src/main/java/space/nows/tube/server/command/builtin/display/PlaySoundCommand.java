package space.nows.tube.server.command.builtin.display;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Positions;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.RegistryNames;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class PlaySoundCommand extends ServerCommand {

    private static final List<String> SOURCES = Arrays.stream(Sound.Source.values()).map(source -> source.name().toLowerCase(Locale.ROOT)).toList();

    private final Targets targets;

    PlaySoundCommand(Targets targets) {
        super("playsound", Permissions.command("playsound"), "playsound <sound> <source> <targets> [x y z] [volume] [pitch]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String name = input.word();
        Sound.Source source = source(input.keyword());
        List<Player> listeners = targets.players(sender, input.word());
        Location at = input.remaining() >= 3 ? Positions.read(input, targets.origin(sender)) : null;
        float volume = (float) (input.hasNext() ? input.decimal() : 1);
        float pitch = (float) (input.hasNext() ? input.decimal() : 1);
        input.end();
        if (volume < 0 || pitch < 0.5 || pitch > 2) {
            throw new CommandFailure("The volume must not be negative and the pitch must be between 0.5 and 2");
        }
        Key key = Key.key(name.contains(":") ? name : "minecraft:" + name);
        for (Player listener : listeners) {
            Location where = at != null ? at : listener.location();
            listener.playSound(Sound.sound(key, source, volume, pitch), where.x(), where.y(), where.z());
        }
        sender.sendMessage(listeners.size() == 1 ? "Played sound " + key.asString() + " to " + listeners.getFirst().name()
                : "Played sound " + key.asString() + " to " + listeners.size() + " players");
    }

    private static Sound.Source source(String text) {
        try {
            return Sound.Source.valueOf(text.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CommandFailure("Unknown sound source " + text);
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, RegistryNames.sounds());
            case 1 -> Suggestions.matching(typed, SOURCES);
            case 2 -> targets.suggestPlayers(typed);
            default -> Positions.suggest(args, 3);
        };
    }
}
