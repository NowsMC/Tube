package space.nows.tube.server;

import space.nows.tube.api.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

final class Profiles {

    record Profile(UUID uuid, String name) {
    }

    private final Supplier<Collection<Player>> online;
    private final UserCache cache;

    Profiles(Supplier<Collection<Player>> online, UserCache cache) {
        this.online = online;
        this.cache = cache;
    }

    Optional<Profile> find(String name) {
        for (Player player : online.get()) {
            if (player.name().equalsIgnoreCase(name)) {
                return Optional.of(new Profile(player.uuid(), player.name()));
            }
        }
        return cache.find(name).map(uuid -> new Profile(uuid, cache.name(uuid).orElse(name)));
    }
}
