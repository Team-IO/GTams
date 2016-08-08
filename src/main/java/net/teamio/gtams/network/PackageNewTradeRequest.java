package net.teamio.gtams.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.entities2.GoodsList;
import net.teamio.gtams.client.entities2.Mode;
import net.teamio.gtams.client.entities2.Player;
import net.teamio.gtams.client.entities2.Trade;
import net.teamio.gtams.client.entities2.TradeDescriptor;
import net.teamio.gtams.client.entities2.TradeList;
import net.teamio.gtams.gui.ContainerTraderTE;

public class PackageNewTradeRequest implements IMessage {

	public static final class HandlerServer implements IMessageHandler<PackageNewTradeRequest, PackageTerminalData> {

		@Override
		public PackageTerminalData onMessage(PackageNewTradeRequest message, MessageContext ctx) {
			Container container = ctx.getServerHandler().playerEntity.openContainer;
			PackageTerminalData response;
			if (container instanceof ContainerTraderTE) {
				ContainerTraderTE ctte = (ContainerTraderTE) container;
				TradeList tl = GTams.gtamsClient.createTrade(ctte.trader.terminalId, message.trade);
				GoodsList gl = GTams.gtamsClient.getGoods(ctte.trader.terminalId, ctte.trader.ownerId);

				Player playerInfo = null;
				if(ctte.trader.ownerId != null) {
					playerInfo = GTams.gtamsClient.getOwner(ctte.trader.ownerId);
				}
				if(tl == null) {
					tl = new TradeList();
				}
				if(gl == null) {
					gl = new GoodsList();
				}
				response = new PackageTerminalData(tl.trades, gl.goods, playerInfo);
			} else {
				response = new PackageTerminalData();
			}
			return response;
		}

	}

	public Trade trade;

	public PackageNewTradeRequest() {
	}

	public PackageNewTradeRequest(Trade trade) {
		this.trade = trade;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);

		try {
			Trade trade = new Trade();

			boolean hasTradeDescriptor = packetBuffer.readBoolean();
			if (hasTradeDescriptor) {
				trade.descriptor = new TradeDescriptor();
				trade.descriptor.itemName = packetBuffer.readStringFromBuffer(255);
				trade.descriptor.damage = packetBuffer.readInt();
				trade.descriptor.nbtHash = packetBuffer.readStringFromBuffer(255);
				boolean hasNbt = packetBuffer.readBoolean();
				if(hasNbt) {
					trade.descriptor.nbt = packetBuffer.readByteArray();
				}
			}
			trade.isBuy = packetBuffer.readBoolean();
			trade.price = packetBuffer.readInt();
			trade.amount = packetBuffer.readInt();
			trade.interval = packetBuffer.readInt();
			trade.stopAfter = packetBuffer.readInt();
			trade.mode = packetBuffer.readEnumValue(Mode.class);

			this.trade = trade;

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
			boolean hasTradeDescriptor = trade.descriptor != null;

			packetBuffer.writeBoolean(hasTradeDescriptor);
			if (hasTradeDescriptor) {
				packetBuffer.writeString(trade.descriptor.itemName);
				packetBuffer.writeInt(trade.descriptor.damage);
				packetBuffer.writeString(trade.descriptor.nbtHash);
				boolean hasNbt = trade.descriptor.nbt != null;
				packetBuffer.writeBoolean(hasNbt);
				if(hasNbt) {
					packetBuffer.writeByteArray(trade.descriptor.nbt);
				}
			}
			packetBuffer.writeBoolean(trade.isBuy);
			packetBuffer.writeInt(trade.price);
			packetBuffer.writeInt(trade.amount);
			packetBuffer.writeInt(trade.interval);
			packetBuffer.writeInt(trade.stopAfter);
			packetBuffer.writeEnumValue(trade.mode);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
