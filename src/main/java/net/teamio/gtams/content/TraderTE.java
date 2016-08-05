package net.teamio.gtams.content;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.teamio.gtams.client.GTamsClient;
import net.teamio.gtams.client.TradeTerminal;

public class TraderTE extends TileEntity {

	private UUID owner;
	private UUID terminalId;
	private GTamsClient client;
	private TradeTerminal terminal;

	public TraderTE() {
		// TODO Auto-generated constructor stub
	}

	public void setOwner(UUID owner) {
		if(worldObj.isRemote) {
			return;
		}
		client = GTamsClient.forOwner(owner);
		terminal.transferOwner(client);
	}

	@Override
	public void onLoad() {
		if(worldObj.isRemote) {
			return;
		}
		client = GTamsClient.forOwner(owner);
		terminal = client.getTerminal(terminalId);
	}

	@Override
	public void onChunkUnload() {
		terminal.release();
		terminal = null;
		client.release();
		client = null;
	}

	@Override
	public boolean restrictNBTCopy() {
		return true;
	}

	public void onBlockBreak() {
		client.destroyTerminal(terminal);
		terminal = null;
		client.release();
		client = null;
	}

	public TradeTerminal getTerminal() {
		return terminal;
	}

	/*
	 * Storage handling
	 */

	@Override
	public NBTTagCompound getUpdateTag() {
		// TODO Auto-generated method stub
		return super.getUpdateTag();
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		// TODO Auto-generated method stub
		super.handleUpdateTag(tag);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = super.writeToNBT(compound);
		compound.setUniqueId("owner", owner);
		terminalId = terminal.getId();
		compound.setUniqueId("terminal", terminalId);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		owner = compound.getUniqueId("owner");
		terminalId = compound.getUniqueId("terminal");
	}
}
