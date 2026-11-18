package space.nows.tube.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

final class LibraryResolver {

    private static final int TIMEOUT_MILLIS = 30_000;

    private final Path directory;
    private final List<String> repositories;
    private boolean announced;

    LibraryResolver(Path directory, List<String> repositories) {
        this.directory = directory;
        this.repositories = repositories.stream().map(url -> url.endsWith("/") ? url : url + "/").toList();
    }

    List<Path> resolve(List<Library> libraries) throws IOException {
        List<Path> jars = new ArrayList<>();
        for (Library library : libraries) {
            jars.add(resolve(library));
        }
        return jars;
    }

    private Path resolve(Library library) throws IOException {
        Path jar = directory.resolve(library.path());
        Path checksum = Path.of(jar + ".sha1");
        if (isIntact(jar, checksum)) {
            return jar;
        }

        IOException failure = null;
        for (String repository : repositories) {
            try {
                download(repository, library, jar, checksum);
                return jar;
            } catch (IOException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (Files.isRegularFile(jar) && !Files.exists(checksum)) {
            System.err.println("[tube] Could not verify " + library.coordinates() + ", using existing file");
            return jar;
        }
        throw new IOException("Could not download " + library.coordinates(), failure);
    }

    private static boolean isIntact(Path jar, Path checksum) throws IOException {
        if (!Files.isRegularFile(jar) || !Files.isRegularFile(checksum)) {
            return false;
        }
        String expected = Files.readString(checksum, StandardCharsets.US_ASCII).strip();
        return expected.equalsIgnoreCase(sha1(jar));
    }

    private void download(String repository, Library library, Path jar, Path checksum) throws IOException {
        URI source = URI.create(repository + library.path());
        String expected = readChecksum(URI.create(source + ".sha1"));

        Files.createDirectories(jar.getParent());
        Path partial = Files.createTempFile(jar.getParent(), jar.getFileName().toString(), ".part");
        try {
            if (!announced) {
                System.out.println("[tube] Preparing libraries...");
                announced = true;
            }
            try (InputStream in = open(source)) {
                Files.copy(in, partial, StandardCopyOption.REPLACE_EXISTING);
            }
            String actual = sha1(partial);
            if (!actual.equalsIgnoreCase(expected)) {
                throw new IOException("Checksum mismatch for " + library.coordinates() + ": expected " + expected + " but was " + actual);
            }
            Files.move(partial, jar, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            Files.writeString(checksum, expected, StandardCharsets.US_ASCII);
        } finally {
            Files.deleteIfExists(partial);
        }
    }

    private static String readChecksum(URI source) throws IOException {
        try (InputStream in = open(source)) {
            String text = new String(in.readAllBytes(), StandardCharsets.US_ASCII).strip();
            int space = text.indexOf(' ');
            return space < 0 ? text : text.substring(0, space);
        }
    }

    private static InputStream open(URI source) throws IOException {
        URLConnection connection = source.toURL().openConnection();
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
        if (connection instanceof HttpURLConnection http && http.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(source + " returned HTTP " + http.getResponseCode());
        }
        return connection.getInputStream();
    }

    private static String sha1(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
