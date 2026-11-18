package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.AutosaveGate;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;

final class SaveOffCommand extends ServerCommand {

    private final AutosaveGate gate;

    SaveOffCommand(AutosaveGate gate) {
        super("save-off", Permissions.command("save-off"), "save-off");
        this.gate = gate;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        input.end();
        if (!gate.enabled()) {
            throw new CommandFailure("Saving is already turned off");
        }
        gate.set(false);
        sender.sendMessage("Turned off world auto-saving");
    }
}
