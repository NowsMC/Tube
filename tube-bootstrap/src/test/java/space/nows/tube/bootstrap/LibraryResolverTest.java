package space.nows.tube.bootstrap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

class LibraryResolverTest {

    private static final Library LIBRARY = new Library("org.example", "thing", "1.0", "");
    private static final byte[] CONTENT = "jar-bytes".getBytes(StandardCharsets.UTF_8);

    @TempDir
    Path directory;

    @Test
    void downloadsFromFirstRepositoryThatHasTheJar() throws Exception {
        Path empty = Files.createDirectories(directory.resolve("empty"));
        Path repository = publish("repo", CONTENT, sha1(CONTENT));
        Path libraries = directory.resolve("libraries");

        List<Path> jars = new LibraryResolver(libraries, List.of(empty.toUri().toString(), repository.toUri().toString())).resolve(List.of(LIBRARY));

        assertArrayEquals(CONTENT, Files.readAllBytes(jars.getFirst()));
        assertTrue(Files.isRegularFile(Path.of(jars.getFirst() + ".sha1")));
    }

    @Test
    void redownloadsWhenTheStoredFileIsCorrupted() throws Exception {
        Path repository = publish("repo", CONTENT, sha1(CONTENT));
        LibraryResolver resolver = new LibraryResolver(directory.resolve("libraries"), List.of(repository.toUri().toString()));
        Path jar = resolver.resolve(List.of(LIBRARY)).getFirst();

        Files.write(jar, new byte[] {1, 2, 3});
        resolver.resolve(List.of(LIBRARY));

        assertArrayEquals(CONTENT, Files.readAllBytes(jar));
    }

    @Test
    void rejectsAChecksumMismatch() throws Exception {
        Path repository = publish("repo", CONTENT, sha1("other".getBytes(StandardCharsets.UTF_8)));
        LibraryResolver resolver = new LibraryResolver(directory.resolve("libraries"), List.of(repository.toUri().toString()));
        assertThrows(IOException.class, () -> resolver.resolve(List.of(LIBRARY)));
        assertTrue(Files.notExists(directory.resolve("libraries").resolve(LIBRARY.path())));
    }

    private Path publish(String name, byte[] content, String checksum) throws IOException {
        Path root = Files.createDirectories(directory.resolve(name));
        Path jar = root.resolve(LIBRARY.path());
        Files.createDirectories(jar.getParent());
        Files.write(jar, content);
        Files.writeString(Path.of(jar + ".sha1"), checksum);
        return root;
    }

    private static String sha1(byte[] content) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(content));
    }
}
