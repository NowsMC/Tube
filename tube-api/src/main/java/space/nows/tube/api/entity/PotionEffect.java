package space.nows.tube.api.entity;

import java.util.Objects;

public record PotionEffect(String type, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {

    public PotionEffect {
        Objects.requireNonNull(type, "type");
        type = type.indexOf(':') < 0 ? "minecraft:" + type : type;
    }

    public static PotionEffect of(String type, int durationTicks, int amplifier) {
        return new PotionEffect(type, durationTicks, amplifier, false, true, true);
    }
}
