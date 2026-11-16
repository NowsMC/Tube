package space.nows.tube.server.plugin;

import space.nows.tube.server.config.ConfigFormat;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class Jars {

    private Jars() {
    }

    static JarEntry findByBaseName(JarFile jar, String baseName) {
        for (ConfigFormat format : ConfigFormat.values()) {
            for (String extension : format.extensions()) {
                JarEntry entry = jar.getJarEntry(baseName + "." + extension);
                if (entry != null) {
                    return entry;
                }
            }
        }
        return null;
    }
}
