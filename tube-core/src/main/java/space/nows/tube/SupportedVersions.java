package space.nows.tube;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public final class SupportedVersions {

    private static final Properties PROPERTIES = load();

    private SupportedVersions() {
    }

    public static List<String> minecraft() {
        return Arrays.stream(PROPERTIES.getProperty("tube.minecraft.versions").split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    public static Optional<String> find(String id) {
        return minecraft().stream().filter(version -> version.equals(id)).findFirst();
    }

    public static String tube() {
        return PROPERTIES.getProperty("tube.version");
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = SupportedVersions.class.getResourceAsStream("/tube.properties")) {
            if (in == null) {
                throw new IllegalStateException("tube.properties is missing from the classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return properties;
    }
}
