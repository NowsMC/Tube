package space.nows.tube.server.access;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

final class JsonFiles {

    static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonFiles() {
    }

    static JsonElement read(Path file) {
        try {
            return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not parse " + file + ": " + e.getMessage(), e);
        }
    }

    static JsonArray readArray(Path file) {
        return Files.isRegularFile(file) ? read(file).getAsJsonArray() : new JsonArray();
    }

    static void write(Path file, JsonElement content) {
        try {
            Files.writeString(file, GSON.toJson(content) + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String now() {
        return TIMESTAMP.format(ZonedDateTime.now());
    }

    static Optional<ZonedDateTime> parseTimestamp(String text) {
        try {
            return Optional.of(ZonedDateTime.parse(text, TIMESTAMP.withZone(ZoneId.systemDefault())));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
