package space.nows.tube.bukkit.plugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BukkitServicesManager implements ServicesManager {

    private final Map<Class<?>, List<RegisteredServiceProvider<?>>> providers = new HashMap<>();

    @Override
    public synchronized <T> void register(Class<T> service, T provider, Plugin plugin, ServicePriority priority) {
        List<RegisteredServiceProvider<?>> list = providers.computeIfAbsent(service, key -> new ArrayList<>());
        list.add(new RegisteredServiceProvider<>(service, provider, priority, plugin));
        Collections.sort(list, (a, b) -> ((RegisteredServiceProvider<?>) a).compareTo(b));
    }

    @Override
    public synchronized void unregisterAll(Plugin plugin) {
        providers.values().forEach(list -> list.removeIf(registration -> registration.getPlugin().equals(plugin)));
    }

    @Override
    public synchronized void unregister(Class<?> service, Object provider) {
        List<RegisteredServiceProvider<?>> list = providers.get(service);
        if (list != null) {
            list.removeIf(registration -> registration.getProvider() == provider);
        }
    }

    @Override
    public synchronized void unregister(Object provider) {
        providers.values().forEach(list -> list.removeIf(registration -> registration.getProvider() == provider));
    }

    @Override
    public synchronized <T> T load(Class<T> service) {
        RegisteredServiceProvider<T> registration = getRegistration(service);
        return registration == null ? null : registration.getProvider();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> RegisteredServiceProvider<T> getRegistration(Class<T> service) {
        List<RegisteredServiceProvider<?>> list = providers.get(service);
        return list == null || list.isEmpty() ? null : (RegisteredServiceProvider<T>) list.getFirst();
    }

    @Override
    public synchronized List<RegisteredServiceProvider<?>> getRegistrations(Plugin plugin) {
        List<RegisteredServiceProvider<?>> result = new ArrayList<>();
        providers.values().forEach(list -> list.stream().filter(registration -> registration.getPlugin().equals(plugin)).forEach(result::add));
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> Collection<RegisteredServiceProvider<T>> getRegistrations(Class<T> service) {
        List<RegisteredServiceProvider<?>> list = providers.get(service);
        List<RegisteredServiceProvider<T>> result = new ArrayList<>();
        if (list != null) {
            list.forEach(registration -> result.add((RegisteredServiceProvider<T>) registration));
        }
        return result;
    }

    @Override
    public synchronized Collection<Class<?>> getKnownServices() {
        return List.copyOf(providers.keySet());
    }

    @Override
    public synchronized <T> boolean isProvidedFor(Class<T> service) {
        return getRegistration(service) != null;
    }
}
