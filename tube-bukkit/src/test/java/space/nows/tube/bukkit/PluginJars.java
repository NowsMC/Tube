package space.nows.tube.bukkit;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

final class PluginJars {

    private PluginJars() {
    }

    static Path build(Path directory, String jarName, String descriptor, String className, String source, String... resources) throws IOException {
        Path classes = Files.createDirectories(directory.resolve("classes-" + jarName));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject unit = new SimpleJavaFileObject(URI.create("string:///" + className.replace('.', '/') + ".java"), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return source;
            }
        };
        try (StandardJavaFileManager files = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8)) {
            var task = compiler.getTask(null, files, null,
                    List.of("-d", classes.toString(), "-classpath", System.getProperty("java.class.path"), "-proc:none"), null, List.of(unit));
            if (!task.call()) {
                throw new IllegalStateException("Could not compile test plugin " + className);
            }
        }

        Path jar = directory.resolve(jarName);
        try (OutputStream out = Files.newOutputStream(jar); JarOutputStream jarOut = new JarOutputStream(out)) {
            jarOut.putNextEntry(new JarEntry("plugin.yml"));
            jarOut.write(descriptor.getBytes(StandardCharsets.UTF_8));
            jarOut.closeEntry();
            for (int i = 0; i + 1 < resources.length; i += 2) {
                jarOut.putNextEntry(new JarEntry(resources[i]));
                jarOut.write(resources[i + 1].getBytes(StandardCharsets.UTF_8));
                jarOut.closeEntry();
            }
            try (Stream<Path> files = Files.walk(classes)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    jarOut.putNextEntry(new JarEntry(classes.relativize(file).toString().replace('\\', '/')));
                    jarOut.write(Files.readAllBytes(file));
                    jarOut.closeEntry();
                }
            }
        }
        return jar;
    }
}
