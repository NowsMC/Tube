package org.bukkit.command;

public interface BlockCommandSender extends org.bukkit.command.CommandSender {

    org.bukkit.block.Block getBlock();
}
