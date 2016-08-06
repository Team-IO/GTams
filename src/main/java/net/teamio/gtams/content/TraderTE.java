package net.teamio.gtams.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.Goods;
import net.teamio.gtams.client.GoodsList;
import net.teamio.gtams.client.Owner;
import net.teamio.gtams.client.Trade;
import net.teamio.gtams.client.TradeDescriptor;
import net.teamio.gtams.client.TradeList;
import net.teamio.gtams.client.TradeTerminal;

public class TraderTE extends TileEntity implements ITickable {

	private UUID ownerId;
	private UUID terminalId;
	private Owner owner;
	private TradeTerminal terminal;

	public final ItemStackHandler itemHandler;

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

	@Override
	public void update() {
		if(worldObj.isRemote) {
			return;
		}
		if(--checkTick == 0) {
			checkTick = CHECK_TICKS;

			if(terminal != null) {
				TradeList tl = GTams.gtamsClient.getTrades(terminal);
				GoodsList gl = GTams.gtamsClient.getGoods(terminal);

				if(tl.trades == null ||
						tl.trades.isEmpty()) {
					return;
				}

				Map<TradeDescriptor, Integer> goodsSimulation = new HashMap<TradeDescriptor, Integer>();

				if(gl.goods == null) {
					gl.goods = new ArrayList<Goods>();
				} else {
					for(Goods g : gl.goods) {
						goodsSimulation.put(g.what, g.unlocked + g.locked);
					}
				}

				int slots = itemHandler.getSlots();
				TradeDescriptor[] inInventory = new TradeDescriptor[slots];
				for(int i = 0; i < slots; i++) {
					ItemStack stack = itemHandler.getStackInSlot(i);
					if(stack != null) {
						inInventory[i] = new TradeDescriptor(stack);
					}
				}

				List<ItemStack> transferToServer = new ArrayList<ItemStack>();

				for(Trade t : tl.trades) {
					if(t.isBuy) {
						continue;
					}
					Integer g = goodsSimulation.get(t.descriptor);
					int inStock = 0;
					if(g != null) {
						inStock = g;
					}
					if(inStock >= t.amount) {
						inStock -= t.amount;
					} else {
						int missing = t.amount - inStock;
						inStock = 0;

						for(int i = 0; i < slots; i++) {
							if(inInventory[i] != null && inInventory[i].equals(t.descriptor)) {
								ItemStack stack = itemHandler.extractItem(i, missing, false);
								if(stack.stackSize > 0 ) {
									missing -= stack.stackSize;
									transferToServer.add(stack);
								}
								if(itemHandler.getStackInSlot(i) == null) {
									inInventory[i] = null;
								}
								if(missing <= 0) {
									break;
								}
							}
						}
					}
					goodsSimulation.put(t.descriptor, inStock);
				}

				gl.goods.clear();
				for(ItemStack stack : transferToServer) {
					Goods goods = new Goods();
					goods.what = new TradeDescriptor(stack);
					goods.locked = stack.stackSize;
					gl.goods.add(goods);
				}
				GTams.gtamsClient.addGoods(terminal, gl);
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
		compound.setUniqueId("owner", ownerId);
		if(terminal != null) {
			terminalId = terminal.id;
			compound.setUniqueId("terminal", terminalId);
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
