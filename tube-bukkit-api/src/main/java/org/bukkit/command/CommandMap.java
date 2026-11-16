package org.bukkit.command;

import java.util.List;

public interface CommandMap {

    boolean register(String fallbackPrefix, Command command);

    boolean dispatch(CommandSender sender, String commandLine);

    Command getCommand(String name);

    List<String> tabComplete(CommandSender sender, String commandLine);
}
