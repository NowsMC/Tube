package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.zip.GZIPInputStream;

class LogRotationTest {

    @TempDir
    Path directory;

    @Test
    void compressesTheLatestLogUnderItsOwnDate() throws IOException {
        Path latest = directory.resolve(LogRotation.LATEST);
        Files.writeString(latest, "[10:00:00] [main/INFO]: hello\n");
        Files.setLastModifiedTime(latest, FileTime.from(LocalDate.of(2026, 11, 7).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        LogRotation.rotate(directory);

        assertFalse(Files.exists(latest));
        Path archive = directory.resolve("2026-11-07-1.log.gz");
        try (var in = new GZIPInputStream(Files.newInputStream(archive))) {
            assertEquals("[10:00:00] [main/INFO]: hello\n", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void picksTheNextFreeIndexForTheSameDay() throws IOException {
        Path latest = directory.resolve(LogRotation.LATEST);
        FileTime day = FileTime.from(LocalDate.of(2026, 11, 7).atStartOfDay(ZoneId.systemDefault()).toInstant());
        for (int i = 0; i < 2; i++) {
            Files.writeString(latest, "run " + i);
            Files.setLastModifiedTime(latest, day);
            LogRotation.rotate(directory);
        }
        assertTrue(Files.exists(directory.resolve("2026-11-07-1.log.gz")));
        assertTrue(Files.exists(directory.resolve("2026-11-07-2.log.gz")));
    }

    @Test
    void ignoresMissingOrEmptyLogs() throws IOException {
        LogRotation.rotate(directory);
        Files.writeString(directory.resolve(LogRotation.LATEST), "");
        LogRotation.rotate(directory);
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }
}
