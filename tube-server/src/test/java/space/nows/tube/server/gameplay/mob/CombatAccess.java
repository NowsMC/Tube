package space.nows.tube.server.gameplay.mob;

final class CombatAccess {

    private CombatAccess() {
    }

    static void install() {
        space.nows.tube.server.gameplay.TestHooks.installCombat(net.minestom.server.MinecraftServer.getGlobalEventHandler());
    }
}
