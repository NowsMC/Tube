package space.nows.tube.server.command.builtin.server;

import space.nows.tube.server.AutosaveGate;
import space.nows.tube.server.Restarter;
import space.nows.tube.server.access.BanList;
import space.nows.tube.server.access.Whitelist;
import space.nows.tube.server.player.IdleKicker;

import java.util.function.IntSupplier;

public record ServerControls(AutosaveGate autosave, IdleKicker idleKicker, Whitelist whitelist, BanList bans, IntSupplier pluginReload,
                             Restarter restarter) {

    private static final Restarter UNAVAILABLE = new Restarter() {
        @Override
        public boolean available() {
            return false;
        }

        @Override
        public void restart() {
            throw new IllegalStateException("Restarting is not available");
        }
    };

    public static ServerControls inert(Whitelist whitelist, BanList bans) {
        return new ServerControls(new AutosaveGate(), new IdleKicker(0, System::currentTimeMillis), whitelist, bans, () -> 0, UNAVAILABLE);
    }
}
