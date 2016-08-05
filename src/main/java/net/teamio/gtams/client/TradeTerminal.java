package net.teamio.gtams.client;

import java.util.ArrayList;
import java.util.UUID;

import net.teamio.gtams.gui.Offer;

public class TradeTerminal {

	private UUID id;
	private GTamsClient client;

	ArrayList<Offer> offerCache = new ArrayList<Offer>();

	public TradeTerminal(GTamsClient client, UUID id) {
		this.id = id;
		this.client = client;

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

	public void release() {
		client.releaseTerminal(this);
	}

	public void transferOwner(GTamsClient client2) {
		// TODO Auto-generated method stub

	}

	public ArrayList<Offer> getOffers() {
		return offerCache;
	}

}
