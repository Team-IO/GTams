package net.teamio.gtams.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.entities2.Goods;
import net.teamio.gtams.client.entities2.Player;
import net.teamio.gtams.client.entities2.Trade;
import net.teamio.gtams.client.entities2.TradeDescriptor;
import net.teamio.gtams.client.entities2.TradeInfo;
import net.teamio.gtams.content.TraderTE;
import net.teamio.gtams.network.PackageNewTradeRequest;
import net.teamio.gtams.network.PackageTerminalDataRequest;
import net.teamio.gtams.network.PackageTradeInfoRequest;

public class ContainerTraderTE extends Container {

	public final TraderTE trader;
	private List<Trade> trades;
	private ArrayList<Goods> goods;
	private ArrayList<ItemStack> tradeStacks;
	private ArrayList<ItemStack> goodsStacks;
	private final Slot newTradeSlot;
	TradeInfo tradeInfo;

	public static interface SlotChangeListener {
		public void slotChanged(ItemStack newStack);
	}

	public SlotChangeListener onSlotChange;
	public Player playerInfo;

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

		for(int r = 0; r < 3; r++) {
			for(int c = 0; c < 4; c++) {
				SlotItemHandler sih = new SlotItemHandler(trader.itemHandler, r * 4 + c, 175 + c * 18, 196 + r * 18);
				this.addSlotToContainer(sih);
			}
		}

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
		tradeStacks = new ArrayList<>();
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

	@Override
	protected void retrySlotClick(int slotId, int clickedButton, boolean mode, EntityPlayer playerIn) {
		//FIXME implement this!
	}

	public List<Trade> getTrades() {
		return trades;
	}

	public ArrayList<Goods> getGoods() {
		return goods;
	}

	public ArrayList<ItemStack> getGoodsStacks() {
		return goodsStacks;
	}

	public List<ItemStack> getTradeStacks() {
		return tradeStacks;
	}

	public void requestTrades() {
		GTams.channel.sendToServer(new PackageTerminalDataRequest());
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

	public void setGoods(ArrayList<Goods> goods) {
		this.goods = goods;
		goodsStacks = new ArrayList<>();
		goodsStacks.ensureCapacity(trades.size());

		for(int i = 0; i < goods.size(); i++) {
			Goods g = goods.get(i);
			TradeDescriptor td = g.what;
			if(td == null) {
				goodsStacks.add(null);
			} else {
				ItemStack stack = td.toItemStack();
				stack.stackSize = g.amount;
				goodsStacks.add(stack);
			}
		}
	}

	public void setPlayerInfo(Player player) {
		this.playerInfo = player;
	}

}
