package net.teamio.gtams.client.entities;

import java.util.UUID;

import net.teamio.gtams.client.Trade;

public class ETerminalCreateTrade {

	public UUID id;
	public Trade trade;

	/**
	 * @param id
	 * @param trade
	 */
	public ETerminalCreateTrade(UUID id, Trade trade) {
		super();
		this.id = id;
		this.trade = trade;
	}

}
