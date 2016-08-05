package net.teamio.gtams.content;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.Owner;
import net.teamio.gtams.client.TradeTerminal;

public class TraderTE extends TileEntity {

	private UUID ownerId;
	private UUID terminalId;
	private Owner owner;
	private TradeTerminal terminal;

	public TraderTE() {
		// TODO Auto-generated constructor stub
	}

	public void setOwner(UUID ownerId) {
		if(worldObj.isRemote) {
			return;
		}
		this.ownerId = ownerId;
		this.owner = GTams.gtamsClient.getOwner(ownerId);
		terminal.transferOwner(owner);
	}

	@Override
	public void onLoad() {
		if(worldObj.isRemote) {
			return;
		}
		this.owner = GTams.gtamsClient.getOwner(ownerId);
		terminal = owner.getTerminal(terminalId);
	}

	@Override
	public void onChunkUnload() {
		if(worldObj.isRemote) {
			return;
		}
		owner.terminalOffline(terminal);
		terminalId = terminal.getId();
		terminal = null;
		owner = null;
	}

	@Override
	public boolean restrictNBTCopy() {
		return true;
	}

	public void onBlockBreak() {
		if(worldObj.isRemote) {
			return;
		}
		owner.terminalDestroyed(terminal);
		terminal = null;
		owner = null;
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
		compound.setUniqueId("owner", ownerId);
		terminalId = terminal.getId();
		compound.setUniqueId("terminal", terminalId);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		ownerId = compound.getUniqueId("owner");
		terminalId = compound.getUniqueId("terminal");
	}
}
