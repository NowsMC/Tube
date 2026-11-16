package space.nows.tube.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;

public final class TubePluginLoader implements PluginLoader {

    private static final Pattern[] FILE_FILTERS = {Pattern.compile("\\.jar$")};

    private final Server explicitServer;

    public TubePluginLoader() {
        this.explicitServer = null;
    }

    public TubePluginLoader(Server server) {
        this.explicitServer = server;
    }

    private Server server() {
        return explicitServer != null ? explicitServer : Bukkit.getServer();
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        PluginDescriptionFile description;
        try {
            description = getPluginDescription(file);
        } catch (InvalidDescriptionException e) {
            throw new InvalidPluginException(e);
        }

        File dataFolder = new File(file.getParentFile(), description.getName());
        TubePluginClassLoader loader = null;
        try {
            loader = new TubePluginClassLoader(server(), description, file, dataFolder, TubePluginLoader.class.getClassLoader());
            Class<?> main = Class.forName(description.getMain(), true, loader);
            if (!JavaPlugin.class.isAssignableFrom(main)) {
                throw new InvalidPluginException("main class " + description.getMain() + " does not extend JavaPlugin");
            }
            return (JavaPlugin) main.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException e) {
            closeQuietly(loader);
            throw new InvalidPluginException(e.getCause() == null ? e : e.getCause());
        } catch (ClassNotFoundException e) {
            closeQuietly(loader);
            throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", e);
        } catch (ReflectiveOperationException | IOException | LinkageError e) {
            closeQuietly(loader);
            throw new InvalidPluginException(e);
        }
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        try (JarFile jar = new JarFile(file)) {
            var entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                if (jar.getJarEntry("paper-plugin.yml") != null) {
                    throw new InvalidDescriptionException("paper-plugin.yml is not supported by Tube yet");
                }
                throw new InvalidDescriptionException(new FileNotFoundException("Jar does not contain plugin.yml"));
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                return new PluginDescriptionFile(stream);
            }
        } catch (IOException e) {
            throw new InvalidDescriptionException(e);
        }
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return FILE_FILTERS.clone();
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        Map<Class<? extends Event>, Set<RegisteredListener>> result = new HashMap<>();
        Set<Method> methods = new HashSet<>();
        try {
            methods.addAll(Set.of(listener.getClass().getMethods()));
            methods.addAll(Set.of(listener.getClass().getDeclaredMethods()));
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
            return result;
        }

        for (Method method : methods) {
            EventHandler handler = method.getAnnotation(EventHandler.class);
            if (handler == null || method.isBridge() || method.isSynthetic()) {
                continue;
            }
            if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                plugin.getLogger().severe(plugin.getDescription().getFullName() + " attempted to register an invalid EventHandler method signature \"" + method.toGenericString() + "\" in " + listener.getClass());
                continue;
            }
            Class<? extends Event> eventType = method.getParameterTypes()[0].asSubclass(Event.class);
            method.setAccessible(true);
            EventExecutor executor = (target, event) -> {
                try {
                    if (eventType.isAssignableFrom(event.getClass())) {
                        method.invoke(target, event);
                    }
                } catch (InvocationTargetException e) {
                    throw new EventException(e.getCause());
                } catch (Throwable e) {
                    throw new EventException(e);
                }
            };
            result.computeIfAbsent(eventType, key -> new HashSet<>())
                    .add(new RegisteredListener(listener, executor, handler.priority(), plugin, handler.ignoreCancelled()));
        }
        return result;
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (!(plugin instanceof JavaPlugin java)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }
        if (plugin.isEnabled()) {
            return;
        }
        plugin.getLogger().info("Enabling " + plugin.getDescription().getFullName());
        try {
            java.setEnabled(true);
        } catch (Throwable e) {
            server().getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
            try {
                java.setEnabled(false);
            } catch (Throwable ignored) {
                // already failed once
            }
            if (plugin.getClass().getClassLoader() instanceof TubePluginClassLoader loader) {
                closeQuietly(loader);
            }
            return;
        }
        server().getPluginManager().callEvent(new PluginEnableEvent(plugin));
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (!(plugin instanceof JavaPlugin java)) {
            throw new IllegalArgumentException("Plugin is not associated with this PluginLoader");
        }
        if (!plugin.isEnabled()) {
            return;
        }
        plugin.getLogger().info("Disabling " + plugin.getDescription().getFullName());
        server().getPluginManager().callEvent(new PluginDisableEvent(plugin));
        try {
            java.setEnabled(false);
        } catch (Throwable e) {
            server().getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
        }
        if (plugin.getClass().getClassLoader() instanceof TubePluginClassLoader loader) {
            closeQuietly(loader);
        }
    }

    private static void closeQuietly(TubePluginClassLoader loader) {
        if (loader != null) {
            try {
                loader.close();
            } catch (IOException ignored) {
                // nothing else to do
            }
        }
    }

    private static final class FileNotFoundException extends IOException {

        FileNotFoundException(String message) {
            super(message);
        }
    }
}
