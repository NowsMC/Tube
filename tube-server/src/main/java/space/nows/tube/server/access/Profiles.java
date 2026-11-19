package space.nows.tube.server.access;

import space.nows.tube.api.entity.player.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class Profiles {

    public record Profile(UUID uuid, String name) {
    }

    private final Supplier<Collection<Player>> online;
    private final UserCache cache;

    public Profiles(Supplier<Collection<Player>> online, UserCache cache) {
        this.online = online;
        this.cache = cache;
    }

    public Optional<Profile> find(String name) {
        for (Player player : online.get()) {
            if (player.name().equalsIgnoreCase(name)) {
                return Optional.of(new Profile(player.uuid(), player.name()));
            }
        }
        return cache.find(name).map(uuid -> new Profile(uuid, cache.name(uuid).orElse(name)));
    }
}
