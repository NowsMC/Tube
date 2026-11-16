package org.bukkit.command;

import org.bukkit.plugin.Plugin;

import java.util.List;

public final class PluginCommand extends Command {

    private final Plugin owningPlugin;
    private CommandExecutor executor;
    private TabCompleter completer;

    public PluginCommand(String name, Plugin owner) {
        super(name);
        this.owningPlugin = owner;
        this.executor = owner instanceof CommandExecutor ? (CommandExecutor) owner : null;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!owningPlugin.isEnabled()) {
            throw new CommandException("Cannot execute command '" + commandLabel + "' in plugin " + owningPlugin.getDescription().getFullName() + " - plugin is disabled.");
        }
        if (!testPermission(sender)) {
            return true;
        }
        boolean success;
        try {
            success = executor != null && executor.onCommand(sender, this, commandLabel, args);
        } catch (Throwable e) {
            throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin " + owningPlugin.getDescription().getFullName(), e);
        }
        if (!success && getUsage() != null && !getUsage().isEmpty()) {
            for (String line : getUsage().replace("<command>", commandLabel).split("\n")) {
                sender.sendMessage(line);
            }
        }
        return success;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        TabCompleter source = completer != null ? completer : executor instanceof TabCompleter tab ? tab : null;
        if (source == null) {
            return super.tabComplete(sender, alias, args);
        }
        List<String> result = source.onTabComplete(sender, this, alias, args);
        return result == null ? List.of() : result;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public void setTabCompleter(TabCompleter completer) {
        this.completer = completer;
    }

    public TabCompleter getTabCompleter() {
        return completer;
    }

    public Plugin getPlugin() {
        return owningPlugin;
    }
}
