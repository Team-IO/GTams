package net.teamio.gtams.client;

import java.util.ArrayList;
import java.util.UUID;

public class TradeTerminal {

	public UUID id;
	private Owner owner;
	/**
	 * Status only used for shutting down terminals that are not fully register
	 * yet, so they will be discarded after registration.
	 */
	public boolean isOnline;

	ArrayList<Offer> offerCache = new ArrayList<Offer>();

	public TradeTerminal(Owner owner, UUID id) {
		this.id = id;
		this.owner = owner;
		this.isOnline = true;

		/*
		 * Debug
		 */
		offerCache.add(new Offer());
		offerCache.add(new Offer());
		offerCache.add(new Offer());
		offerCache.add(new Offer());
	}

	public UUID getId() {
		return id;
	}

	public void transferOwner(Owner newOwner) {
		synchronized (owner.client.sync_object) {
			owner.removeTerminal(this);
			this.owner = newOwner;
			newOwner.addTerminal(this);
			owner.client.changeTerminalOwner(this, newOwner);
		}
	}

	public ArrayList<Offer> getOffers() {
		return offerCache;
	}

}
