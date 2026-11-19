package space.nows.tube.api.entity.player;

import net.kyori.adventure.text.Component;

import java.util.Objects;
import java.util.UUID;

public record ResourcePack(UUID id, String url, String sha1, boolean required, Component prompt) {

    public ResourcePack {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(url, "url");
        sha1 = sha1 == null ? "" : sha1;
    }

    public static ResourcePack of(String url, String sha1) {
        return new ResourcePack(UUID.nameUUIDFromBytes(url.getBytes(java.nio.charset.StandardCharsets.UTF_8)), url, sha1, false, null);
    }
}
