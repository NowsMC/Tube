package space.nows.tube.bukkit;

import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

public final class TubeBuildInfo implements ServerBuildInfo {

    private static final Key BRAND = Key.key("tube", "tube");

    private static volatile String tubeVersion = "unknown";
    private static volatile String minecraftVersion = "unknown";

    static void configure(String tube, String minecraft) {
        tubeVersion = tube;
        minecraftVersion = minecraft;
    }

    @Override
    public Key brandId() {
        return BRAND;
    }

    @Override
    public boolean isBrandCompatible(Key brandId) {
        return brandId.equals(BRAND);
    }

    @Override
    public String brandName() {
        return "Tube";
    }

    @Override
    public String minecraftVersionId() {
        return minecraftVersion;
    }

    @Override
    public String minecraftVersionName() {
        return minecraftVersion;
    }

    @Override
    public OptionalInt buildNumber() {
        return OptionalInt.empty();
    }

    @Override
    public Instant buildTime() {
        return Instant.EPOCH;
    }

    @Override
    public Optional<String> gitBranch() {
        return Optional.empty();
    }

    @Override
    public Optional<String> gitCommit() {
        return Optional.empty();
    }

    @Override
    public String asString(StringRepresentation representation) {
        return switch (representation) {
            case VERSION_SIMPLE -> tubeVersion;
            case VERSION_FULL -> "Tube " + tubeVersion + " (MC: " + minecraftVersion + ")";
        };
    }
}
