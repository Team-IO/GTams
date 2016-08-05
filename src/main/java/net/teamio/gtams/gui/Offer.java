package net.teamio.gtams.gui;

public class Offer {

	public Offer() {
		// TODO Auto-generated constructor stub
		/*
		 * Debug
		 */
		itemName = "minecraft:stick";
		damage = 0;
		nbtHash = "";
	}

	/**
	 * @param itemName
	 * @param damage
	 * @param nbtHash
	 */
	public Offer(String itemName, int damage, String nbtHash) {
		super();
		this.itemName = itemName;
		this.damage = damage;
		this.nbtHash = nbtHash;
	}



	public String itemName;
	public int damage;
	public String nbtHash;
}
