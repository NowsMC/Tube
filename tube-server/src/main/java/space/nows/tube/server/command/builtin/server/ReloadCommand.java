package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.Server;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.event.ServerReloadEvent;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Typing;

import java.util.List;

final class ReloadCommand extends ServerCommand {

    private static final List<String> SCOPES = List.of("all", "files", "plugins");

    private final Server server;
    private final ServerControls controls;

    ReloadCommand(Server server, ServerControls controls) {
        super("reload", Permissions.ADMIN, "reload [files|plugins|all]");
        this.server = server;
        this.controls = controls;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        String scope = input.hasNext() ? input.keyword() : "files";
        input.end();
        boolean files = scope.equals("files") || scope.equals("all");
        boolean plugins = scope.equals("plugins") || scope.equals("all");
        if (!files && !plugins) {
            throw input.usage();
        }
        if (files) {
            controls.whitelist().reload();
            controls.bans().reload();
        }
        int enabled = plugins ? controls.pluginReload().getAsInt() : -1;
        server.events().post(new ServerReloadEvent(plugins));
        sender.sendMessage(describe(files, enabled));
    }

    private static String describe(boolean files, int enabled) {
        if (enabled < 0) {
            return "Reloaded the whitelist and the ban lists";
        }
        String plugins = enabled + (enabled == 1 ? " plugin" : " plugins");
        return files ? "Reloaded the whitelist, the ban lists and " + plugins : "Reloaded " + plugins;
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? Suggestions.matching(Typing.current(args), SCOPES) : List.of();
    }
}
