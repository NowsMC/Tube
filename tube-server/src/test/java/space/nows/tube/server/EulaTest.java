package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class EulaTest {

    @TempDir
    Path directory;

    @Test
    void firstRunCreatesTheFileAndIsNotAccepted() throws IOException {
        assertFalse(Eula.isAccepted(directory));
        String content = Files.readString(directory.resolve(Eula.FILE_NAME));
        assertTrue(content.contains("https://aka.ms/MinecraftEULA"));
        assertTrue(content.contains("eula=false"));
    }

    @Test
    void staysNotAcceptedUntilTheValueIsTrue() throws IOException {
        Eula.isAccepted(directory);
        assertFalse(Eula.isAccepted(directory));

        Files.writeString(directory.resolve(Eula.FILE_NAME), "eula=true\n");
        assertTrue(Eula.isAccepted(directory));
    }
}
