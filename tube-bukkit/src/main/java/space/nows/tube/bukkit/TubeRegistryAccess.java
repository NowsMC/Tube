package space.nows.tube.bukkit;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class TubeRegistryAccess implements RegistryAccess {

    private static final Map<RegistryKey<?>, Class<?>> TYPES = discoverTypes();

    private final Map<Class<?>, Registry<?>> registries = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Keyed> Registry<T> getRegistry(Class<T> type) {
        return (Registry<T>) registries.computeIfAbsent(type, key -> create((Class) key));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends Keyed> Registry<T> getRegistry(RegistryKey<T> key) {
        Class<?> type = TYPES.get(key);
        if (type == null) {
            throw new IllegalArgumentException("Unknown registry " + key.key());
        }
        return getRegistry((Class) type);
    }

    private static Map<RegistryKey<?>, Class<?>> discoverTypes() {
        Map<RegistryKey<?>, Class<?>> types = new ConcurrentHashMap<>();
        for (Field field : RegistryKey.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != RegistryKey.class) {
                continue;
            }
            try {
                Type argument = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                Class<?> raw = argument instanceof Class<?> type ? type : (Class<?>) ((ParameterizedType) argument).getRawType();
                types.put((RegistryKey<?>) field.get(null), raw);
            } catch (ReflectiveOperationException | ClassCastException e) {
                // a registry without a usable value type is simply not available
            }
        }
        return types;
    }

    private static <T extends Keyed> Registry<T> create(Class<T> type) {
        Map<NamespacedKey, T> entries = new ConcurrentHashMap<>();
        return cast(Proxies.of(Registry.class, "Registry<" + type.getSimpleName() + ">")
                .on("get(NamespacedKey)", args -> element(type, entries, (NamespacedKey) args[0]))
                .on("getKey", args -> ((Keyed) args[0]).getKey())
                .on("stream", args -> all(type, entries))
                .on("keyStream", args -> all(type, entries).map(Keyed::getKey))
                .on("size", args -> (int) all(type, entries).count())
                .on("iterator", args -> all(type, entries).iterator())
                .on("hasTag", args -> false)
                .on("getTags", args -> List.of())
                .build());
    }

    private static <T extends Keyed> Stream<T> all(Class<T> type, Map<NamespacedKey, T> entries) {
        return Stream.concat(constants(type).stream().filter(TubeRegistryAccess::hasKey), entries.values().stream()).distinct();
    }

    private static boolean hasKey(Keyed constant) {
        try {
            return constant.getKey() != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Keyed> Registry<T> cast(Registry registry) {
        return (Registry<T>) registry;
    }

    private static <T> List<T> constants(Class<T> type) {
        T[] constants = type.getEnumConstants();
        return constants == null ? List.of() : List.of(constants);
    }

    private static <T extends Keyed> T element(Class<T> type, Map<NamespacedKey, T> entries, NamespacedKey key) {
        if (type.isEnum()) {
            return constants(type).stream().filter(TubeRegistryAccess::hasKey).filter(constant -> constant.getKey().equals(key)).findFirst().orElse(null);
        }
        if (!type.isInterface()) {
            return null;
        }
        return entries.computeIfAbsent(key, ignored -> Proxies.of(type, type.getSimpleName() + "{" + key + "}")
                .on("getKey", args -> key)
                .on("key", args -> (Key) key)
                .on("getKeyOrThrow", args -> key)
                .on("equals", args -> args[0] instanceof Keyed other && other.getKey().equals(key))
                .on("hashCode", args -> key.hashCode())
                .on("toString", args -> type.getSimpleName() + "{" + key + "}")
                .build());
    }
}
