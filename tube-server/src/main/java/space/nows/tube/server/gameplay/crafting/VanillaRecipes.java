package space.nows.tube.server.gameplay.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.server.vanilla.MojangServerJar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class VanillaRecipes {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final String RECIPE_FOLDER = "data/minecraft/recipe/";

    private VanillaRecipes() {
    }

    public static Path cacheFile(Path cache, String version) {
        return MojangServerJar.cacheFile(cache, version, MojangServerJar.RECIPES);
    }

    public static RecipeBook load(Path cache, String version) {
        try {
            return read(MojangServerJar.extracted(cache, version, MojangServerJar.RECIPES));
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

    static void extractRecipes(Path jar, String version, Path target) throws IOException {
        MojangServerJar.extract(jar, version, RECIPE_FOLDER, target);
    }
}
