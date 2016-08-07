package net.teamio.gtams.client;

public class Trade {

	public TradeDescriptor descriptor;
	public boolean isBuy;
	public int price;
	public int interval;
	public int stopAfter;
	public Mode mode = Mode.Once;
	public int amount;

	/**
	 * @param itemName
	 * @param damage
	 * @param nbtHash
	 */
	public Trade(TradeDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public Trade() {

	}

	@Override
	public String toString() {
		return (isBuy ? "Buy " : "Sell ") + amount + "x" + descriptor + " for " + price + " " + (mode == Mode.Recurring ? " every " + interval + " seconds " + stopAfter + " times." : mode);
	}

	public String toDisplayString() {
		String buyOrSell = isBuy ? "Buy" : "Sell";
		String templateA = "%s Amount: %d Price: %d %s";
		String templateB = "%s Amount: %d Price: %d Every %d seconds.";
		String templateC = "%s Amount: %d Price: %d Every %d seconds, %d times.";

		if(mode == Mode.Recurring) {
			if(stopAfter > 0) {
				return String.format(templateB, buyOrSell, amount, price, interval);
			}
			return String.format(templateC, buyOrSell, amount, price, interval, stopAfter);
		}
		return String.format(templateA, buyOrSell, amount, price, mode);
	}
}
