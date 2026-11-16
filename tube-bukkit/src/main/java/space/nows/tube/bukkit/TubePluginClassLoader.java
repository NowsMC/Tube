package space.nows.tube.bukkit;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class TubePluginClassLoader extends URLClassLoader implements ConfiguredPluginClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private static final List<TubePluginClassLoader> LOADERS = new CopyOnWriteArrayList<>();

    private final Server server;
    private final PluginDescriptionFile description;
    private final File file;
    private final File dataFolder;
    private volatile JavaPlugin plugin;

    TubePluginClassLoader(Server server, PluginDescriptionFile description, File file, File dataFolder, ClassLoader parent) throws MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        this.server = server;
        this.description = description;
        this.file = file;
        this.dataFolder = dataFolder;
        LOADERS.add(this);
    }

    @Override
    public PluginMeta getConfiguration() {
        return description;
    }

    static void closeAll() {
        for (TubePluginClassLoader loader : List.copyOf(LOADERS)) {
            try {
                loader.close();
            } catch (IOException ignored) {
                // nothing else to do
            }
        }
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve, boolean checkGlobal, boolean checkLibraries) throws ClassNotFoundException {
        return loadClass(name, resolve);
    }

    @Override
    public void init(JavaPlugin plugin) {
        if (this.plugin != null) {
            throw new IllegalStateException("Plugin already initialized: " + description.getName());
        }
        this.plugin = plugin;
        plugin.init(server, description, dataFolder, file, this, description, PaperPluginLogger.getLogger(description));
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public PluginClassLoaderGroup getGroup() {
        return null;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException own) {
            for (TubePluginClassLoader other : LOADERS) {
                if (other != this) {
                    try {
                        return other.findLocal(name);
                    } catch (ClassNotFoundException ignored) {
                        // try the next plugin
                    }
                }
            }
            throw own;
        }
    }

    private Class<?> findLocal(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            return loaded != null ? loaded : super.findClass(name);
        }
    }

    @Override
    public void close() throws IOException {
        LOADERS.remove(this);
        super.close();
    }
}
