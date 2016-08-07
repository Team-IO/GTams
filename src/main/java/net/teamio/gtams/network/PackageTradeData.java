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
import net.teamio.gtams.client.Goods;
import net.teamio.gtams.client.GoodsList;
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
				GoodsList gl = GTams.gtamsClient.getGoods(ctte.trader.getTerminal());
				if(tl == null || gl == null) {
					//TODO: what to do?
				} else {
					return new PackageTradeData(tl.trades, gl.goods);
				}
			} else {
				return new PackageTradeData();
			}
			return null;
		}
	}

	public static final class HandlerClient implements IMessageHandler<PackageTradeData, IMessage> {

		@Override
		public IMessage onMessage(PackageTradeData message, MessageContext ctx) {
			Container container = Minecraft.getMinecraft().thePlayer.openContainer;
			if(container instanceof ContainerTraderTE) {

				((ContainerTraderTE)container).setTrades(message.trades);
				((ContainerTraderTE)container).setGoods(message.goods);
			} else {
				//TODO: Log
				System.out.println("Wrong container open");
			}
			return null;
		}

	}

	public final ArrayList<Trade> trades;
	public final ArrayList<Goods> goods;

	public PackageTradeData() {
		trades = new ArrayList<Trade>();
		goods = new ArrayList<Goods>();
	}

	public PackageTradeData(ArrayList<Trade> trades, ArrayList<Goods> goods) {
		if(goods == null) {
			goods = new ArrayList<Goods>();
		}
		if(trades == null) {
			trades = new ArrayList<Trade>();
		}
		this.trades = trades;
		this.goods = goods;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);

		try {
			trades.clear();
			int length = packetBuffer.readInt();
			trades.ensureCapacity(length);
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

				trades.add(trade);
			}
			goods.clear();
			length = packetBuffer.readInt();
			goods.ensureCapacity(length);
			for(int i = 0; i < length; i++) {
				Goods g = new Goods();

				boolean hasTradeDescriptor = packetBuffer.readBoolean();
				if(hasTradeDescriptor) {
					g.what = new TradeDescriptor();
					g.what.itemName = packetBuffer.readStringFromBuffer(255);
					g.what.damage = packetBuffer.readInt();
					g.what.nbtHash = packetBuffer.readStringFromBuffer(255);
				}
				g.amount = packetBuffer.readInt();

				goods.add(g);
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
			packetBuffer.writeInt(trades.size());
			for(Trade off : trades) {
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
			packetBuffer.writeInt(goods.size());
			for(Goods g : goods) {
				boolean hasTradeDescriptor = g.what != null;

				packetBuffer.writeBoolean(hasTradeDescriptor);
				if(hasTradeDescriptor) {
					packetBuffer.writeString(g.what.itemName);
					packetBuffer.writeInt(g.what.damage);
					packetBuffer.writeString(g.what.nbtHash);
				}
				packetBuffer.writeInt(g.amount);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
