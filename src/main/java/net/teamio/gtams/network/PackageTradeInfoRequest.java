package net.teamio.gtams.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PackageTradeInfoRequest implements IMessage {

	public ItemStack stack;

	public PackageTradeInfoRequest() {
		// TODO Auto-generated constructor stub
	}

	public PackageTradeInfoRequest(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);

		try {
			stack = packetBuffer.readItemStackFromBuffer();
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
			packetBuffer.writeItemStackToBuffer(stack);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
