package net.teamio.gtams.client;

import java.util.ArrayList;

public class TradeList {

	public TradeList() {
		trades = new ArrayList<Trade>();
	}

	public TradeList(ArrayList<Trade> trades) {
		this.trades = trades;
	}

	public ArrayList<Trade> trades;
}
