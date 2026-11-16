package space.nows.tube.api.entity;

import java.util.Objects;

public record Skin(String textures, String signature) {

    public Skin {
        Objects.requireNonNull(textures, "textures");
    }
}
