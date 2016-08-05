package net.teamio.gtams.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class GTamsClient {

	protected final Map<UUID, Owner> owners;
	public final Object sync_object = new Object();

	public GTamsClient() {
		owners = new HashMap<UUID, Owner>();
	}

	public Owner getOwner(UUID id) {
		synchronized(sync_object) {
			Owner owner = owners.get(id);
			if (owner == null) {
				owner = new Owner(this, id);
				owners.put(id, owner);
				notifyClientOnline(owner);
			}
			return owner;
		}
	}

	public void logoffOwner(Owner owner) {
		synchronized(sync_object) {
			owners.remove(owner.persistentID);
			notifyClientOffline(owner);
		}
	}

	public abstract void notifyTerminalOnline(TradeTerminal terminal);

	public abstract void notifyTerminalOffline(TradeTerminal terminal);

	public abstract void registerNewTerminal(TradeTerminal terminal);

	/**
	 * Removes a terminal from the list, deleting all related trades.
	 * @param terminal
	 */
	public abstract void destroyTerminal(TradeTerminal terminal);

	public abstract void notifyClientOffline(Owner owner);

	public abstract void notifyClientOnline(Owner owner);

	public abstract void changeTerminalOwner(TradeTerminal tradeTerminal, Owner newOwner);

}