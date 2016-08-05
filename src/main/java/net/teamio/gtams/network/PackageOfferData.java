package net.teamio.gtams.network;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.teamio.gtams.client.Offer;
import net.teamio.gtams.gui.ContainerTraderTE;

public class PackageOfferData implements IMessage {

	public static final class HandlerServer implements IMessageHandler<PackageOfferRequest, PackageOfferData> {

		@Override
		public PackageOfferData onMessage(PackageOfferRequest message, MessageContext ctx) {
			Container container = ctx.getServerHandler().playerEntity.openContainer;
			PackageOfferData response;
			if(container instanceof ContainerTraderTE) {
				ContainerTraderTE ctte = (ContainerTraderTE)container;
				response = new PackageOfferData(ctte.trader.getTerminal().getOffers());
			} else {
				response = new PackageOfferData();
			}
			return response;
		}

	}

	public static final class HandlerClient implements IMessageHandler<PackageOfferData, IMessage> {

		@Override
		public IMessage onMessage(PackageOfferData message, MessageContext ctx) {
			Container container = Minecraft.getMinecraft().thePlayer.openContainer;
			if(container instanceof ContainerTraderTE) {

				((ContainerTraderTE)container).setOffers(message.offers);
			} else {
				//TODO: Log
				System.out.println("Wrong container open");
			}
			return null;
		}

	}

	public final ArrayList<Offer> offers;

	public PackageOfferData() {
		offers = new ArrayList<Offer>();
	}

	public PackageOfferData(ArrayList<Offer> offers) {
		this.offers = offers;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);

		try {
			offers.clear();
			int length = packetBuffer.readInt();
			offers.ensureCapacity(length);
			for (int i = 0; i < length; i++) {
				String name = packetBuffer.readStringFromBuffer(255);
				int damage = packetBuffer.readInt();
				String nbtHash = packetBuffer.readStringFromBuffer(255);
				offers.add(new Offer(name, damage, nbtHash));
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		packetBuffer.release();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);

		try {
			packetBuffer.writeInt(offers.size());
			for(Offer off : offers) {
				packetBuffer.writeString(off.itemName);
				packetBuffer.writeInt(off.damage);
				packetBuffer.writeString(off.nbtHash);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
