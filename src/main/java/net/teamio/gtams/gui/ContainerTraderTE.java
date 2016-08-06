package net.teamio.gtams.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.Trade;
import net.teamio.gtams.client.TradeDescriptor;
import net.teamio.gtams.client.TradeInfo;
import net.teamio.gtams.content.TraderTE;
import net.teamio.gtams.network.PackageNewTradeRequest;
import net.teamio.gtams.network.PackageTradeInfoRequest;
import net.teamio.gtams.network.PackageTradeRequest;

public class ContainerTraderTE extends Container {

	public final TraderTE trader;
	private List<Trade> trades;
	private ArrayList<ItemStack> tradeStacks;
	private final Slot newTradeSlot;
	TradeInfo tradeInfo;

	public static interface SlotChangeListener {
		public void slotChanged(ItemStack newStack);
	}

	public SlotChangeListener onSlotChange;

	public ContainerTraderTE(IInventory playerInventory, TraderTE trader) {
		this.trader = trader;
		IInventory fakeInventory = new InventoryBasic("Trader", true, 1) {
			@Override
			public int getInventoryStackLimit() {
				return 0;
			}
		};
		newTradeSlot = new Slot(fakeInventory, 0, 10, 31) {
			@Override
			public void putStack(ItemStack stack) {
				// Only notify the listener, do not actually fill the slot
				if(onSlotChange != null) {
					onSlotChange.slotChanged(stack);
				}
			}

			@Override
			public ItemStack getStack() {
				return null;
			}
		};
		addSlotToContainer(newTradeSlot);

		int yOffset = 71;

        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + yOffset));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + yOffset));
        }
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		// TODO Auto-generated method stub
		return true;
	}

	public void setTrades(List<Trade> trades) {
		this.trades = trades;
		tradeStacks = new ArrayList<ItemStack>();
		tradeStacks.ensureCapacity(trades.size());

		for(int i = 0; i < trades.size(); i++) {
			TradeDescriptor td = trades.get(i).descriptor;
			if(td == null) {
				tradeStacks.add(null);
			} else {
				tradeStacks.add(td.toItemStack());
			}
		}
	}

	public List<Trade> getTrades() {
		return trades;
	}

	public List<ItemStack> getTradeStacks() {
		return tradeStacks;
	}

	public void requestTrades() {
		GTams.channel.sendToServer(new PackageTradeRequest());
	}

	public void requestTradeInfo(ItemStack stack) {
		tradeInfo = null;
		GTams.channel.sendToServer(new PackageTradeInfoRequest(stack));
	}

	public void requestCreateTrade(Trade newTrade) {
		GTams.channel.sendToServer(new PackageNewTradeRequest(newTrade));
	}

	public void setTradeInfo(TradeInfo info) {
		this.tradeInfo = info;
	}

}
