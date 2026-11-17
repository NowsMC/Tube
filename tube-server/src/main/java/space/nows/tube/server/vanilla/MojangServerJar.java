package space.nows.tube.server.vanilla;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class MojangServerJar {

    public static final String RECIPES = "recipes";
    public static final String WORLDGEN = "worldgen";

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final String MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final Map<String, String> FOLDERS = folders();
    private static final Object LOCK = new Object();

    private MojangServerJar() {
    }

    private static Map<String, String> folders() {
        Map<String, String> folders = new LinkedHashMap<>();
        folders.put(RECIPES, "data/minecraft/recipe/");
        folders.put(WORLDGEN, "data/minecraft/worldgen/");
        return folders;
    }

    public static Path cacheFile(Path cache, String version, String data) {
        return cache.resolve(data + "-" + version + ".zip");
    }

    public static Path extracted(Path cache, String version, String data) throws IOException, InterruptedException {
        Path file = cacheFile(cache, version, data);
        if (Files.isRegularFile(file)) {
            return file;
        }
        synchronized (LOCK) {
            if (!Files.isRegularFile(file)) {
                download(cache, version);
            }
        }
        return file;
    }

    private static void download(Path cache, String version) throws IOException, InterruptedException {
        LOGGER.info("Downloading the Minecraft {} server jar from Mojang to read its recipes and world generation data", version);
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NORMAL).build();
        JsonObject manifest = json(http, MANIFEST);
        String versionUrl = null;
        for (var element : manifest.getAsJsonArray("versions")) {
            JsonObject entry = element.getAsJsonObject();
            if (entry.get("id").getAsString().equals(version)) {
                versionUrl = entry.get("url").getAsString();
            }
        }
        if (versionUrl == null) {
            throw new IOException("Mojang does not list Minecraft " + version);
        }
        JsonObject server = json(http, versionUrl).getAsJsonObject("downloads").getAsJsonObject("server");
        String sha1 = server.get("sha1").getAsString();
        Files.createDirectories(cache);
        Path jar = Files.createTempFile(cache, "mojang-" + version, ".jar");
        try {
            HttpResponse<Path> response = http.send(HttpRequest.newBuilder(URI.create(server.get("url").getAsString())).build(),
                    HttpResponse.BodyHandlers.ofFile(jar));
            if (response.statusCode() != 200) {
                throw new IOException("Mojang answered " + response.statusCode());
            }
            if (!sha1(jar).equalsIgnoreCase(sha1)) {
                throw new IOException("The downloaded server jar does not match Mojang's checksum");
            }
            for (Map.Entry<String, String> folder : FOLDERS.entrySet()) {
                Path target = cacheFile(cache, version, folder.getKey());
                if (Files.isRegularFile(target)) {
                    continue;
                }
                Path partial = Files.createTempFile(cache, folder.getKey() + "-" + version, ".zip");
                extract(jar, version, folder.getValue(), partial);
                Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Saved the Minecraft {} {} to {}", version, folder.getKey().equals(RECIPES) ? "recipes" : "world generation data", target);
            }
        } finally {
            Files.deleteIfExists(jar);
        }
    }

    public static void extract(Path jar, String version, String folder, Path target) throws IOException {
        try (ZipFile outer = new ZipFile(jar.toFile());
             OutputStream out = Files.newOutputStream(target);
             ZipOutputStream zip = new ZipOutputStream(out)) {
            ZipEntry nested = outer.getEntry("META-INF/versions/" + version + "/server-" + version + ".jar");
            if (nested == null) {
                copyFolder(outer, folder, zip);
                return;
            }
            byte[] inner;
            try (InputStream source = outer.getInputStream(nested)) {
                inner = source.readAllBytes();
            }
            try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(inner))) {
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    if (entry.getName().startsWith(folder) && entry.getName().endsWith(".json")) {
                        zip.putNextEntry(new ZipEntry(entry.getName().substring(folder.length())));
                        in.transferTo(zip);
                        zip.closeEntry();
                    }
                }
            }
        }
    }

    private static void copyFolder(ZipFile outer, String folder, ZipOutputStream zip) throws IOException {
        var entries = outer.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith(folder) && entry.getName().endsWith(".json")) {
                zip.putNextEntry(new ZipEntry(entry.getName().substring(folder.length())));
                try (InputStream in = outer.getInputStream(entry)) {
                    in.transferTo(zip);
                }
                zip.closeEntry();
            }
        }
    }

    private static JsonObject json(HttpClient http, String url) throws IOException, InterruptedException {
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(url + " answered " + response.statusCode());
        }
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static String sha1(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
