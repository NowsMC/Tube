package space.nows.tube.server.command.builtin;

import space.nows.tube.api.Server;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.Profiles;
import space.nows.tube.server.access.Whitelist;
import space.nows.tube.server.command.CommandService;
import space.nows.tube.server.command.builtin.chat.ChatCommands;
import space.nows.tube.server.command.builtin.info.InfoCommands;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BuiltinCommands {

    private final Server server;
    private final CommandService commands;
    private final Profiles profiles;
    private final BanList bans;
    private final Whitelist whitelist;
    private final Path settingsFile;
    private final boolean enforceWhitelist;

    public BuiltinCommands(Server server, CommandService commands, Profiles profiles, BanList bans, Whitelist whitelist, Path settingsFile,
                           boolean enforceWhitelist) {
        this.server = server;
        this.commands = commands;
        this.profiles = profiles;
        this.bans = bans;
        this.whitelist = whitelist;
        this.settingsFile = settingsFile;
        this.enforceWhitelist = enforceWhitelist;
    }

    public void registerAll() {
        all().forEach(command -> commands.register(command.name(), command, command.aliases()));
    }

    List<ServerCommand> all() {
        Targets targets = new Targets(server);
        List<ServerCommand> all = new ArrayList<>();
        all.add(new SaveAllCommand(server));
        all.add(new StopCommand(server));
        all.add(new SeedCommand(targets));
        all.add(new OperatorCommand(true, server.permissions(), profiles, targets));
        all.add(new OperatorCommand(false, server.permissions(), profiles, targets));
        all.add(new KickCommand(targets));
        all.add(new BanCommand(bans, profiles, targets));
        all.add(new PardonCommand(bans));
        all.add(new BanIpCommand(server, bans, targets));
        all.add(new PardonIpCommand(bans));
        all.add(new BanListCommand(bans));
        all.add(new WhitelistCommand(server, whitelist, profiles, targets, settingsFile, enforceWhitelist));
        all.add(new GameModeCommand(targets));
        all.add(new DefaultGameModeCommand(server));
        all.add(new GiveCommand(targets));
        all.add(new ClearCommand(targets));
        all.add(new ExperienceCommand(targets));
        all.add(new EffectCommand(targets));
        all.add(new KillCommand(targets));
        all.add(new TeleportCommand(targets));
        all.add(new SpawnPointCommand(targets));
        all.add(new SummonCommand(targets));
        all.add(new TimeCommand(targets));
        all.add(new SetWorldSpawnCommand(targets));
        all.add(new DifficultyCommand(server));
        all.add(new WorldBorderCommand(targets));
        all.add(new SetBlockCommand(targets));
        all.add(new FillCommand(targets));
        all.add(new CloneCommand(targets));
        all.addAll(ChatCommands.create(server, targets));
        all.addAll(InfoCommands.create(server, commands));
        return all;
    }
}
