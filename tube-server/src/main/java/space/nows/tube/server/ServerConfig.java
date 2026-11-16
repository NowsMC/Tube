package space.nows.tube.server;

public record ServerConfig(String host, int port, boolean onlineMode) {

    public static ServerConfig fromSystemProperties() {
        return new ServerConfig(
                System.getProperty("tube.host", "0.0.0.0"),
                Integer.getInteger("tube.port", 25565),
                Boolean.parseBoolean(System.getProperty("tube.online", "true")));
    }
}
