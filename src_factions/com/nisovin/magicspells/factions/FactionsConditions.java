package com.nisovin.magicspells.factions;

import java.util.HashMap;
import java.util.Map;

import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.factions.conditions.HasFactionCondition;
import com.nisovin.magicspells.factions.conditions.PowerEqualsCondition;
import com.nisovin.magicspells.factions.conditions.PowerGreaterThanCondition;
import com.nisovin.magicspells.factions.conditions.PowerLessThanCondition;

public class FactionsConditions {
	
	public static Map<String, Class<? extends Condition>> conditions;
	private static final String ADDON_KEY = "factions";
	
	private static String makeKey(String basicName) {
		return (ADDON_KEY + ':' + basicName).toLowerCase();
	}
	
	static {
		conditions = new HashMap<>();
		conditions.put(makeKey("powerlessthan"), PowerLessThanCondition.class);
		conditions.put(makeKey("powergreaterthan"), PowerGreaterThanCondition.class);
		conditions.put(makeKey("powerequals"), PowerEqualsCondition.class);
		conditions.put(makeKey("hasfaction"), HasFactionCondition.class);
	}
	
}
