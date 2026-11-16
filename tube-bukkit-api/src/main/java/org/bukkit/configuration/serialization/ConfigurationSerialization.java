package org.bukkit.configuration.serialization;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationSerialization {

    public static final String SERIALIZED_TYPE_KEY = "==";

    private static final Map<String, Class<? extends ConfigurationSerializable>> ALIASES = new ConcurrentHashMap<>();

    private final Class<? extends ConfigurationSerializable> type;

    protected ConfigurationSerialization(Class<? extends ConfigurationSerializable> type) {
        this.type = type;
    }

    public static ConfigurationSerializable deserializeObject(Map<String, ?> args, Class<? extends ConfigurationSerializable> type) {
        return new ConfigurationSerialization(type).deserialize(args);
    }

    public static ConfigurationSerializable deserializeObject(Map<String, ?> args) {
        Object alias = args.get(SERIALIZED_TYPE_KEY);
        if (alias == null) {
            throw new IllegalArgumentException("Args doesn't contain type key ('" + SERIALIZED_TYPE_KEY + "')");
        }
        Class<? extends ConfigurationSerializable> type = getClassByAlias(alias.toString());
        if (type == null) {
            throw new IllegalArgumentException("Specified class does not exist ('" + alias + "')");
        }
        return deserializeObject(args, type);
    }

    public static void registerClass(Class<? extends ConfigurationSerializable> type) {
        DelegateDeserialization delegate = type.getAnnotation(DelegateDeserialization.class);
        if (delegate == null) {
            registerClass(type, getAlias(type));
            registerClass(type, type.getName());
        }
    }

    public static void registerClass(Class<? extends ConfigurationSerializable> type, String alias) {
        ALIASES.put(alias, type);
    }

    public static void unregisterClass(String alias) {
        ALIASES.remove(alias);
    }

    public static void unregisterClass(Class<? extends ConfigurationSerializable> type) {
        ALIASES.values().removeIf(type::equals);
    }

    public static Class<? extends ConfigurationSerializable> getClassByAlias(String alias) {
        Class<? extends ConfigurationSerializable> registered = ALIASES.get(alias);
        if (registered != null) {
            return registered;
        }
        try {
            Class<?> found = Class.forName(alias);
            return ConfigurationSerializable.class.isAssignableFrom(found) ? found.asSubclass(ConfigurationSerializable.class) : null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static String getAlias(Class<? extends ConfigurationSerializable> type) {
        DelegateDeserialization delegate = type.getAnnotation(DelegateDeserialization.class);
        if (delegate != null && delegate.value() != type) {
            return getAlias(delegate.value());
        }
        SerializableAs alias = type.getAnnotation(SerializableAs.class);
        return alias != null ? alias.value() : type.getName();
    }

    protected ConfigurationSerializable deserialize(Map<String, ?> args) {
        try {
            Method factory = staticFactory("deserialize");
            if (factory == null) {
                factory = staticFactory("valueOf");
            }
            if (factory != null) {
                Object result = factory.invoke(null, args);
                if (result != null) {
                    return (ConfigurationSerializable) result;
                }
            }
            Constructor<? extends ConfigurationSerializable> constructor = type.getConstructor(Map.class);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException e) {
            Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                    "Could not deserialize " + type.getName() + ": no deserialize(Map), valueOf(Map) or constructor(Map)", e);
        } catch (InvocationTargetException e) {
            Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                    "Could not call deserialization of " + type.getName(), e.getCause());
        } catch (ReflectiveOperationException e) {
            Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE,
                    "Could not deserialize " + type.getName(), e);
        }
        return null;
    }

    private Method staticFactory(String name) {
        try {
            Method method = type.getDeclaredMethod(name, Map.class);
            if (!ConfigurationSerializable.class.isAssignableFrom(method.getReturnType()) || !Modifier.isStatic(method.getModifiers())) {
                return null;
            }
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
