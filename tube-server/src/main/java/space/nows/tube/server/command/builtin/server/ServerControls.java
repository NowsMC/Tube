package space.nows.tube.server.command.builtin.server;

import space.nows.tube.server.AutosaveGate;
import space.nows.tube.server.player.IdleKicker;

public record ServerControls(AutosaveGate autosave, IdleKicker idleKicker) {

    public static ServerControls inert() {
        return new ServerControls(new AutosaveGate(), new IdleKicker(0, System::currentTimeMillis));
    }
}
