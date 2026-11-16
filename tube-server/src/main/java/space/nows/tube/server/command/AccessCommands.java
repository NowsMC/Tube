package space.nows.tube.server.command;

import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.CommandSender;
import space.nows.tube.api.Player;
import space.nows.tube.api.Server;
import space.nows.tube.server.access.AccessControl;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.access.Whitelist;
import space.nows.tube.server.config.ServerProperties;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class AccessCommands {

    static final String KICK = "tube.kick";
    static final String BAN = "tube.ban";
    static final String WHITELIST = "tube.whitelist";

    private static final Pattern IP_ADDRESS = Pattern.compile("[0-9a-fA-F:.]+");

    private final Server server;
    private final Profiles profiles;
    private final BanList bans;
    private final Whitelist whitelist;
    private final Path settingsFile;
    private final boolean enforceWhitelist;

    public AccessCommands(Server server, Profiles profiles, BanList bans, Whitelist whitelist, Path settingsFile, boolean enforceWhitelist) {
        this.server = server;
        this.profiles = profiles;
        this.bans = bans;
        this.whitelist = whitelist;
        this.settingsFile = settingsFile;
        this.enforceWhitelist = enforceWhitelist;
    }

    public void register(CommandRegistry registry) {
        registry.register("kick", Command.withPermission(KICK, this::kick));
        registry.register("ban", Command.withPermission(BAN, this::ban));
        registry.register("pardon", Command.withPermission(BAN, this::pardon));
        registry.register("ban-ip", Command.withPermission(BAN, this::banIp));
        registry.register("pardon-ip", Command.withPermission(BAN, this::pardonIp));
        registry.register("banlist", Command.withPermission(BAN, this::banlist));
        registry.register("whitelist", Command.withPermission(WHITELIST, this::whitelist));
    }

    private void kick(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: kick <player> [reason]");
            return;
        }
        Optional<Player> target = online(args.getFirst());
        if (target.isEmpty()) {
            sender.sendMessage("Player " + args.getFirst() + " is not online");
            return;
        }
        target.get().kick(reason(args, "Kicked by an operator"));
        sender.sendMessage("Kicked " + target.get().name());
    }

    private void ban(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: ban <player> [reason]");
            return;
        }
        Optional<Profiles.Profile> profile = profiles.find(args.getFirst());
        if (profile.isEmpty()) {
            sender.sendMessage("Unknown player " + args.getFirst());
            return;
        }
        String reason = reason(args, BanList.DEFAULT_REASON);
        bans.banPlayer(profile.get().uuid(), profile.get().name(), reason, sender.name());
        online(profile.get().name()).ifPresent(player -> player.kick("You are banned from this server.\nReason: " + reason));
        sender.sendMessage("Banned " + profile.get().name() + ": " + reason);
    }

    private void pardon(CommandSender sender, List<String> args) {
        if (args.size() != 1) {
            sender.sendMessage("Usage: pardon <player>");
            return;
        }
        Optional<java.util.UUID> uuid = bans.players().stream()
                .filter(ban -> ban.name().equalsIgnoreCase(args.getFirst()))
                .map(ban -> java.util.UUID.fromString(ban.key()))
                .findFirst();
        if (uuid.isEmpty() || !bans.pardonPlayer(uuid.get())) {
            sender.sendMessage(args.getFirst() + " is not banned");
            return;
        }
        sender.sendMessage("Unbanned " + args.getFirst());
    }

    private void banIp(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: ban-ip <address|player> [reason]");
            return;
        }
        String address = args.getFirst();
        if (!IP_ADDRESS.matcher(address).matches()) {
            Optional<Player> player = online(address);
            Optional<String> resolved = player.flatMap(Player::address).map(AccessControl::host);
            if (resolved.isEmpty()) {
                sender.sendMessage("Invalid address or the player is not online");
                return;
            }
            address = resolved.get();
        }
        String reason = reason(args, BanList.DEFAULT_REASON);
        bans.banIp(address, reason, sender.name());
        String banned = address;
        server.players().stream()
                .filter(player -> player.address().map(AccessControl::host).filter(banned::equals).isPresent())
                .forEach(player -> player.kick("Your IP address is banned from this server.\nReason: " + reason));
        sender.sendMessage("Banned IP " + address + ": " + reason);
    }

    private void pardonIp(CommandSender sender, List<String> args) {
        if (args.size() != 1) {
            sender.sendMessage("Usage: pardon-ip <address>");
            return;
        }
        sender.sendMessage(bans.pardonIp(args.getFirst()) ? "Unbanned IP " + args.getFirst() : args.getFirst() + " is not banned");
    }

    private void banlist(CommandSender sender, List<String> args) {
        boolean ips = !args.isEmpty() && args.getFirst().equalsIgnoreCase("ips");
        List<BanList.Ban> entries = ips ? bans.ips() : bans.players();
        sender.sendMessage("There are " + entries.size() + " banned " + (ips ? "IPs" : "players"));
        entries.forEach(ban -> sender.sendMessage(ban.name() + " was banned by " + ban.source() + ": " + ban.reason()));
    }

    private void whitelist(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage("Usage: whitelist <add|remove|list|on|off|reload> [player]");
            return;
        }
        switch (args.getFirst().toLowerCase(java.util.Locale.ROOT)) {
            case "on" -> setEnabled(sender, true);
            case "off" -> setEnabled(sender, false);
            case "list" -> sender.sendMessage("There are " + whitelist.names().size() + " whitelisted players: "
                    + whitelist.names().stream().sorted().collect(Collectors.joining(", ")));
            case "reload" -> {
                whitelist.reload();
                kickUnlisted();
                sender.sendMessage("Reloaded the whitelist");
            }
            case "add", "remove" -> changeEntry(sender, args);
            default -> sender.sendMessage("Usage: whitelist <add|remove|list|on|off|reload> [player]");
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

    private void changeEntry(CommandSender sender, List<String> args) {
        if (args.size() != 2) {
            sender.sendMessage("Usage: whitelist " + args.getFirst() + " <player>");
            return;
        }
        Optional<Profiles.Profile> profile = profiles.find(args.get(1));
        if (profile.isEmpty()) {
            sender.sendMessage("Unknown player " + args.get(1));
            return;
        }
        if (args.getFirst().equalsIgnoreCase("add")) {
            sender.sendMessage(whitelist.add(profile.get().uuid(), profile.get().name())
                    ? "Added " + profile.get().name() + " to the whitelist"
                    : profile.get().name() + " is already whitelisted");
        } else {
            sender.sendMessage(whitelist.remove(profile.get().uuid())
                    ? "Removed " + profile.get().name() + " from the whitelist"
                    : profile.get().name() + " is not whitelisted");
            kickUnlisted();
        }
    }

    private void kickUnlisted() {
        if (!enforceWhitelist || !whitelist.enabled()) {
            return;
        }
        server.players().stream()
                .filter(player -> !whitelist.contains(player.uuid()))
                .forEach(player -> player.kick("You are not white-listed on this server!"));
    }

    private Optional<Player> online(String name) {
        return server.players().stream().filter(player -> player.name().equalsIgnoreCase(name)).findFirst();
    }

    private static String reason(List<String> args, String fallback) {
        return args.size() > 1 ? String.join(" ", args.subList(1, args.size())) : fallback;
    }
}
