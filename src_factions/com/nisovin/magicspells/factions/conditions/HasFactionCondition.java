package com.nisovin.magicspells.factions.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.MPlayer;
import com.nisovin.magicspells.castmodifiers.Condition;

public class HasFactionCondition extends Condition {

	@Override
	public boolean setVar(String var) {
		return true;
	}

	@Override
	public boolean check(Player player) {
		return player != null && MPlayer.get(player).hasFaction();
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		return target != null && target instanceof Player && MPlayer.get(target).hasFaction();
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
