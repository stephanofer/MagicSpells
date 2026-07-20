package com.nisovin.magicspells.teams;

import org.bukkit.command.CommandSender;

@FunctionalInterface
public interface TeamsSubCommand {

	boolean process(CommandSender sender, String[] args);
	
}
