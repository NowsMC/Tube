package space.nows.tube.server.command;

import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.plugin.PluginCommand;

import java.util.List;

final class DeclaredCommand implements Command {

    private final Command command;
    private final PluginCommand declared;

    DeclaredCommand(Command command, PluginCommand declared) {
        this.command = command;
        this.declared = declared;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        command.execute(sender, args);
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return command.suggest(sender, args);
    }

    @Override
    public String permission() {
        return command.permission() != null ? command.permission() : declared.permission();
    }

    @Override
    public String description() {
        return command.description() != null ? command.description() : declared.description();
    }

    @Override
    public String usage() {
        return command.usage() != null ? command.usage() : declared.usage();
    }

    @Override
    public String permissionMessage() {
        return command.permissionMessage() != null ? command.permissionMessage() : declared.permissionMessage();
    }
}
