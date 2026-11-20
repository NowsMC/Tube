package space.nows.tube.server.gameplay;

import net.minestom.server.event.GlobalEventHandler;

public final class TestHooks {

    private TestHooks() {
    }

    public static void installCombat(GlobalEventHandler handler) {
        Combat.install(handler);
    }

    public static void installFalling(GlobalEventHandler handler) {
        Falling.install(handler);
    }
}
