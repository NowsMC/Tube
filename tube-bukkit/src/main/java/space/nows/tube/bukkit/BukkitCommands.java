package space.nows.tube.bukkit;

import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

final class BukkitCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube-bukkit");

    private final BukkitRuntime runtime;
    private final Map<String, PluginCommand> commands = new ConcurrentHashMap<>();
    private final List<String> exposed = new ArrayList<>();

    BukkitCommands(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    PluginCommand get(String name) {
        return commands.get(name.toLowerCase(Locale.ROOT));
    }

    void register(Plugin plugin) {
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

            List<String> labels = new ArrayList<>();
            labels.add(command.getName());
            labels.addAll(aliases);
            for (String label : labels) {
                String key = label.toLowerCase(Locale.ROOT);
                commands.put(plugin.getName().toLowerCase(Locale.ROOT) + ":" + key, command);
                if (commands.putIfAbsent(key, command) == null) {
                    expose(plugin, key);
                } else if (commands.get(key).getPlugin() != plugin) {
                    LOGGER.warn("Command /{} of {} is already provided by {}", key, plugin.getName(), commands.get(key).getPlugin().getName());
                }
            }
            command.register(null);
        }
    }

    private void expose(Plugin plugin, String label) {
        try {
            runtime.tube.commands().register(label, (sender, args) -> {
                CommandSender bukkitSender = runtime.wrappers.sender(sender);
                if (!dispatch(bukkitSender, args.isEmpty() ? label : label + " " + String.join(" ", args))) {
                    bukkitSender.sendMessage("Unknown command: " + label);
                }
            });
            synchronized (exposed) {
                exposed.add(label);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Command /{} of {} clashes with a Tube command and was not exposed", label, plugin.getName());
        }
    }

    boolean dispatch(CommandSender sender, String line) {
        String trimmed = line.strip();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        String[] parts = trimmed.split("\\s+");
        PluginCommand command = parts[0].isEmpty() ? null : get(parts[0]);
        if (command == null) {
            return false;
        }
        String[] arguments = java.util.Arrays.copyOfRange(parts, 1, parts.length);
        try {
            command.execute(sender, parts[0], arguments);
        } catch (CommandException e) {
            sender.sendMessage("An internal error occurred while attempting to perform this command");
            runtime.julLogger.log(Level.SEVERE, e.getMessage(), e.getCause() == null ? e : e.getCause());
        }
        return true;
    }

    void unregister(Plugin plugin) {
        List<String> removed = new ArrayList<>();
        commands.entrySet().removeIf(entry -> {
            boolean ours = entry.getValue().getPlugin() == plugin;
            if (ours) {
                removed.add(entry.getKey());
            }
            return ours;
        });
        synchronized (exposed) {
            for (String label : removed) {
                if (exposed.remove(label)) {
                    runtime.tube.commands().unregister(label);
                }
            }
        }
    }

    void unregisterAll() {
        synchronized (exposed) {
            exposed.forEach(runtime.tube.commands()::unregister);
            exposed.clear();
        }
        commands.clear();
    }
}
