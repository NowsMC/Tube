package space.nows.tube.bukkit;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.PermissionManager;
import space.nows.tube.api.Server;
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.entity.GameMode;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.event.Event;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.Subscription;
import space.nows.tube.api.scheduler.ScheduledTask;
import space.nows.tube.api.scheduler.Scheduler;
import space.nows.tube.api.world.Location;
import space.nows.tube.api.world.World;
import space.nows.tube.api.world.WorldManager;

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
    final List<String> playerMessages = new java.util.concurrent.CopyOnWriteArrayList<>();
    final List<String> createdWorlds = new java.util.concurrent.CopyOnWriteArrayList<>();
    final Events events = new Events();
    final ManualScheduler scheduler = new ManualScheduler();
    final List<Player> players = new ArrayList<>();
    final List<String> broadcasts = new ArrayList<>();
    final Set<UUID> ops = new HashSet<>();
    final java.util.Map<String, space.nows.tube.api.network.PluginChannels.ChannelHandler> channelHandlers = new java.util.HashMap<>();
    final List<String> sentMessages = new ArrayList<>();
    final Server server;

    StubServer() {
        PermissionManager permissions = (PermissionManager) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {PermissionManager.class}, (proxy, method, args) -> switch (method.getName()) {
            case "isOp", "has" -> ops.contains((UUID) args[0]);
            default -> null;
        });
        World world = (World) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {World.class}, (proxy, method, args) -> switch (method.getName()) {
            case "name" -> "world";
            case "folder" -> java.nio.file.Path.of("world");
            case "spawn" -> new space.nows.tube.api.world.Location((World) proxy, 0.5, 64, 0.5);
            case "entities", "loadedChunks" -> List.of();
            case "isChunkLoaded" -> false;
            case "border" -> space.nows.tube.api.world.WorldBorder.DEFAULT;
            case "equals" -> proxy == args[0];
            case "hashCode" -> 1;
            case "toString" -> "StubWorld(world)";
            default -> null;
        });
        WorldManager worlds = (WorldManager) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {WorldManager.class}, (proxy, method, args) -> switch (method.getName()) {
            case "all" -> List.of(world);
            case "get" -> "world".equals(args[0]) ? Optional.of(world) : Optional.empty();
            case "defaultWorld" -> world;
            case "generators" -> java.util.Set.of(space.nows.tube.api.world.WorldOptions.FLAT, space.nows.tube.api.world.WorldOptions.NORMAL);
            case "create" -> {
                createdWorlds.add((String) args[0] + " " + args[1]);
                yield world;
            }
            default -> null;
        });
        server = (Server) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Server.class}, (proxy, method, args) -> switch (method.getName()) {
            case "name" -> "Tube";
            case "version" -> "0.2611.0-test";
            case "minecraftVersion" -> "26.2";
            case "dataVersion" -> 4700;
            case "maxPlayers" -> 20;
            case "onlineMode" -> false;
            case "motd" -> "A Tube Server";
            case "port" -> 25565;
            case "bindAddress" -> "0.0.0.0";
            case "commands" -> commands;
            case "events" -> events;
            case "network" -> Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {space.nows.tube.api.network.Network.class}, (p, m, a) -> {
                if (!m.getName().equals("channels")) {
                    return null;
                }
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {space.nows.tube.api.network.PluginChannels.class}, (p2, m2, a2) -> {
                    if (m2.getName().equals("register")) {
                        channelHandlers.put((String) a2[0], (space.nows.tube.api.network.PluginChannels.ChannelHandler) a2[1]);
                        return (space.nows.tube.api.event.Subscription) () -> channelHandlers.remove((String) a2[0]);
                    }
                    sentMessages.add(a2[1] + ":" + new String((byte[]) a2[2]));
                    return null;
                });
            });
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
        java.util.Set<String> tags = new java.util.LinkedHashSet<>();
        Player player = (Player) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Player.class}, (proxy, method, args) -> switch (method.getName()) {
            case "tags" -> java.util.Set.copyOf(tags);
            case "addTag" -> tags.add((String) args[0]);
            case "removeTag" -> tags.remove((String) args[0]);
            case "loginMode" -> space.nows.tube.api.entity.LoginMode.OFFLINE;
            case "skin", "clientBrand" -> Optional.empty();
            case "latency", "protocolVersion", "heldSlot" -> 0;
            case "name" -> name;
            case "uuid" -> uuid;
            case "hasPermission" -> ops.contains(uuid);
            case "gameMode" -> GameMode.SURVIVAL;
            case "health", "maxHealth" -> 20.0;
            case "food" -> 20;
            case "level" -> 0;
            case "address" -> Optional.of(new java.net.InetSocketAddress("127.0.0.1", 50000));
            case "isRemoved" -> false;
            case "sendMessage" -> {
                playerMessages.add(name + " <- " + (args[0] instanceof Component component ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component) : String.valueOf(args[0])));
                yield null;
            }
            case "showTitle" -> {
                playerMessages.add(name + " title " + net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize((Component) args[0]));
                yield null;
            }
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
