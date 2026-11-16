package org.bukkit.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Command {

    private String name;
    private String label;
    private List<String> aliases;
    private String description;
    private String usageMessage;
    private String permission;
    private String permissionMessage;
    private boolean registered;

    protected Command(String name) {
        this(name, "", "/" + name, new ArrayList<>());
    }

    protected Command(String name, String description, String usageMessage, List<String> aliases) {
        this.name = name;
        this.label = name;
        this.description = description == null ? "" : description;
        this.usageMessage = usageMessage;
        this.aliases = aliases;
    }

    public abstract boolean execute(CommandSender sender, String commandLabel, String[] args);

    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    public String getName() {
        return name;
    }

    public boolean setName(String name) {
        if (registered) {
            return false;
        }
        this.name = name;
        return true;
    }

    public String getLabel() {
        return label;
    }

    public boolean setLabel(String name) {
        if (registered) {
            return false;
        }
        this.label = name;
        return true;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public boolean testPermission(CommandSender target) {
        if (testPermissionSilent(target)) {
            return true;
        }
        String message = permissionMessage == null
                ? "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."
                : permissionMessage;
        if (!message.isEmpty()) {
            for (String line : message.replace("<permission>", permission).split("\n")) {
                target.sendMessage(line);
            }
        }
        return false;
    }

    public boolean testPermissionSilent(CommandSender target) {
        return permission == null || permission.isEmpty() || Arrays.stream(permission.split(";")).anyMatch(target::hasPermission);
    }

    public List<String> getAliases() {
        return aliases;
    }

    public Command setAliases(List<String> aliases) {
        this.aliases = new ArrayList<>(aliases);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Command setDescription(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public String getUsage() {
        return usageMessage;
    }

    public Command setUsage(String usage) {
        this.usageMessage = usage;
        return this;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public Command setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    public boolean register(CommandMap commandMap) {
        registered = true;
        return true;
    }

    public boolean unregister(CommandMap commandMap) {
        registered = false;
        return true;
    }

    public boolean isRegistered() {
        return registered;
    }

    @Override
    public String toString() {
        return getClass().getName() + '(' + name + ')';
    }
}
