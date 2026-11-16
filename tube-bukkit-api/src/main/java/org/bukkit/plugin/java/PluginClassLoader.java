package org.bukkit.plugin.java;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PluginClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private static final List<PluginClassLoader> LOADERS = new CopyOnWriteArrayList<>();

    private final PluginDescriptionFile description;
    private final File file;
    private final File dataFolder;
    private volatile JavaPlugin plugin;

    public PluginClassLoader(ClassLoader parent, PluginDescriptionFile description, File dataFolder, File file) throws MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        this.description = description;
        this.file = file;
        this.dataFolder = dataFolder;
        LOADERS.add(this);
    }

    public static void closeAll() {
        for (PluginClassLoader loader : List.copyOf(LOADERS)) {
            try {
                loader.close();
            } catch (IOException ignored) {
                // nothing else can be done for a loader that cannot close
            }
        }
    }

    synchronized void initialize(JavaPlugin javaPlugin) {
        if (plugin != null) {
            throw new IllegalStateException("Plugin already initialized: " + description.getName());
        }
        plugin = javaPlugin;
        javaPlugin.init(Bukkit.getServer(), description, dataFolder, file, this);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException own) {
            for (PluginClassLoader other : LOADERS) {
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
