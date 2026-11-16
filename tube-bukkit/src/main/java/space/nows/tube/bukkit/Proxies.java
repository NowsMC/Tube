package space.nows.tube.bukkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

final class Proxies {

    @FunctionalInterface
    interface Handler {
        Object call(Object[] args) throws Throwable;
    }

    static final Set<String> MISSING = ConcurrentHashMap.newKeySet();

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");

    private Proxies() {
    }

    static final class Builder<T> {

        private final Class<T> type;
        private final String label;
        private final Map<String, Handler> handlers = new HashMap<>();

        private Builder(Class<T> type, String label) {
            this.type = type;
            this.label = label;
        }

        Builder<T> on(String name, Handler handler) {
            handlers.put(name, handler);
            return this;
        }

        T build() {
            InvocationHandler dispatcher = (proxy, method, args) -> {
                Object[] arguments = args == null ? new Object[0] : args;
                Handler handler = handlers.get(signature(method));
                if (handler == null) {
                    handler = handlers.get(method.getName() + "/" + method.getParameterCount());
                }
                if (handler == null) {
                    handler = handlers.get(method.getName());
                }
                if (handler != null) {
                    return handler.call(arguments);
                }
                return fallback(proxy, method, arguments);
            };
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, dispatcher));
        }

        private Object fallback(Object proxy, Method method, Object[] arguments) throws Throwable {
            switch (method.getName()) {
                case "equals" -> {
                    if (method.getParameterCount() == 1) {
                        return proxy == arguments[0];
                    }
                }
                case "hashCode" -> {
                    if (method.getParameterCount() == 0) {
                        return System.identityHashCode(proxy);
                    }
                }
                case "toString" -> {
                    if (method.getParameterCount() == 0) {
                        return label + "@" + Integer.toHexString(System.identityHashCode(proxy));
                    }
                }
                default -> {
                }
            }
            if (method.isDefault()) {
                return InvocationHandler.invokeDefault(proxy, method, arguments);
            }
            throw missing(label, method);
        }
    }

    static <T> Builder<T> of(Class<T> type, String label) {
        return new Builder<>(type, label);
    }

    static String signature(Method method) {
        return method.getName() + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(",")) + ")";
    }

    static UnsupportedOperationException missing(String label, Method method) {
        String description = label + "#" + signature(method);
        if (MISSING.add(description)) {
            LOGGER.warn("Bukkit API not implemented by Tube yet: {}", description);
        }
        return new UnsupportedOperationException("Tube does not implement " + description + " yet");
    }
}
