package space.nows.tube.server;

import space.nows.tube.server.config.ServerSettings;

final class EngineProperties {

    private EngineProperties() {
    }

    // Minestom reads these system properties once, when its ServerFlag class initializes, so they must be set before MinecraftServer.init.
    static void apply(ServerSettings settings) {
        System.setProperty("minestom.chunk-view-distance", Integer.toString(settings.level().viewDistance()));
        System.setProperty("minestom.accept-transfers", Boolean.toString(settings.connection().acceptsTransfers()));
        System.setProperty("minestom.auth.prevent-proxy-connections", Boolean.toString(settings.connection().preventProxyConnections()));
    }
}
