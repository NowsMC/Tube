package space.nows.tube.api;

import java.nio.file.Path;

public interface ServerExtension {

    String name();

    void enable(Server server, Path pluginsDirectory) throws Exception;

    void disable();
}
