package space.nows.tube.server.command.builtin.server;

import space.nows.tube.api.command.CommandSender;
import space.nows.tube.server.AutosaveGate;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.CommandFailure;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

final class SaveOnCommand extends ServerCommand {

    private final AutosaveGate gate;

    SaveOnCommand(AutosaveGate gate) {
        super("save-on", Permissions.command("save-on"), "save-on");
        this.gate = gate;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        input.end();
        if (gate.enabled()) {
            throw new CommandFailure("Saving is already turned on");
        }
        gate.set(true);
        sender.sendMessage("Turned on world auto-saving");
    }
}
