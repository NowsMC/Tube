package space.nows.tube.api.entity.player;

import java.util.Objects;

public record Skin(String textures, String signature) {

    public Skin {
        Objects.requireNonNull(textures, "textures");
    }
}
