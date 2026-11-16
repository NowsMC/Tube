package space.nows.tube.bukkit;

import io.papermc.paper.plugin.PermissionManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;

final class TubePluginManager implements PluginManager {

    private final Server server;
    private final SimpleCommandMap commandMap;
    private final PluginLoader loader;
    private final List<Plugin> plugins = new ArrayList<>();
    private final Map<String, Plugin> lookup = new HashMap<>();
    private final Map<String, Permission> permissions = new HashMap<>();
    private final Map<Boolean, Set<Permission>> defaultPermissions = new LinkedHashMap<>();
    private final Map<String, Map<Permissible, Boolean>> permissionSubscriptions = new HashMap<>();
    private final Map<Boolean, Map<Permissible, Boolean>> defaultSubscriptions = new HashMap<>();

    TubePluginManager(Server server, SimpleCommandMap commandMap) {
        this.server = server;
        this.commandMap = commandMap;
        this.loader = new TubePluginLoader(server);
        defaultPermissions.put(true, new LinkedHashSet<>());
        defaultPermissions.put(false, new LinkedHashSet<>());
    }

    @Override
    public void registerInterface(Class<? extends PluginLoader> loaderClass) {
        if (!loaderClass.isAssignableFrom(TubePluginLoader.class) && !TubePluginLoader.class.isAssignableFrom(loaderClass)) {
            throw new IllegalArgumentException("Tube only supports its own plugin loader, not " + loaderClass.getName());
        }
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
    public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        Plugin plugin = loader.loadPlugin(file);
        synchronized (this) {
            plugins.add(plugin);
            lookup.put(plugin.getName().toLowerCase(Locale.ROOT), plugin);
        }
        return plugin;
    }

    @Override
    public Plugin[] loadPlugins(File directory) {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        return loadPlugins(files == null ? new File[0] : files);
    }

    @Override
    public Plugin[] loadPlugins(File[] files) {
        Map<String, File> candidates = new LinkedHashMap<>();
        Map<String, PluginDescriptionFile> descriptions = new LinkedHashMap<>();
        Arrays.sort(files);
        for (File file : files) {
            try {
                PluginDescriptionFile description = loader.getPluginDescription(file);
                String key = description.getName().toLowerCase(Locale.ROOT);
                if (candidates.containsKey(key)) {
                    server.getLogger().severe("Ambiguous plugin name `" + description.getName() + "' for files `" + file + "' and `" + candidates.get(key) + "' in `" + file.getParentFile() + "'");
                    continue;
                }
                candidates.put(key, file);
                descriptions.put(key, description);
            } catch (InvalidDescriptionException e) {
                if (!isBukkitDescriptorMissing(file)) {
                    server.getLogger().log(Level.SEVERE, "Could not load '" + file.getPath() + "' in folder '" + file.getParentFile().getPath() + "'", e);
                }
            }
        }

        Map<String, Set<String>> after = new HashMap<>();
        for (var entry : descriptions.entrySet()) {
            after.computeIfAbsent(entry.getKey(), key -> new LinkedHashSet<>()).addAll(lower(entry.getValue().getDepend()));
            for (String soft : lower(entry.getValue().getSoftDepend())) {
                if (descriptions.containsKey(soft)) {
                    after.get(entry.getKey()).add(soft);
                }
            }
            for (String before : lower(entry.getValue().getLoadBefore())) {
                if (descriptions.containsKey(before)) {
                    after.computeIfAbsent(before, key -> new LinkedHashSet<>()).add(entry.getKey());
                }
            }
        }

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
            server.getLogger().severe("Could not load '" + files.get(key) + "': circular dependency involving " + description.getName());
            failed.add(key);
            return;
        }
        for (String dependency : after.getOrDefault(key, Set.of())) {
            if (!descriptions.containsKey(dependency)) {
                if (lookup.containsKey(dependency)) {
                    continue;
                }
                server.getLogger().log(Level.SEVERE, "Could not load '" + files.get(key) + "' in folder '" + files.get(key).getParentFile() + "'",
                        new UnknownDependencyException("Unknown/missing dependency plugins: [" + dependency + "]. Please download and install these plugins to run '" + description.getName() + "'."));
                failed.add(key);
                visiting.remove(key);
                return;
            }
            load(dependency, files, descriptions, after, done, failed, visiting, loaded);
            if (failed.contains(dependency) && lower(description.getDepend()).contains(dependency)) {
                failed.add(key);
                visiting.remove(key);
                return;
            }
        }
        try {
            loaded.add(loadPlugin(files.get(key)));
            done.add(key);
        } catch (InvalidPluginException | InvalidDescriptionException | RuntimeException e) {
            server.getLogger().log(Level.SEVERE, "Could not load '" + files.get(key) + "' in folder '" + files.get(key).getParentFile() + "'", e);
            failed.add(key);
        }
        visiting.remove(key);
    }

    private static List<String> lower(List<String> names) {
        return names.stream().map(name -> name.toLowerCase(Locale.ROOT)).toList();
    }

    private static boolean isBukkitDescriptorMissing(File file) {
        try (var jar = new java.util.jar.JarFile(file)) {
            return jar.getJarEntry("plugin.yml") == null && jar.getJarEntry("paper-plugin.yml") == null && jar.getJarEntry("tube-plugin.yml") != null
                    || jar.getJarEntry("tube-plugin.yml") != null || jar.getJarEntry("tube-plugin.json") != null || jar.getJarEntry("tube-plugin.toml") != null;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (plugin.isEnabled()) {
            return;
        }
        List<Command> pluginCommands = PluginCommandYamlParser.parse(plugin);
        if (!pluginCommands.isEmpty()) {
            commandMap.registerAll(plugin.getName(), pluginCommands);
        }
        loader.enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (!plugin.isEnabled()) {
            return;
        }
        loader.disablePlugin(plugin);
        server.getScheduler().cancelTasks(plugin);
        server.getServicesManager().unregisterAll(plugin);
        HandlerList.unregisterAll(plugin);
        for (var entry : new ArrayList<>(commandMap.getKnownCommands().entrySet())) {
            if (entry.getValue() instanceof PluginCommand command && command.getPlugin() == plugin) {
                command.unregister(commandMap);
                commandMap.getKnownCommands().remove(entry.getKey());
            }
        }
    }

    @Override
    public void disablePlugins() {
        Plugin[] snapshot = getPlugins();
        for (int i = snapshot.length - 1; i >= 0; i--) {
            try {
                disablePlugin(snapshot[i]);
            } catch (Throwable e) {
                server.getLogger().log(Level.SEVERE, "Error occurred (in the plugin loader) while disabling " + snapshot[i].getName(), e);
            }
        }
    }

    @Override
    public synchronized void clearPlugins() {
        disablePlugins();
        plugins.clear();
        lookup.clear();
        HandlerList.unregisterAll();
        commandMap.clearCommands();
    }

    @Override
    public void callEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        for (RegisteredListener registration : handlers.getRegisteredListeners()) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }
            try {
                registration.callEvent(event);
            } catch (AuthorNagException e) {
                Plugin plugin = registration.getPlugin();
                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);
                    server.getLogger().log(Level.SEVERE, String.format("Nag author(s): '%s' of '%s' about the following: %s", plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), e.getMessage()));
                }
            } catch (Throwable e) {
                server.getLogger().log(Level.SEVERE, "Could not pass event " + event.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), e);
            }
        }
    }

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }
        for (var entry : ((org.bukkit.plugin.java.JavaPlugin) plugin).getPluginLoader().createRegisteredListeners(listener, plugin).entrySet()) {
            handlerList(registrationClass(entry.getKey())).registerAll(entry.getValue());
        }
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
    public synchronized void addPermissions(List<Permission> toAdd) {
        toAdd.forEach(this::addPermission);
    }

    @Override
    public synchronized void clearPermissions() {
        permissions.clear();
        defaultPermissions.get(true).clear();
        defaultPermissions.get(false).clear();
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
        calculateDefaults(permission);
    }

    @Override
    public synchronized void removePermission(Permission permission) {
        removePermission(permission.getName());
    }

    @Override
    public synchronized void removePermission(String name) {
        permissions.remove(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public synchronized Set<Permission> getDefaultPermissions(boolean op) {
        return Collections.unmodifiableSet(defaultPermissions.get(op));
    }

    @Override
    public synchronized void recalculatePermissionDefaults(Permission permission) {
        if (permission != null && permissions.containsKey(permission.getName().toLowerCase(Locale.ROOT))) {
            defaultPermissions.get(true).remove(permission);
            defaultPermissions.get(false).remove(permission);
            calculateDefaults(permission);
        }
    }

    private void calculateDefaults(Permission permission) {
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
    public synchronized void subscribeToPermission(String permission, Permissible permissible) {
        permissionSubscriptions.computeIfAbsent(permission.toLowerCase(Locale.ROOT), key -> new WeakHashMap<>()).put(permissible, true);
    }

    @Override
    public synchronized void unsubscribeFromPermission(String permission, Permissible permissible) {
        var subscribers = permissionSubscriptions.get(permission.toLowerCase(Locale.ROOT));
        if (subscribers != null) {
            subscribers.remove(permissible);
        }
    }

    @Override
    public synchronized Set<Permissible> getPermissionSubscriptions(String permission) {
        var subscribers = permissionSubscriptions.get(permission.toLowerCase(Locale.ROOT));
        return subscribers == null ? Set.of() : Set.copyOf(subscribers.keySet());
    }

    @Override
    public synchronized void subscribeToDefaultPerms(boolean op, Permissible permissible) {
        defaultSubscriptions.computeIfAbsent(op, key -> new WeakHashMap<>()).put(permissible, true);
    }

    @Override
    public synchronized void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) {
        var subscribers = defaultSubscriptions.get(op);
        if (subscribers != null) {
            subscribers.remove(permissible);
        }
    }

    @Override
    public synchronized Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        var subscribers = defaultSubscriptions.get(op);
        return subscribers == null ? Set.of() : Set.copyOf(subscribers.keySet());
    }

    @Override
    public synchronized Set<Permission> getPermissions() {
        return Set.copyOf(permissions.values());
    }

    @Override
    public boolean useTimings() {
        return false;
    }

    @Override
    public boolean isTransitiveDependency(PluginMeta plugin, PluginMeta depend) {
        Set<String> seen = new HashSet<>();
        return dependsOn(plugin, depend.getName(), seen);
    }

    private boolean dependsOn(PluginMeta plugin, String target, Set<String> seen) {
        List<String> direct = new ArrayList<>(plugin.getPluginDependencies());
        direct.addAll(plugin.getPluginSoftDependencies());
        for (String name : direct) {
            if (name.equalsIgnoreCase(target)) {
                return true;
            }
            Plugin other = getPlugin(name);
            if (other != null && seen.add(name.toLowerCase(Locale.ROOT)) && dependsOn(other.getPluginMeta(), target, seen)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void overridePermissionManager(Plugin plugin, PermissionManager permissionManager) {
        server.getLogger().warning(plugin.getName() + " tried to replace the permission manager, which Tube does not allow");
    }
}
