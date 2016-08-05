package net.teamio.gtams.gui;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.Offer;
import net.teamio.gtams.content.TraderTE;
import net.teamio.gtams.network.PackageOfferRequest;

public class ContainerTraderTE extends Container {

	public final TraderTE trader;
	public List<Offer> offers;

	public ContainerTraderTE(TraderTE trader) {
		this.trader = trader;
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

}
