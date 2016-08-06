package net.teamio.gtams.network;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.Mode;
import net.teamio.gtams.client.Trade;
import net.teamio.gtams.client.TradeDescriptor;
import net.teamio.gtams.client.TradeList;
import net.teamio.gtams.gui.ContainerTraderTE;

public class PackageTradeData implements IMessage {

	public static final class HandlerServer implements IMessageHandler<PackageTradeRequest, PackageTradeData> {

		@Override
		public PackageTradeData onMessage(PackageTradeRequest message, MessageContext ctx) {
			Container container = ctx.getServerHandler().playerEntity.openContainer;
			PackageTradeData response;
			if(container instanceof ContainerTraderTE) {
				ContainerTraderTE ctte = (ContainerTraderTE)container;
				TradeList tl = GTams.gtamsClient.getTrades(ctte.trader.getTerminal());
				response = new PackageTradeData(tl.trades);
			} else {
				response = new PackageTradeData();
			}
			return response;
		}

	}

	public static final class HandlerClient implements IMessageHandler<PackageTradeData, IMessage> {

		@Override
		public IMessage onMessage(PackageTradeData message, MessageContext ctx) {
			Container container = Minecraft.getMinecraft().thePlayer.openContainer;
			if(container instanceof ContainerTraderTE) {

				((ContainerTraderTE)container).setTrades(message.offers);
			} else {
				//TODO: Log
				System.out.println("Wrong container open");
			}
			return null;
		}

	}

	public final ArrayList<Trade> offers;

	public PackageTradeData() {
		offers = new ArrayList<Trade>();
	}

	public PackageTradeData(ArrayList<Trade> offers) {
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
				Trade trade = new Trade();

				boolean hasTradeDescriptor = packetBuffer.readBoolean();
				if(hasTradeDescriptor) {
					trade.descriptor = new TradeDescriptor();
					trade.descriptor.itemName = packetBuffer.readStringFromBuffer(255);
					trade.descriptor.damage = packetBuffer.readInt();
					trade.descriptor.nbtHash = packetBuffer.readStringFromBuffer(255);
				}
				trade.isBuy = packetBuffer.readBoolean();
				trade.price = packetBuffer.readInt();
				trade.interval = packetBuffer.readInt();
				trade.stopAfter = packetBuffer.readInt();
				trade.mode = packetBuffer.readEnumValue(Mode.class);

				offers.add(trade);
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
			for(Trade off : offers) {
				boolean hasTradeDescriptor = off.descriptor != null;

				packetBuffer.writeBoolean(hasTradeDescriptor);
				if(hasTradeDescriptor) {
					packetBuffer.writeString(off.descriptor.itemName);
					packetBuffer.writeInt(off.descriptor.damage);
					packetBuffer.writeString(off.descriptor.nbtHash);
				}
				packetBuffer.writeBoolean(off.isBuy);
				packetBuffer.writeInt(off.price);
				packetBuffer.writeInt(off.interval);
				packetBuffer.writeInt(off.stopAfter);
				packetBuffer.writeEnumValue(off.mode);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
