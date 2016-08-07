package net.teamio.gtams.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.entities2.TradeDescriptor;
import net.teamio.gtams.client.entities2.TradeInfo;
import net.teamio.gtams.gui.ContainerTraderTE;

public class PackageTradeInfo implements IMessage {

	public static final class HandlerServer implements IMessageHandler<PackageTradeInfoRequest, PackageTradeInfo> {

		@Override
		public PackageTradeInfo onMessage(PackageTradeInfoRequest message, MessageContext ctx) {
			Container container = ctx.getServerHandler().playerEntity.openContainer;
			PackageTradeInfo response;
			if(container instanceof ContainerTraderTE) {
				TradeInfo ti = GTams.gtamsClient.getTradeInfo(new TradeDescriptor(message.stack));

				response = new PackageTradeInfo(ti);
			} else {
				response = new PackageTradeInfo();
			}
			return response;
		}
	}

	public static final class HandlerClient implements IMessageHandler<PackageTradeInfo, IMessage> {

		@Override
		public IMessage onMessage(PackageTradeInfo message, MessageContext ctx) {
			Container container = Minecraft.getMinecraft().thePlayer.openContainer;
			if(container instanceof ContainerTraderTE) {

				((ContainerTraderTE)container).setTradeInfo(message.info);
			} else {
				//TODO: Log
				System.out.println("Wrong container open");
			}
			return null;
		}

	}

	public TradeInfo info;

	public PackageTradeInfo() {
	}

	public PackageTradeInfo(TradeInfo info) {
		this.info = info;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);

		try {
			TradeInfo ti = new TradeInfo();
			ti.meanPrice = packetBuffer.readFloat();
			ti.supply = packetBuffer.readInt();
			ti.demand = packetBuffer.readInt();
			ti.supplyDemandFactor = packetBuffer.readFloat();
			ti.tradesLastPeriod = packetBuffer.readInt();
			ti.volumeLastPeriod = packetBuffer.readInt();

			boolean hasTradeDescriptor = packetBuffer.readBoolean();
			if(hasTradeDescriptor) {
				ti.trade = new TradeDescriptor();
				ti.trade.itemName = packetBuffer.readStringFromBuffer(255);
				ti.trade.damage = packetBuffer.readInt();
				ti.trade.nbtHash = packetBuffer.readStringFromBuffer(255);
			}

			this.info = ti;
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
			packetBuffer.writeFloat(info.meanPrice);
			packetBuffer.writeInt(info.supply);
			packetBuffer.writeInt(info.demand);
			packetBuffer.writeFloat(info.supplyDemandFactor);
			packetBuffer.writeInt(info.tradesLastPeriod);
			packetBuffer.writeInt(info.volumeLastPeriod);
			boolean hasTradeDescriptor = info.trade != null;

			packetBuffer.writeBoolean(hasTradeDescriptor);
			if(hasTradeDescriptor) {
				packetBuffer.writeString(info.trade.itemName);
				packetBuffer.writeInt(info.trade.damage);
				packetBuffer.writeString(info.trade.nbtHash);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
