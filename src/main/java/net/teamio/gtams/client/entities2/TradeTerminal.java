package net.teamio.gtams.client.entities2;

import java.util.UUID;

public class TradeTerminal {

	public UUID id;
	public Owner owner;
	/**
	 * Status only used for shutting down terminals that are not fully register
	 * yet, so they will be discarded after registration.
	 */
	public boolean isOnline;

	public TradeTerminal(Owner owner, UUID id) {
		this.id = id;
		this.owner = owner;
		this.isOnline = true;
	}

	public void transferOwner(Owner newOwner) {
		synchronized (owner.client.sync_object) {
			owner.removeTerminal(this);
			this.owner = newOwner;
			newOwner.addTerminal(this);
			owner.client.changeTerminalOwner(this, newOwner);
		}
	}
}
