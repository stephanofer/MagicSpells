package com.nisovin.magicspells.util.compat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collection;
import java.util.function.Supplier;

public class CompatBasics {
	
	public static boolean pluginEnabled(String plugin) {
		return Bukkit.getPluginManager().isPluginEnabled(plugin);
	}
	
	public static Plugin getPlugin(String name) {
		return Bukkit.getPluginManager().getPlugin(name);
	}
	
	public static <T> RegisteredServiceProvider<T> getServiceProvider(Class<T> clazz) {
		return Bukkit.getServer().getServicesManager().getRegistration(clazz);
	}
	
	public static boolean runsWithoutError(Runnable runnable) {
		try {
			runnable.run();
			return true;
		} catch (Throwable throwable) {
			return false;
		}
	}
	
	public static boolean doesClassExist(String name) {
		try {
			return Class.forName(name) != null;
		} catch (Throwable throwable) {
			return false;
		}
	}
	
	// Anticheat integrations were removed; retain the no-op adapter for spell execution.
	public static final ExemptionAssistant activeExemptionAssistant = new DummyExemptionAssistant();
	
	public static <T> T exemptAction(Supplier<T> runnable, Player player, Collection<?> checks) {
		if (activeExemptionAssistant != null && player != null) {
			return activeExemptionAssistant.exemptRunnable(runnable, player, checks);
		} else {
			return runnable.get();
		}
	}
	
}
