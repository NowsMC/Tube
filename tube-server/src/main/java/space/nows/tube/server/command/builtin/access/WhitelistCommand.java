package space.nows.tube.server.command.builtin.access;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.access.Whitelist;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;
import space.nows.tube.server.command.support.Typing;
import space.nows.tube.server.config.ServerProperties;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

final class WhitelistCommand extends ServerCommand {

    private static final List<String> ACTIONS = List.of("add", "list", "off", "on", "reload", "remove");

    private final Server server;
    private final Whitelist whitelist;
    private final Profiles profiles;
    private final Targets targets;
    private final Path settingsFile;
    private final boolean enforce;

    WhitelistCommand(Server server, Whitelist whitelist, Profiles profiles, Targets targets, Path settingsFile, boolean enforce) {
        super("whitelist", Permissions.WHITELIST, "whitelist <add|remove|list|on|off|reload> [player]");
        this.server = server;
        this.whitelist = whitelist;
        this.profiles = profiles;
        this.targets = targets;
        this.settingsFile = settingsFile;
        this.enforce = enforce;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String action = input.keyword();
        switch (action) {
            case "on", "off" -> setEnabled(sender, action.equals("on"));
            case "list" -> sender.sendMessage("There are " + whitelist.names().size() + " whitelisted players: "
                    + whitelist.names().stream().sorted().collect(Collectors.joining(", ")));
            case "reload" -> {
                whitelist.reload();
                kickUnlisted();
                sender.sendMessage("Reloaded the whitelist");
            }
            case "add" -> add(sender, input);
            case "remove" -> remove(sender, input);
            default -> throw input.usage();
        }
    }

    private void setEnabled(CommandSender sender, boolean enabled) {
        whitelist.setEnabled(enabled);
        ServerProperties.set(settingsFile, "white-list", Boolean.toString(enabled));
        if (enabled) {
            kickUnlisted();
        }
        sender.sendMessage("Whitelist is now " + (enabled ? "on" : "off"));
    }

    private void add(CommandSender sender, CommandInput input) {
        Profiles.Profile profile = profile(input);
        sender.sendMessage(whitelist.add(profile.uuid(), profile.name())
                ? "Added " + profile.name() + " to the whitelist"
                : profile.name() + " is already whitelisted");
    }

    private void remove(CommandSender sender, CommandInput input) {
        Profiles.Profile profile = profile(input);
        sender.sendMessage(whitelist.remove(profile.uuid())
                ? "Removed " + profile.name() + " from the whitelist"
                : profile.name() + " is not whitelisted");
        kickUnlisted();
    }

    private Profiles.Profile profile(CommandInput input) {
        String name = input.word();
        input.end();
        return profiles.find(name).orElseThrow(() -> new CommandFailure("Unknown player " + name));
    }

    private void kickUnlisted() {
        if (!enforce || !whitelist.enabled()) {
            return;
        }
        server.players().stream()
                .filter(player -> !whitelist.contains(player.uuid()))
                .forEach(player -> player.kick("You are not white-listed on this server!"));
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> Suggestions.matching(typed, ACTIONS);
            case 1 -> switch (args.getFirst().toLowerCase(java.util.Locale.ROOT)) {
                case "add" -> Suggestions.matching(typed, targets.playerNames());
                case "remove" -> Suggestions.matching(typed, whitelist.names());
                default -> List.of();
            };
            default -> List.of();
        };
    }
}
