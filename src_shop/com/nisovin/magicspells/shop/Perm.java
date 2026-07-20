package com.nisovin.magicspells.shop;

import org.bukkit.permissions.Permissible;

public enum Perm {
	
	CREATESIGNSHOP("magicspells.createsignshop")
	
	;
	
	private final String node;
	public String getNode() { return this.node; }
	
	Perm(String node) {
		this.node = node;
	}
	
	public boolean has(Permissible permissible) {
		return permissible.hasPermission(this.node);
	}
	
}
