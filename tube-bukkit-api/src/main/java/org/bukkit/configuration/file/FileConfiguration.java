package org.bukkit.configuration.file;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class FileConfiguration extends MemoryConfiguration {

    public FileConfiguration() {
    }

    public void save(File file) throws IOException {
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }
        Files.writeString(file.toPath(), saveToString(), StandardCharsets.UTF_8);
    }

    public void save(String file) throws IOException {
        save(new File(file));
    }

    public abstract String saveToString();

    public void load(File file) throws IOException, InvalidConfigurationException {
        load(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
    }

    public void load(InputStream stream) throws IOException, InvalidConfigurationException {
        load(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public void load(Reader reader) throws IOException, InvalidConfigurationException {
        try (reader) {
            loadFromString(new String(readAll(reader)));
        }
    }

    public void load(String file) throws IOException, InvalidConfigurationException {
        load(new File(file));
    }

    public abstract void loadFromString(String contents) throws InvalidConfigurationException;

    @Override
    public FileConfigurationOptions options() {
        throw new UnsupportedOperationException("options() must be provided by the concrete configuration");
    }

    private static char[] readAll(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, read);
        }
        char[] result = new char[builder.length()];
        builder.getChars(0, builder.length(), result, 0);
        return result;
    }
}
