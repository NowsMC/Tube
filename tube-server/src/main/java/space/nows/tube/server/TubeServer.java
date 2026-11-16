package space.nows.tube.server;

import space.nows.tube.api.Server;

import java.util.Objects;

public final class TubeServer implements Server {

    private static final System.Logger LOGGER = System.getLogger("tube");

    private final VersionProfile profile;

    public TubeServer(VersionProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    @Override
    public String name() {
        return "Tube";
    }

    @Override
    public String minecraftVersion() {
        return profile.minecraftVersion();
    }

    public void start() {
        LOGGER.log(System.Logger.Level.INFO, "Starting {0} for Minecraft {1}", name(), minecraftVersion());
    }
}
