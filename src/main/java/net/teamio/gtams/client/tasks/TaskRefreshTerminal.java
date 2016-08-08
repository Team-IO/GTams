package net.teamio.gtams.client.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.GTamsException;
import net.teamio.gtams.client.Task;
import net.teamio.gtams.client.entities2.Goods;
import net.teamio.gtams.client.entities2.GoodsList;
import net.teamio.gtams.client.entities2.Trade;
import net.teamio.gtams.client.entities2.TradeDescriptor;
import net.teamio.gtams.client.entities2.TradeList;
import net.teamio.gtams.client.entities2.TradeTerminal;
import net.teamio.gtams.content.TraderTE;

public class TaskRefreshTerminal extends Task {

	private TraderTE tileEntity;

	public GoodsList goodsCache;
	private TradeTerminal terminal;
	public TradeList tradesCache;

	private GoodsList transferFromServer;

	private int step;

	TradeDescriptor[] inInventory;

	private Map<TradeDescriptor, Integer> goodsSimulation;

	private List<ItemStack> transferToServer;

	public TaskRefreshTerminal(TraderTE tileEntity) {
		this.tileEntity = tileEntity;
	}



	@Override
	public void process() throws GTamsException {
		int retryCount = 0;
		do {
			terminal = tileEntity.terminal;
			retryCount++;
		} while(terminal == null && retryCount < 50);
		if(terminal == null) {
			throw new GTamsException("Could not get terminal");
		}
		UUID terminalId = tileEntity.terminalId;
		UUID ownerId = tileEntity.ownerId;

		//TODO: only refresh when updated?
		goodsCache = GTams.gtamsClient.getGoods(terminalId, ownerId);
		tradesCache = GTams.gtamsClient.getTrades(terminalId, ownerId);

		if(goodsCache == null || tradesCache == null) {
			//TODO: what to do?
			return;
		}

		goodsSimulation = new HashMap<>();

		if(goodsCache.goods == null) {
			goodsCache.goods = new ArrayList<>();
		} else {
			for(Goods g : goodsCache.goods) {
				goodsSimulation.put(g.what, g.amount);
			}
		}

		/*
		 * Get items from local inventory &
		 * Check which stocks to send to the server
		 */

		step = 1;
		waitForSync();



		if(!transferToServer.isEmpty()) {
			/*
			 * Create goods list for transfer
			 */
			GoodsList gl = new GoodsList();
			for(ItemStack stack : transferToServer) {
				Goods goods = new Goods();
				goods.what = new TradeDescriptor(stack);
				goods.amount = stack.stackSize;
				gl.goods.add(goods);
			}
			/*
			 * Send goods to server
			 */
			GTams.gtamsClient.addGoods(terminalId, gl);
			gl.goods.clear();
		}


		transferFromServer = new GoodsList();

		/*
		 * Simulate what would fit into the inventory
		 */

		step = 2;
		waitForSync();

		if(!transferFromServer.goods.isEmpty()) {
			/*
			 * Request removal from the server
			 */
			transferFromServer = GTams.gtamsClient.removeGoods(terminalId, transferFromServer);

			/*
			 * Actually add the removed goods to the inventory
			 */

			step = 3;
			waitForSync();
		}

	}

	private void buildInventoryDescriptors(ItemStackHandler itemHandler) {
		int slots = itemHandler.getSlots();
		inInventory = new TradeDescriptor[slots];
		for(int i = 0; i < slots; i++) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if(stack != null) {
				inInventory[i] = new TradeDescriptor(stack);
			}
		}
	}

	@Override
	protected void doInSync() {
		ItemStackHandler itemHandler = tileEntity.itemHandler;
		int slots = itemHandler.getSlots();

		if(step == 1) {
			/*
			 * Check what is in local inventory
			 */
			buildInventoryDescriptors(itemHandler);


			/*
			 * Determine transfer to server
			 */
			transferToServer = new ArrayList<>();

			for(Trade t : tradesCache.trades) {
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

		} else if(step == 2) {
			/*
			 * Check what is in local inventory
			 */
			buildInventoryDescriptors(itemHandler);
			/*
			 * Simulate what would fit into the inventory
			 */
			for(Entry<TradeDescriptor, Integer> kv : goodsSimulation.entrySet()) {
				TradeDescriptor what = kv.getKey();
				ItemStack stack = what.toItemStack();

				int maxStackSize = Math.min(stack.getMaxStackSize(), 64);
				int canFit = 0;

				for(int i = 0; i < slots; i++) {
					TradeDescriptor ii = inInventory[i];
					if(ii == null) {
						canFit += maxStackSize;
						// Lock that slot for different goods
						inInventory[i] = what;
					} else if(ii.equals(what)) {
						canFit += maxStackSize - itemHandler.getStackInSlot(i).stackSize;
						// No need to lock the slot, as the other goods will not match.
					}
				}

				int amount = Math.min(canFit, kv.getValue());

				if(amount > 0) {
					transferFromServer.goods.add(new Goods(what, amount));
				}
			}
		} else if(step == 3) {
			/*
			 * Check what is in local inventory
			 */
			buildInventoryDescriptors(itemHandler);

			/*
			 * Actually add the removed goods to the inventory
			 */
			for(Entry<TradeDescriptor, Integer> kv : goodsSimulation.entrySet()) {
				TradeDescriptor what = kv.getKey();
				ItemStack stack = what.toItemStack();

				int maxStackSize = Math.min(stack.getMaxStackSize(), 64);

				int amount = kv.getValue();

				for(int i = 0; i < slots; i++) {
					TradeDescriptor ii = inInventory[i];
					if(ii == null) {
						ItemStack copy = stack.copy();
						copy.stackSize = Math.min(maxStackSize, amount);
						amount -= copy.stackSize;
						itemHandler.setStackInSlot(i, copy);
						// Lock that slot for different goods
						inInventory[i] = what;
					} else if(ii.equals(what)) {
						ItemStack inSlot = itemHandler.getStackInSlot(i);
						int toAdd = maxStackSize - inSlot.stackSize;
						toAdd = Math.min(toAdd, amount);
						inSlot.stackSize += toAdd;
						amount -= toAdd;
						// No need to lock the slot, as the other goods will not match.
					}
					if(amount <= 0) {
						break;
					}
				}
				if(amount > 0) {
					//TODO: Store & add later!!
				}
			}
		}
	}

}
