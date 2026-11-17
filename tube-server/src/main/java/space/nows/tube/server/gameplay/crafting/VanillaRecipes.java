package space.nows.tube.server.gameplay.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class VanillaRecipes {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final String MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    private static final String RECIPE_FOLDER = "data/minecraft/recipe/";

    private VanillaRecipes() {
    }

    public static Path cacheFile(Path cache, String version) {
        return cache.resolve("recipes-" + version + ".zip");
    }

    public static RecipeBook load(Path cache, String version) {
        Path file = cacheFile(cache, version);
        try {
            if (!Files.isRegularFile(file)) {
                download(cache, version, file);
            }
            return read(file);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warn("Could not load the vanilla recipes for Minecraft {}; crafting and smelting stay off until they can be downloaded: {}", version, e.toString());
            return RecipeBook.EMPTY;
        }
    }

    public static RecipeBook read(Path file) throws IOException {
        RecipeBook.Builder builder = RecipeBook.builder();
        try (ZipFile zip = new ZipFile(file.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".json")) {
                    continue;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                    builder.add(entry.getName().substring(0, entry.getName().length() - 5), json);
                }
            }
        }
        return builder.build();
    }

    private static void download(Path cache, String version, Path target) throws IOException, InterruptedException {
        LOGGER.info("Downloading the Minecraft {} server jar from Mojang to read its recipes", version);
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
            Path partial = Files.createTempFile(cache, "recipes-" + version, ".zip");
            extractRecipes(jar, version, partial);
            Files.move(partial, target, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Saved the Minecraft {} recipes to {}", version, target);
        } finally {
            Files.deleteIfExists(jar);
        }
    }

    static void extractRecipes(Path jar, String version, Path target) throws IOException {
        try (ZipFile outer = new ZipFile(jar.toFile());
             OutputStream out = Files.newOutputStream(target);
             ZipOutputStream zip = new ZipOutputStream(out)) {
            ZipEntry nested = outer.getEntry("META-INF/versions/" + version + "/server-" + version + ".jar");
            InputStream source = nested == null ? null : outer.getInputStream(nested);
            if (source == null) {
                copyRecipes(outer, zip);
                return;
            }
            byte[] inner;
            try (source) {
                inner = source.readAllBytes();
            }
            try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(inner))) {
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    if (entry.getName().startsWith(RECIPE_FOLDER) && entry.getName().endsWith(".json")) {
                        zip.putNextEntry(new ZipEntry(entry.getName().substring(RECIPE_FOLDER.length())));
                        in.transferTo(zip);
                        zip.closeEntry();
                    }
                }
            }
        }
    }

    private static void copyRecipes(ZipFile outer, ZipOutputStream zip) throws IOException {
        var entries = outer.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith(RECIPE_FOLDER) && entry.getName().endsWith(".json")) {
                zip.putNextEntry(new ZipEntry(entry.getName().substring(RECIPE_FOLDER.length())));
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
