package space.nows.tube.bukkit;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.Command;
import space.nows.tube.api.CommandRegistry;
import space.nows.tube.api.CommandSender;
import space.nows.tube.api.GameMode;
import space.nows.tube.api.Location;
import space.nows.tube.api.PermissionManager;
import space.nows.tube.api.Player;
import space.nows.tube.api.ScheduledTask;
import space.nows.tube.api.Scheduler;
import space.nows.tube.api.Server;
import space.nows.tube.api.World;
import space.nows.tube.api.WorldManager;
import space.nows.tube.api.event.Event;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.Subscription;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

final class StubServer {

    static final class Events implements EventBus {

        private record Registration(Class<?> type, EventPriority priority, Consumer<Object> listener) {
        }

        private final List<Registration> registrations = new ArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public <E extends Event> Subscription subscribe(Class<E> type, EventPriority priority, boolean ignoreCancelled, Consumer<? super E> listener) {
            Registration registration = new Registration(type, priority, (Consumer<Object>) listener);
            registrations.add(registration);
            return () -> registrations.remove(registration);
        }

        @Override
        public <E extends Event> E post(E event) {
            for (Registration registration : List.copyOf(registrations)) {
                if (registration.type().isInstance(event)) {
                    registration.listener().accept(event);
                }
            }
            return event;
        }
    }

    static final class ManualScheduler implements Scheduler {

        private final List<Runnable> queue = new ArrayList<>();

        @Override
        public ScheduledTask runSync(Runnable task) {
            return runLater(0, task);
        }

        @Override
        public ScheduledTask runLater(long delayTicks, Runnable task) {
            queue.add(task);
            return handle(task);
        }

        @Override
        public ScheduledTask runRepeating(long delayTicks, long periodTicks, Runnable task) {
            queue.add(task);
            return handle(task);
        }

        void tick() {
            List<Runnable> now = List.copyOf(queue);
            queue.clear();
            now.forEach(Runnable::run);
        }

        private ScheduledTask handle(Runnable task) {
            return new ScheduledTask() {
                @Override
                public void cancel() {
                    queue.remove(task);
                }

                @Override
                public boolean isActive() {
                    return queue.contains(task);
                }
            };
        }
    }

    static final class Registry implements CommandRegistry {

        final Map<String, Command> commands = new LinkedHashMap<>();

        @Override
        public void register(String name, Command command, String... aliases) {
            if (commands.putIfAbsent(name, command) != null) {
                throw new IllegalArgumentException("Command already registered: " + name);
            }
        }

        @Override
        public void unregister(String name) {
            commands.remove(name);
        }

        @Override
        public Collection<String> names() {
            return commands.keySet();
        }
    }

    final Registry commands = new Registry();
    final Events events = new Events();
    final ManualScheduler scheduler = new ManualScheduler();
    final List<Player> players = new ArrayList<>();
    final List<String> broadcasts = new ArrayList<>();
    final Set<UUID> ops = new HashSet<>();
    final Server server;

    StubServer() {
        PermissionManager permissions = (PermissionManager) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {PermissionManager.class}, (proxy, method, args) -> switch (method.getName()) {
            case "isOp", "has" -> ops.contains((UUID) args[0]);
            default -> null;
        });
        WorldManager worlds = (WorldManager) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {WorldManager.class}, (proxy, method, args) -> switch (method.getName()) {
            case "all" -> List.<World>of();
            case "get" -> Optional.empty();
            default -> null;
        });
        server = (Server) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Server.class}, (proxy, method, args) -> switch (method.getName()) {
            case "name" -> "Tube";
            case "version" -> "2026.11.1-test";
            case "minecraftVersion" -> "26.2";
            case "maxPlayers" -> 20;
            case "onlineMode" -> false;
            case "motd" -> "A Tube Server";
            case "port" -> 25565;
            case "bindAddress" -> "0.0.0.0";
            case "commands" -> commands;
            case "events" -> events;
            case "scheduler" -> scheduler;
            case "permissions" -> permissions;
            case "worlds" -> worlds;
            case "players" -> List.copyOf(players);
            case "broadcast" -> {
                broadcasts.add(String.valueOf(args[0]));
                yield null;
            }
            default -> null;
        });
    }

    Player player(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(name.getBytes());
        Player player = (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Player.class}, (proxy, method, args) -> switch (method.getName()) {
            case "name" -> name;
            case "uuid" -> uuid;
            case "hasPermission" -> ops.contains(uuid);
            case "gameMode" -> GameMode.SURVIVAL;
            case "health", "maxHealth" -> 20.0;
            case "food" -> 20;
            case "level" -> 0;
            case "address" -> Optional.empty();
            case "isRemoved" -> false;
            case "sendMessage" -> null;
            case "equals" -> proxy == args[0];
            case "hashCode" -> uuid.hashCode();
            case "toString" -> "StubPlayer(" + name + ")";
            default -> null;
        });
        players.add(player);
        return player;
    }

    static CommandSender consoleLike(List<String> sink) {
        return new CommandSender() {
            @Override
            public String name() {
                return "tester";
            }

            @Override
            public boolean hasPermission(String node) {
                return true;
            }

            @Override
            public void sendMessage(Component message) {
                sink.add(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(message));
            }
        };
    }

    @SuppressWarnings("unused")
    private static Location unused() {
        return null;
    }
}
