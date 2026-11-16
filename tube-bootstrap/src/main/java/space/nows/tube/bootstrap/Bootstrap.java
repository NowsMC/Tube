package space.nows.tube.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Bootstrap {

    private static final String LIBRARY_LIST = "META-INF/tube/libraries.txt";
    private static final String SERVER_MAIN = "space.nows.tube.server.Main";
    private static final List<String> REPOSITORIES = List.of(
            "https://repo1.maven.org/maven2/",
            "https://repo.papermc.io/repository/maven-public/");

    private Bootstrap() {
    }

    public static void main(String[] args) throws Exception {
        Path self = Path.of(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        List<Library> libraries = Library.parse(readLibraryList());
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

    private static String readLibraryList() throws IOException {
        try (InputStream in = Bootstrap.class.getClassLoader().getResourceAsStream(LIBRARY_LIST)) {
            if (in == null) {
                throw new IllegalStateException(LIBRARY_LIST + " is missing from this jar");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
