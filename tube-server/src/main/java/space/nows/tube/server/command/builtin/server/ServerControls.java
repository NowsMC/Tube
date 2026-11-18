package space.nows.tube.server.command.builtin.server;

import space.nows.tube.server.AutosaveGate;

public record ServerControls(AutosaveGate autosave) {

    public static ServerControls inert() {
        return new ServerControls(new AutosaveGate());
    }
}
