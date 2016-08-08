package net.teamio.gtams.network;

import java.util.ArrayList;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.entities2.Goods;
import net.teamio.gtams.client.entities2.GoodsList;
import net.teamio.gtams.client.entities2.Mode;
import net.teamio.gtams.client.entities2.Player;
import net.teamio.gtams.client.entities2.Trade;
import net.teamio.gtams.client.entities2.TradeDescriptor;
import net.teamio.gtams.client.entities2.TradeList;
import net.teamio.gtams.gui.ContainerTraderTE;

public class PackageTerminalData implements IMessage {

	public static final class HandlerServer implements IMessageHandler<PackageTerminalDataRequest, PackageTerminalData> {

		@Override
		public PackageTerminalData onMessage(PackageTerminalDataRequest message, MessageContext ctx) {
			Container container = ctx.getServerHandler().playerEntity.openContainer;
			if (container instanceof ContainerTraderTE) {
				ContainerTraderTE ctte = (ContainerTraderTE) container;

				UUID terminalId = ctte.trader.terminalId;
				UUID ownerId = ctte.trader.ownerId;

				TradeList tl = ctte.trader.tradesCache;
				if (tl == null)
					tl = GTams.gtamsClient.getTrades(terminalId, ownerId);
				if (tl == null)
					tl = new TradeList();

				GoodsList gl = ctte.trader.goodsCache;
				if (gl == null)
					gl = GTams.gtamsClient.getGoods(terminalId, ownerId);
				if (gl == null)
					gl = new GoodsList();

				Player playerInfo = null;
				if(ownerId != null) {
					playerInfo = GTams.gtamsClient.getOwner(ownerId);
				}

				return new PackageTerminalData(tl.trades, gl.goods, playerInfo);
			}
			return new PackageTerminalData();
		}
	}

	public static final class HandlerClient implements IMessageHandler<PackageTerminalData, IMessage> {

		@Override
		public IMessage onMessage(PackageTerminalData message, MessageContext ctx) {
			Container container = Minecraft.getMinecraft().thePlayer.openContainer;
			if (container instanceof ContainerTraderTE) {

				ContainerTraderTE ctte = (ContainerTraderTE) container;
				ctte.setTrades(message.trades);
				ctte.setGoods(message.goods);
				ctte.setPlayerInfo(message.playerInfo);
			} else {
				// TODO: Log
				System.out.println("Wrong container open");
			}
			return null;
		}

	}

	public final ArrayList<Trade> trades;
	public final ArrayList<Goods> goods;
	public final Player playerInfo;

	public PackageTerminalData() {
		trades = new ArrayList<>();
		goods = new ArrayList<>();
		playerInfo = new Player();
	}

	public PackageTerminalData(ArrayList<Trade> trades, ArrayList<Goods> goods, Player playerInfo) {
		if (goods == null) {
			goods = new ArrayList<>();
		}
		if (trades == null) {
			trades = new ArrayList<>();
		}
		if(playerInfo == null) {
			playerInfo = new Player();
		}
		this.trades = trades;
		this.goods = goods;
		this.playerInfo = playerInfo;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);

		try {
			boolean hasPlayerInfo = packetBuffer.readBoolean();
			if(hasPlayerInfo) {
				playerInfo.id = packetBuffer.readUuid();
				playerInfo.funds = packetBuffer.readLong();
				if(playerInfo.name == null) {
					playerInfo.name = "<??>";
				}
				playerInfo.name = packetBuffer.readStringFromBuffer(255);
			} else {
				playerInfo.id = null;
				playerInfo.funds = 0;
				playerInfo.name = "<??>";
			}

			trades.clear();
			int length = packetBuffer.readInt();
			trades.ensureCapacity(length);
			for (int i = 0; i < length; i++) {
				Trade trade = new Trade();

				boolean hasTradeDescriptor = packetBuffer.readBoolean();
				if (hasTradeDescriptor) {
					trade.descriptor = new TradeDescriptor();
					trade.descriptor.itemName = packetBuffer.readStringFromBuffer(255);
					trade.descriptor.damage = packetBuffer.readInt();
					trade.descriptor.nbtHash = packetBuffer.readStringFromBuffer(255);
				}
				trade.isBuy = packetBuffer.readBoolean();
				trade.price = packetBuffer.readInt();
				trade.amount = packetBuffer.readInt();
				trade.interval = packetBuffer.readInt();
				trade.stopAfter = packetBuffer.readInt();
				trade.mode = packetBuffer.readEnumValue(Mode.class);

				trades.add(trade);
			}
			goods.clear();
			length = packetBuffer.readInt();
			goods.ensureCapacity(length);
			for (int i = 0; i < length; i++) {
				Goods g = new Goods();

				boolean hasTradeDescriptor = packetBuffer.readBoolean();
				if (hasTradeDescriptor) {
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
			boolean hasPlayerInfo = playerInfo.id != null;
			packetBuffer.writeBoolean(hasPlayerInfo);
			if(hasPlayerInfo) {
				packetBuffer.writeUuid(playerInfo.id);
				packetBuffer.writeLong(playerInfo.funds);
				if(playerInfo.name == null) {
					playerInfo.name = "<??>";
				}
				packetBuffer.writeString(playerInfo.name);
			}

			packetBuffer.writeInt(trades.size());
			for (Trade off : trades) {
				boolean hasTradeDescriptor = off.descriptor != null;

				packetBuffer.writeBoolean(hasTradeDescriptor);
				if (hasTradeDescriptor) {
					packetBuffer.writeString(off.descriptor.itemName);
					packetBuffer.writeInt(off.descriptor.damage);
					packetBuffer.writeString(off.descriptor.nbtHash);
				}
				packetBuffer.writeBoolean(off.isBuy);
				packetBuffer.writeInt(off.price);
				packetBuffer.writeInt(off.amount);
				packetBuffer.writeInt(off.interval);
				packetBuffer.writeInt(off.stopAfter);
				packetBuffer.writeEnumValue(off.mode);
			}
			packetBuffer.writeInt(goods.size());
			for (Goods g : goods) {
				boolean hasTradeDescriptor = g.what != null;

				packetBuffer.writeBoolean(hasTradeDescriptor);
				if (hasTradeDescriptor) {
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
