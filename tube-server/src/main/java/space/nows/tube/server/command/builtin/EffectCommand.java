package space.nows.tube.server.command.builtin;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.entity.PotionEffect;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.RegistryNames;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class EffectCommand extends ServerCommand {

    private static final int DEFAULT_SECONDS = 30;
    private static final int TICKS_PER_SECOND = 20;

    private final Targets targets;

    EffectCommand(Targets targets) {
        super("effect", Permissions.command("effect"),
                "effect give <player> <effect> [seconds] [amplifier] [hideParticles] | effect clear <player> [effect]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        switch (input.keyword()) {
            case "give" -> give(sender, input);
            case "clear" -> clear(sender, input);
            default -> throw input.usage();
        }
    }

    private void give(CommandSender sender, CommandInput input) {
        List<Player> players = targets.players(sender, input.word());
        String effect = known(input.word());
        int seconds = input.integerOr(DEFAULT_SECONDS);
        int amplifier = input.integerOr(0);
        boolean hidden = Boolean.parseBoolean(input.wordOr("false"));
        input.end();
        if (seconds < 1 || amplifier < 0 || amplifier > 255) {
            throw new CommandFailure("Seconds must be at least 1 and the amplifier between 0 and 255");
        }
        PotionEffect potion = new PotionEffect(effect, seconds * TICKS_PER_SECOND, amplifier, false, !hidden, true);
        players.forEach(player -> player.addEffect(potion));
        sender.sendMessage("Applied " + potion.type() + " to " + names(players));
    }

    private void clear(CommandSender sender, CommandInput input) {
        List<Player> players = input.hasNext() ? targets.players(sender, input.word()) : List.of(Targets.self(sender));
        String effect = input.hasNext() ? known(input.word()) : null;
        input.end();
        for (Player player : players) {
            if (effect == null) {
                player.clearEffects();
            } else {
                player.removeEffect(effect);
            }
        }
        sender.sendMessage("Removed " + (effect == null ? "every effect" : effect) + " from " + names(players));
    }

    private static String known(String effect) {
        String key = effect.indexOf(':') < 0 ? "minecraft:" + effect : effect;
        if (!RegistryNames.effects().contains(key)) {
            throw new CommandFailure("Unknown effect " + effect);
        }
        return key;
    }

    private static String names(List<Player> players) {
        return players.size() == 1 ? players.getFirst().name() : players.size() + " players";
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, "clear", "give");
            case 1 -> Suggestions.matching(typed, targets.playerOptions());
            case 2 -> Suggestions.matching(typed, RegistryNames.effects());
            case 5 -> Typing.word(args, 0).equalsIgnoreCase("give") ? Suggestions.matching(typed, "false", "true") : List.of();
            default -> List.of();
        };
    }
}
