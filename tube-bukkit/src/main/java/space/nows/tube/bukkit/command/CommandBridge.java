package space.nows.tube.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.bukkit.BukkitRuntime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public final class CommandBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");

    private final BukkitRuntime runtime;
    private final BridgedCommandMap commandMap;
    private final Set<String> exposed = new HashSet<>();

    public CommandBridge(BukkitRuntime runtime) {
        this.runtime = runtime;
        this.commandMap = new BridgedCommandMap(runtime);
    }

    public SimpleCommandMap commandMap() {
        return commandMap;
    }

    public PluginCommand get(String name) {
        return commandMap.getCommand(name) instanceof PluginCommand command ? command : null;
    }

    public void register(Plugin plugin) {
        List<Command> commands = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : plugin.getDescription().getCommands().entrySet()) {
            PluginCommand command = new PluginCommand(entry.getKey(), plugin);
            Map<String, Object> attributes = entry.getValue();
            if (attributes.get("description") != null) {
                command.setDescription(String.valueOf(attributes.get("description")));
            }
            if (attributes.get("usage") != null) {
                command.setUsage(String.valueOf(attributes.get("usage")));
            }
            if (attributes.get("permission") != null) {
                command.setPermission(String.valueOf(attributes.get("permission")));
            }
            if (attributes.get("permission-message") != null) {
                command.setPermissionMessage(String.valueOf(attributes.get("permission-message")));
            }
            List<String> aliases = new ArrayList<>();
            Object declared = attributes.get("aliases");
            if (declared instanceof List<?> list) {
                list.forEach(alias -> aliases.add(String.valueOf(alias)));
            } else if (declared != null) {
                aliases.add(String.valueOf(declared));
            }
            command.setAliases(aliases);
            commands.add(command);
        }
        commandMap.registerAll(plugin.getName(), commands);
    }

    void expose(String label) {
        if (label.indexOf(':') >= 0) {
            return;
        }
        synchronized (exposed) {
            if (exposed.contains(label)) {
                return;
            }
            try {
                runtime.tube().commands().register(label, (sender, args) -> {
                    CommandSender bukkitSender = runtime.adapters().sender(sender);
                    if (!dispatch(bukkitSender, args.isEmpty() ? label : label + " " + String.join(" ", args))) {
                        bukkitSender.sendMessage("Unknown command: " + label);
                    }
                });
                exposed.add(label);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Command /{} clashes with a Tube command and was not exposed", label);
            }
        }
    }

    public boolean dispatch(CommandSender sender, String line) {
        try {
            return commandMap.dispatch(sender, line.strip());
        } catch (CommandException e) {
            sender.sendMessage("An internal error occurred while attempting to perform this command");
            runtime.julLogger().log(Level.SEVERE, e.getMessage(), e.getCause() == null ? e : e.getCause());
            return true;
        }
    }

    public void unregister(Plugin plugin) {
        commandMap.removeIf(command -> command instanceof PluginCommand pluginCommand && pluginCommand.getPlugin() == plugin);
        synchronized (exposed) {
            exposed.removeIf(label -> {
                if (commandMap.getCommand(label) != null) {
                    return false;
                }
                runtime.tube().commands().unregister(label);
                return true;
            });
        }
    }

    public void unregisterAll() {
        synchronized (exposed) {
            exposed.forEach(runtime.tube().commands()::unregister);
            exposed.clear();
        }
        commandMap.clearCommands();
    }

    private final class BridgedCommandMap extends SimpleCommandMap {

        private BridgedCommandMap(BukkitRuntime runtime) {
            super(runtime.server());
        }

        @Override
        public boolean register(String label, String fallbackPrefix, Command command) {
            boolean registered = super.register(label, fallbackPrefix, command);
            knownCommands.forEach((key, value) -> {
                if (value == command) {
                    expose(key);
                }
            });
            if (!registered) {
                LOGGER.warn("Command /{} of {} is already provided by another plugin", label, fallbackPrefix);
            }
            return registered;
        }

        private void removeIf(java.util.function.Predicate<Command> filter) {
            knownCommands.values().removeIf(filter);
        }
    }
}
