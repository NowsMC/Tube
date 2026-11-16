package space.nows.tube.bukkit;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;

final class BukkitPluginManager implements PluginManager {

    private final BukkitRuntime runtime;
    private final List<Plugin> plugins = new ArrayList<>();
    private final Map<String, Plugin> lookup = new HashMap<>();
    private final Map<String, Permission> permissions = new HashMap<>();
    private final Map<Boolean, Set<Permission>> defaultPermissions = new LinkedHashMap<>();

    BukkitPluginManager(BukkitRuntime runtime) {
        this.runtime = runtime;
        defaultPermissions.put(true, new LinkedHashSet<>());
        defaultPermissions.put(false, new LinkedHashSet<>());
    }

    PluginDescriptionFile describe(File file) throws InvalidDescriptionException {
        try (JarFile jar = new JarFile(file)) {
            var entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                throw new InvalidDescriptionException("Jar does not contain plugin.yml");
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                return new PluginDescriptionFile(stream);
            }
        } catch (IOException e) {
            throw new InvalidDescriptionException(e);
        }
    }

    Plugin[] loadPlugins(File[] files) {
        Arrays.sort(files);
        Map<String, File> candidates = new LinkedHashMap<>();
        Map<String, PluginDescriptionFile> descriptions = new LinkedHashMap<>();
        for (File file : files) {
            try {
                PluginDescriptionFile description = describe(file);
                String key = description.getName().toLowerCase(Locale.ROOT);
                if (candidates.containsKey(key)) {
                    runtime.julLogger.severe("Ambiguous plugin name `" + description.getName() + "' for files `" + file + "' and `" + candidates.get(key) + "'");
                    continue;
                }
                candidates.put(key, file);
                descriptions.put(key, description);
            } catch (InvalidDescriptionException e) {
                runtime.julLogger.log(Level.SEVERE, "Could not load '" + file.getPath() + "'", e);
            }
        }

        Map<String, Set<String>> after = new HashMap<>();
        descriptions.forEach((key, description) -> {
            Set<String> requirements = after.computeIfAbsent(key, ignored -> new LinkedHashSet<>());
            requirements.addAll(lower(description.getDepend()));
            lower(description.getSoftDepend()).stream().filter(descriptions::containsKey).forEach(requirements::add);
            lower(description.getLoadBefore()).stream().filter(descriptions::containsKey)
                    .forEach(other -> after.computeIfAbsent(other, ignored -> new LinkedHashSet<>()).add(key));
        });

        List<Plugin> loaded = new ArrayList<>();
        Set<String> done = new HashSet<>();
        Set<String> failed = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        for (String key : descriptions.keySet()) {
            load(key, candidates, descriptions, after, done, failed, visiting, loaded);
        }
        return loaded.toArray(new Plugin[0]);
    }

    private void load(String key, Map<String, File> files, Map<String, PluginDescriptionFile> descriptions, Map<String, Set<String>> after,
                      Set<String> done, Set<String> failed, Set<String> visiting, List<Plugin> loaded) {
        if (done.contains(key) || failed.contains(key)) {
            return;
        }
        PluginDescriptionFile description = descriptions.get(key);
        if (!visiting.add(key)) {
            runtime.julLogger.severe("Could not load '" + files.get(key) + "': circular dependency involving " + description.getName());
            failed.add(key);
            return;
        }
        for (String dependency : after.getOrDefault(key, Set.of())) {
            if (!descriptions.containsKey(dependency)) {
                if (!lookup.containsKey(dependency)) {
                    runtime.julLogger.severe("Could not load '" + files.get(key) + "': unknown/missing dependency plugin " + dependency);
                    failed.add(key);
                    visiting.remove(key);
                    return;
                }
                continue;
            }
            load(dependency, files, descriptions, after, done, failed, visiting, loaded);
            if (failed.contains(dependency) && lower(description.getDepend()).contains(dependency)) {
                failed.add(key);
                visiting.remove(key);
                return;
            }
        }
        try {
            loaded.add(instantiate(files.get(key), description));
            done.add(key);
        } catch (InvalidPluginException e) {
            runtime.julLogger.log(Level.SEVERE, "Could not load '" + files.get(key) + "'", e);
            failed.add(key);
        }
        visiting.remove(key);
    }

    private Plugin instantiate(File file, PluginDescriptionFile description) throws InvalidPluginException {
        PluginClassLoader loader = null;
        try {
            File dataFolder = new File(file.getParentFile(), description.getName());
            loader = new PluginClassLoader(BukkitPluginManager.class.getClassLoader(), description, dataFolder, file);
            Class<?> main = Class.forName(description.getMain(), true, loader);
            if (!JavaPlugin.class.isAssignableFrom(main)) {
                throw new InvalidPluginException("main class " + description.getMain() + " does not extend JavaPlugin");
            }
            Plugin plugin = (Plugin) main.getDeclaredConstructor().newInstance();
            synchronized (this) {
                plugins.add(plugin);
                lookup.put(description.getName().toLowerCase(Locale.ROOT), plugin);
            }
            return plugin;
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

    private static void closeQuietly(PluginClassLoader loader) {
        if (loader != null) {
            try {
                loader.close();
            } catch (IOException ignored) {
                // nothing else can be done
            }
        }
    }

    private static List<String> lower(List<String> names) {
        return names.stream().map(name -> name.toLowerCase(Locale.ROOT)).toList();
    }

    @Override
    public synchronized Plugin getPlugin(String name) {
        return lookup.get(name.replace(' ', '_').toLowerCase(Locale.ROOT));
    }

    @Override
    public synchronized Plugin[] getPlugins() {
        return plugins.toArray(new Plugin[0]);
    }

    @Override
    public boolean isPluginEnabled(String name) {
        return isPluginEnabled(getPlugin(name));
    }

    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        return plugin != null && plugins.contains(plugin) && plugin.isEnabled();
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (plugin.isEnabled() || !(plugin instanceof JavaPlugin java)) {
            return;
        }
        runtime.commands.register(plugin);
        plugin.getLogger().info("Enabling " + plugin.getDescription().getFullName());
        try {
            java.setEnabled(true);
        } catch (Throwable e) {
            runtime.julLogger.log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
            teardown(plugin);
            return;
        }
        callEvent(new PluginEnableEvent(plugin));
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (!plugin.isEnabled() || !(plugin instanceof JavaPlugin java)) {
            return;
        }
        plugin.getLogger().info("Disabling " + plugin.getDescription().getFullName());
        callEvent(new PluginDisableEvent(plugin));
        try {
            java.setEnabled(false);
        } catch (Throwable e) {
            runtime.julLogger.log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName() + " (Is it up to date?)", e);
        }
        teardown(plugin);
    }

    private void teardown(Plugin plugin) {
        runtime.scheduler.cancelTasks(plugin);
        runtime.services.unregisterAll(plugin);
        HandlerList.unregisterAll(plugin);
        runtime.commands.unregister(plugin);
        if (plugin.getClass().getClassLoader() instanceof PluginClassLoader loader) {
            closeQuietly(loader);
        }
    }

    @Override
    public void disablePlugins() {
        Plugin[] snapshot = getPlugins();
        for (int i = snapshot.length - 1; i >= 0; i--) {
            try {
                disablePlugin(snapshot[i]);
            } catch (Throwable e) {
                runtime.julLogger.log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + snapshot[i].getName(), e);
            }
        }
    }

    @Override
    public void callEvent(Event event) {
        for (RegisteredListener registration : event.getHandlers().getRegisteredListeners()) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }
            try {
                registration.callEvent(event);
            } catch (Throwable e) {
                runtime.julLogger.log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), e);
            }
        }
    }

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }
        Set<Method> methods = new HashSet<>();
        try {
            methods.addAll(Set.of(listener.getClass().getMethods()));
            methods.addAll(Set.of(listener.getClass().getDeclaredMethods()));
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for " + listener.getClass() + " because " + e.getMessage() + " does not exist.");
            return;
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
            registerEvent(eventType, listener, handler.priority(), reflect(method, eventType), plugin, handler.ignoreCancelled());
        }
    }

    private static EventExecutor reflect(Method method, Class<? extends Event> eventType) {
        return (target, event) -> {
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
    }

    @Override
    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin) {
        registerEvent(event, listener, priority, executor, plugin, false);
    }

    @Override
    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority, EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + event + " while not enabled");
        }
        handlerList(registrationClass(event)).register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
    }

    private static HandlerList handlerList(Class<? extends Event> type) {
        try {
            Method method = type.getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalPluginAccessException("Error while registering listener for event type " + type + ": " + e);
        }
    }

    private static Class<? extends Event> registrationClass(Class<? extends Event> type) {
        try {
            type.getDeclaredMethod("getHandlerList");
            return type;
        } catch (NoSuchMethodException e) {
            Class<?> parent = type.getSuperclass();
            if (parent != null && parent != Event.class && Event.class.isAssignableFrom(parent)) {
                return registrationClass(parent.asSubclass(Event.class));
            }
            throw new IllegalPluginAccessException("Unable to find handler list for event " + type.getName() + ". Static getHandlerList method required!");
        }
    }

    @Override
    public synchronized Permission getPermission(String name) {
        return permissions.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public synchronized void addPermission(Permission permission) {
        String name = permission.getName().toLowerCase(Locale.ROOT);
        if (permissions.containsKey(name)) {
            throw new IllegalArgumentException("The permission " + name + " is already defined!");
        }
        permissions.put(name, permission);
        switch (permission.getDefault()) {
            case TRUE -> {
                defaultPermissions.get(true).add(permission);
                defaultPermissions.get(false).add(permission);
            }
            case OP -> defaultPermissions.get(true).add(permission);
            case NOT_OP -> defaultPermissions.get(false).add(permission);
            default -> {
            }
        }
    }

    @Override
    public synchronized void removePermission(Permission permission) {
        removePermission(permission.getName());
    }

    @Override
    public synchronized void removePermission(String name) {
        Permission removed = permissions.remove(name.toLowerCase(Locale.ROOT));
        if (removed != null) {
            defaultPermissions.values().forEach(set -> set.remove(removed));
        }
    }

    @Override
    public synchronized Set<Permission> getDefaultPermissions(boolean op) {
        return Set.copyOf(defaultPermissions.get(op));
    }

    @Override
    public synchronized Set<Permission> getPermissions() {
        return Set.copyOf(permissions.values());
    }
}
