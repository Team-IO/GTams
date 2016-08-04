package net.teamio.gtams.client;

import java.util.UUID;

public class TradeTerminal {

	private UUID id;
	private GTamsClient client;

	public TradeTerminal(GTamsClient client, UUID id) {
		this.id = id;
		this.client = client;
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

}
