package space.nows.tube.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class Bootstrap {

    private static final String LIBRARY_LIST = "META-INF/tube/libraries.txt";
    private static final String BUKKIT_LIBRARY_LIST = "META-INF/tube/libraries-bukkit.txt";
    private static final String BUKKIT_PINS = "META-INF/tube/pins-bukkit.txt";
    private static final String SERVER_MAIN = "space.nows.tube.server.Main";
    private static final List<String> REPOSITORIES = List.of(
            "https://repo1.maven.org/maven2/",
            "https://repo.papermc.io/repository/maven-public/",
            "https://hub.spigotmc.org/nexus/content/repositories/snapshots/");

    private Bootstrap() {
    }

    public static void main(String[] args) throws Exception {
        Path self = Path.of(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<Library> libraries = new ArrayList<>(Library.parse(readList(LIBRARY_LIST, true)));
        if (hasBukkitPlugins(Path.of("plugins"))) {
            Set<String> present = new HashSet<>();
            libraries.forEach(library -> present.add(library.key()));
            Map<String, Library> optional = new LinkedHashMap<>();
            Library.parse(readList(BUKKIT_LIBRARY_LIST, false)).forEach(library -> optional.put(library.key(), library));
            Library.parse(readList(BUKKIT_PINS, false)).forEach(library -> optional.put(library.key(), library));
            for (Library library : optional.values()) {
                if (present.add(library.key())) {
                    libraries.add(library);
                }
            }
        }
        List<Path> jars = new LibraryResolver(Path.of("libraries"), REPOSITORIES).resolve(libraries);

        List<URL> urls = new ArrayList<>();
        urls.add(self.toUri().toURL());
        for (Path jar : jars) {
            urls.add(jar.toUri().toURL());
        }

        URLClassLoader loader = new URLClassLoader("tube", urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class.forName(SERVER_MAIN, true, loader).getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    static boolean hasBukkitPlugins(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return false;
        }
        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.filter(path -> path.getFileName().toString().endsWith(".jar")).toList()) {
                try (JarFile jar = new JarFile(file.toFile())) {
                    if (jar.getEntry("plugin.yml") != null) {
                        return true;
                    }
                } catch (IOException ignored) {
                    // an unreadable jar is reported later by the server itself
                }
            }
        }
        return false;
    }

    private static String readList(String resource, boolean required) throws IOException {
        try (InputStream in = Bootstrap.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                if (required) {
                    throw new IllegalStateException(resource + " is missing from this jar");
                }
                return "";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
