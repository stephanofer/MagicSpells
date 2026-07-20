package com.nisovin.magicspells.memory;

import com.nisovin.magicspells.Spell;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellLearnEvent;

public class MemorySpellListener implements Listener {

	private MagicSpellsMemory plugin;
	
	public MemorySpellListener(MagicSpellsMemory plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onSpellLearn(SpellLearnEvent event) {
		Spell spell = event.getSpell();
		int req = this.plugin.getRequiredMemory(spell);
		if (req > 0) {
			Player learner = event.getLearner();
			int mem = this.plugin.getMemoryRemaining(learner);
			MagicSpells.debug("Memory check: " + req + " required, " + mem + " remaining");
			if (mem < req) {
				event.setCancelled(true);
				MagicSpells.sendMessage(MagicSpells.formatMessage(this.plugin.strOutOfMemory, "%spell", spell.getName()), learner, (String[])null);
			}
		}
	}

}
