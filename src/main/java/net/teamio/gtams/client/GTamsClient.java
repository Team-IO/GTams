package net.teamio.gtams.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.teamio.gtams.client.entities2.GoodsList;
import net.teamio.gtams.client.entities2.Owner;
import net.teamio.gtams.client.entities2.Trade;
import net.teamio.gtams.client.entities2.TradeList;
import net.teamio.gtams.client.entities2.TradeTerminal;
import net.teamio.gtams.client.tasks.TaskRefreshPlayerInfo;

public abstract class GTamsClient {

	protected final Map<UUID, Owner> owners;
	public final Object sync_object = new Object();
	private BlockingQueue<Runnable> pendingTasks = new LinkedBlockingQueue<>();
	private ThreadPoolExecutor tp = new ThreadPoolExecutor(5, 30, 5, TimeUnit.SECONDS, pendingTasks, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "GTams Worker Thread");
			}
		});

	public GTamsClient() {
		owners = new HashMap<>();
	}

	public Owner getOwner(UUID id) {
		synchronized(sync_object) {
			Owner owner = owners.get(id);
			if (owner == null) {
				owner = new Owner(this, id);
				owners.put(id, owner);
				notifyClientOnline(owner);
			}
			if(owner.refreshTask == null ||
				(owner.refreshTask.isDone &&
				(System.currentTimeMillis() - owner.refreshTask.lastUpdate) > 2000
				)) {
				owner.refreshTask = new TaskRefreshPlayerInfo(owner);
				addTask(owner.refreshTask);
			}
			return owner;
		}
	}

	public void logoffOwner(Owner owner) {
		synchronized(sync_object) {
			owners.remove(owner.id);
			notifyClientOffline(owner);
		}
	}

	public abstract void notifyTerminalOnline(TradeTerminal terminal);

	public abstract void notifyTerminalOffline(TradeTerminal terminal);

	public abstract void registerNewTerminal(TradeTerminal terminal);

	/**
	 * Removes a terminal from the list, deleting all related trades.
	 * @param terminal
	 */
	public abstract void destroyTerminal(TradeTerminal terminal);

	public abstract void notifyClientOffline(Owner owner);

	public abstract void notifyClientOnline(Owner owner);

	public abstract void changeTerminalOwner(TradeTerminal tradeTerminal, Owner newOwner);

	public abstract TradeList getTrades(TradeTerminal terminal);

	public abstract TradeList createTrade(TradeTerminal terminal, Trade trade);

	public abstract GoodsList getGoods(TradeTerminal terminal);

	public abstract GoodsList addGoods(TradeTerminal terminal, GoodsList gl);

	public void addTask(Task newTask) {
		tp.execute(newTask);
	}

	public abstract GoodsList removeGoods(TradeTerminal terminal, GoodsList request);

	public abstract void updateOwnerInfo(Owner owner);

}