package space.nows.tube.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.zip.GZIPOutputStream;

final class LogRotation {

    static final String LATEST = "latest.log";

    private LogRotation() {
    }

    static void rotate(Path directory) {
        Path latest = directory.resolve(LATEST);
        try {
            if (!Files.isRegularFile(latest) || Files.size(latest) == 0) {
                return;
            }
            LocalDate date = Files.getLastModifiedTime(latest).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int index = 1;
            Path archive;
            do {
                archive = directory.resolve(date + "-" + index++ + ".log.gz");
            } while (Files.exists(archive));

            try (OutputStream out = new GZIPOutputStream(Files.newOutputStream(archive))) {
                Files.copy(latest, out);
            }
            Files.delete(latest);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
