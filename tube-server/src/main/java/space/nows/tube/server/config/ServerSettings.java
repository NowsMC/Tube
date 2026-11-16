package space.nows.tube.server.config;

import space.nows.tube.api.entity.GameMode;

public record ServerSettings(
        Connection connection,
        Status status,
        Level level,
        Gameplay gameplay,
        Access access,
        Rcon rcon,
        Query query,
        ResourcePack resourcePack) {

    public record Connection(String serverIp, int port, boolean onlineMode, int maxPlayers, int compressionThreshold, boolean logIps) {

        public String bindAddress() {
            return serverIp.isBlank() ? "0.0.0.0" : serverIp;
        }
    }

    public record Status(String motd, boolean enabled, boolean hideOnlinePlayers) {
    }

    public record Level(String name, String type, int viewDistance, int spawnProtection, int autosaveSeconds) {
    }

    public record Gameplay(GameMode gamemode, boolean forceGamemode, Difficulty difficulty, boolean hardcore) {
    }

    public record Access(boolean whitelist, boolean enforceWhitelist) {
    }

    public record Rcon(boolean enabled, int port, String password, boolean broadcastToOps) {
    }

    public record Query(boolean enabled, int port) {
    }

    public record ResourcePack(String url, String sha1, String id, String prompt, boolean required) {
    }

    public enum Difficulty {
        PEACEFUL,
        EASY,
        NORMAL,
        HARD
    }
}
