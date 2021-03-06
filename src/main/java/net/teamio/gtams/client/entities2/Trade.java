package net.teamio.gtams.client.entities2;

public class Trade {

	public TradeDescriptor descriptor;
	public boolean isBuy;
	public int price;
	public int interval;
	public int stopAfter;
	public Mode mode = Mode.Once;
	public int amount;
	public boolean allowPartialFulfillment = true;


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
		return (isBuy ? "Buy " : "Sell ") + amount + (allowPartialFulfillment ? "(P)" : "") + "x" + descriptor + " for " + price + " " + (mode == Mode.Recurring ? " every " + interval + " seconds " + stopAfter + " times." : mode);
	}

	public String toDisplayString() {
		String buyOrSell = isBuy ? "Buy" : "Sell";
		String partial = allowPartialFulfillment ? "(P)" : "";
		String templateA = "%s Amount: %d %s Price: %d %s";
		String templateB = "%s Amount: %d %s Price: %d Every %d seconds.";
		String templateC = "%s Amount: %d %s Price: %d Every %d seconds, %d times.";

		if(mode == Mode.Recurring) {
			if(stopAfter > 0) {
				return String.format(templateB, buyOrSell, amount, partial, price, interval);
			}
			return String.format(templateC, buyOrSell, amount, partial, price, interval, stopAfter);
		}
		return String.format(templateA, buyOrSell, amount, partial, price, mode);
	}
}
