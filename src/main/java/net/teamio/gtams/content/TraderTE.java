package net.teamio.gtams.content;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.entities2.GoodsList;
import net.teamio.gtams.client.entities2.Owner;
import net.teamio.gtams.client.entities2.TradeList;
import net.teamio.gtams.client.entities2.TradeTerminal;
import net.teamio.gtams.client.tasks.TaskRefreshTerminal;

public class TraderTE extends TileEntity implements ITickable {

	public UUID ownerId;
	public UUID terminalId;
	private Owner owner;
	private TradeTerminal terminal;

	public final ItemStackHandler itemHandler;

	public TradeList tradesCache;
	public GoodsList goodsCache;

	public TraderTE() {
		itemHandler = new ItemStackHandler(12);
	}

	public void setOwner(UUID ownerId) {
		if(worldObj.isRemote) {
			return;
		}
		if(ownerId != null) {
			this.ownerId = ownerId;
			this.owner = GTams.gtamsClient.getOwner(ownerId);
			if(terminal == null) {
				terminal = owner.getTerminal(terminalId);
			} else {
				terminal.transferOwner(owner);
			}
		}
	}

	@Override
	public void onLoad() {
		if(worldObj.isRemote) {
			return;
		}
		setOwner(ownerId);
	}

	@Override
	public void onChunkUnload() {
		if(worldObj.isRemote) {
			return;
		}
		if(ownerId != null) {
			owner.terminalOffline(terminal);
			terminalId = terminal.id;
			terminal = null;
			owner = null;
		}
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

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) itemHandler;
		}
		return super.getCapability(capability, facing);
	}

	private static final int CHECK_TICKS = 50;
	private int checkTick = CHECK_TICKS;
	private TaskRefreshTerminal refreshTask;

	@Override
	public void update() {
		if(worldObj.isRemote) {
			return;
		}

		if(terminalId != null) {
			if(refreshTask == null) {
				if(--checkTick == 0) {
					checkTick = CHECK_TICKS;
					refreshTask = new TaskRefreshTerminal(this);
					GTams.gtamsClient.addTask(refreshTask);
				}
			} else {
				if(refreshTask.isDone) {
					this.tradesCache = refreshTask.tradesCache;
					this.goodsCache = refreshTask.goodsCache;
					refreshTask = null;
				} else {
					refreshTask.processSyncTasks();
				}
			}
		}
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
		if(ownerId != null) {
			compound.setUniqueId("owner", ownerId);
		}
		if(terminal != null) {
			terminalId = terminal.id;
			if(terminalId != null) {
				compound.setUniqueId("terminal", terminalId);
			}
		}
		NBTTagCompound itemTag = itemHandler.serializeNBT();
		compound.setTag("inventory", itemTag);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		ownerId = compound.getUniqueId("owner");
		terminalId = compound.getUniqueId("terminal");
		NBTTagCompound itemTag = compound.getCompoundTag("inventory");
		itemHandler.deserializeNBT(itemTag);
	}
}
