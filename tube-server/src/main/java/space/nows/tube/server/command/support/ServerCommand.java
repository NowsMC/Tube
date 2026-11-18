package space.nows.tube.server.command.support;

import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandSender;

import java.util.List;

public abstract class ServerCommand implements Command {

    private final String name;
    private final String permission;
    private final String usage;
    private final String[] aliases;

    protected ServerCommand(String name, String permission, String usage, String... aliases) {
        this.name = name;
        this.permission = permission;
        this.usage = usage;
        this.aliases = aliases;
    }

    public String name() {
        return name;
    }

    public String[] aliases() {
        return aliases.clone();
    }

    @Override
    public String permission() {
        return permission;
    }

    @Override
    public String usage() {
        return usage;
    }

    @Override
    public final void execute(CommandSender sender, List<String> args) {
        space.nows.tube.api.command.CommandInput input = new CommandInput(args, usage);
        try {
            run(sender, input);
        } catch (space.nows.tube.api.command.CommandFailure | IllegalArgumentException failure) {
            sender.sendMessage(failure.getMessage());
        }
    }

    protected void run(CommandSender sender, space.nows.tube.api.command.CommandInput input) {
        run(sender, (CommandInput) input);
    }

    protected void run(CommandSender sender, CommandInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return List.of();
    }
}
