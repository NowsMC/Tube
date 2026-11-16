package space.nows.tube.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

final class Eula {

    static final String FILE_NAME = "eula.txt";

    private Eula() {
    }

    static boolean isAccepted(Path directory) {
        Path file = directory.resolve(FILE_NAME);
        try {
            if (!Files.isRegularFile(file)) {
                Files.writeString(file, template(), StandardCharsets.UTF_8);
                return false;
            }
            Properties properties = new Properties();
            properties.load(new StringReader(Files.readString(file, StandardCharsets.UTF_8)));
            return Boolean.parseBoolean(properties.getProperty("eula", "false").strip());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String template() {
        String lineBreak = System.lineSeparator();
        return "#By changing the setting below to TRUE you are indicating your agreement to the Minecraft EULA (https://aka.ms/MinecraftEULA)." + lineBreak
                + "#" + DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()) + lineBreak
                + "eula=false" + lineBreak;
    }
}
