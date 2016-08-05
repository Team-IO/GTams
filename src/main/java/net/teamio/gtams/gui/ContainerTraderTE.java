package net.teamio.gtams.gui;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.Offer;
import net.teamio.gtams.client.TradeInfo;
import net.teamio.gtams.content.TraderTE;
import net.teamio.gtams.network.PackageOfferRequest;
import net.teamio.gtams.network.PackageTradeInfoRequest;

public class ContainerTraderTE extends Container {

	public final TraderTE trader;
	public List<Offer> offers;
	public final Slot newTradeSlot;
	TradeInfo tradeInfo;

	public ContainerTraderTE(IInventory playerInventory, TraderTE trader) {
		this.trader = trader;
		IInventory fakeInventory = new InventoryBasic("Trader", true, 1) {
			@Override
			public int getInventoryStackLimit() {
				return 0;
			}

			@Override
			public void markDirty() {
				super.markDirty();
				ContainerTraderTE.this.trader.markDirty();
			}
		};
		newTradeSlot = new Slot(fakeInventory, 0, 10, 10) {
			@Override
			public void putStack(ItemStack stack) {
				super.putStack(stack);
				ItemStack setStack = getStack();
				if(setStack != null) {
					setStack.stackSize = 1;
					requestTradeInfo(setStack);
				}
			}
		};
		addSlotToContainer(newTradeSlot);

		int yOffset = 50;

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

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}

	public void requestOffers() {
		GTams.channel.sendToServer(new PackageOfferRequest());
	}

	public void requestTradeInfo(ItemStack stack) {
		tradeInfo = null;
		GTams.channel.sendToServer(new PackageTradeInfoRequest(stack));
	}

	public void setTradeInfo(TradeInfo info) {
		this.tradeInfo = info;
	}

}
