package org.bukkit.command;

import org.bukkit.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCommandMap implements CommandMap {

    protected final Map<String, Command> knownCommands = new ConcurrentHashMap<>();
    private final Server server;

    public SimpleCommandMap(Server server) {
        this.server = server;
    }

    public void registerAll(String fallbackPrefix, List<Command> commands) {
        if (commands != null) {
            for (Command command : commands) {
                register(fallbackPrefix, command);
            }
        }
    }

    @Override
    public boolean register(String fallbackPrefix, Command command) {
        return register(command.getName(), fallbackPrefix, command);
    }

    public boolean register(String label, String fallbackPrefix, Command command) {
        String key = label.toLowerCase(Locale.ROOT).strip();
        String prefix = fallbackPrefix.toLowerCase(Locale.ROOT).strip();
        boolean registered = register(key, command, false, prefix);
        for (String alias : command.getAliases()) {
            register(alias.toLowerCase(Locale.ROOT).strip(), command, true, prefix);
        }
        if (!registered) {
            command.setLabel(prefix + ":" + key);
        }
        command.register(this);
        return registered;
    }

    private synchronized boolean register(String label, Command command, boolean alias, String prefix) {
        knownCommands.put(prefix + ":" + label, command);
        Command existing = knownCommands.get(label);
        if (existing != null && existing != command && (alias || existing.getLabel().equals(label))) {
            return false;
        }
        if (!alias) {
            command.setLabel(label);
        }
        knownCommands.put(label, command);
        return true;
    }

    @Override
    public boolean dispatch(CommandSender sender, String commandLine) {
        String line = commandLine.startsWith("/") ? commandLine.substring(1) : commandLine;
        String[] parts = line.strip().split("\s+");
        if (parts.length == 0 || parts[0].isEmpty()) {
            return false;
        }
        Command target = getCommand(parts[0]);
        if (target == null) {
            return false;
        }
        try {
            target.execute(sender, parts[0], Arrays.copyOfRange(parts, 1, parts.length));
        } catch (CommandException e) {
            throw e;
        } catch (Throwable e) {
            throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + target, e);
        }
        return true;
    }

    public synchronized void clearCommands() {
        knownCommands.values().forEach(command -> command.unregister(this));
        knownCommands.clear();
    }

    @Override
    public Command getCommand(String name) {
        return knownCommands.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String commandLine) {
        int space = commandLine.indexOf(' ');
        if (space < 0) {
            List<String> labels = new ArrayList<>();
            String prefix = commandLine.toLowerCase(Locale.ROOT);
            for (String label : knownCommands.keySet()) {
                if (label.startsWith(prefix)) {
                    labels.add("/" + label);
                }
            }
            return labels;
        }
        Command target = getCommand(commandLine.substring(0, space));
        if (target == null) {
            return null;
        }
        String[] args = commandLine.substring(space + 1).split(" ", -1);
        return target.tabComplete(sender, commandLine.substring(0, space), args);
    }

    protected Server getServer() {
        return server;
    }
}
