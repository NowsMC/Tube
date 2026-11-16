package org.bukkit.help;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class GenericCommandHelpTopic extends HelpTopic {

    protected Command command;

    public GenericCommandHelpTopic(Command command) {
        this.command = command;
        this.name = command.getLabel().startsWith("/") ? command.getLabel() : "/" + command.getLabel();
        String description = command.getDescription() == null ? "" : command.getDescription();
        int newline = description.indexOf('\n');
        this.shortText = newline > 1 ? description.substring(0, newline - 1) : description;
        this.fullText = description + (command.getUsage() == null ? "" : "\nUsage: " + command.getUsage());
    }

    @Override
    public boolean canSee(CommandSender sender) {
        if (amendedPermission != null) {
            return sender.hasPermission(amendedPermission);
        }
        return command.testPermissionSilent(sender);
    }
}
