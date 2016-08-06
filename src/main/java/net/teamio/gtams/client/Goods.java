package net.teamio.gtams.client;

public class Goods {
	public TradeDescriptor what;
	public int locked;
	public int unlocked;

	@Override
	public String toString() {
		return "[l" + locked + " u" + unlocked + "]x" + what;
	}
}
