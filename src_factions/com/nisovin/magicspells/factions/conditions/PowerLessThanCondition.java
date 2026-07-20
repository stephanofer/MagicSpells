package com.nisovin.magicspells.factions.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.MPlayer;
import com.nisovin.magicspells.castmodifiers.Condition;

public class PowerLessThanCondition extends Condition {

	double power;
	
	@Override
	public boolean setVar(String var) {
		try {
			power = Double.parseDouble(var);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		return player != null && MPlayer.get(player).getPower() < power;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return target != null && target instanceof Player && MPlayer.get(target).getPower() < power;
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
