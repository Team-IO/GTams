package net.teamio.gtams.client.entities2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.teamio.gtams.client.GTamsClient;
import net.teamio.gtams.client.tasks.TaskRefreshPlayerInfo;

public class Owner extends Player {

	private final Map<UUID, TradeTerminal> terminalCache;
	private final Set<TradeTerminal> onlineTerminals;

	public final GTamsClient client;

	public TaskRefreshPlayerInfo refreshTask;

	public Owner(GTamsClient client, UUID persistentID) {
		this.client = client;
		this.id = persistentID;
		terminalCache = new HashMap<>();
		onlineTerminals = new HashSet<>();
	}

	public TradeTerminal getTerminal(UUID terminalId) {
		synchronized(client.sync_object) {
			TradeTerminal terminal;
			if(terminalId == null) {
				// New terminal
				terminal = new TradeTerminal(this, null);
				client.registerNewTerminal(terminal);
			} else {
				// Existing terminal
				terminal = terminalCache.get(terminalId);
				if(terminal == null) {
					terminal = new TradeTerminal(this, terminalId);
					terminalCache.put(terminalId, terminal);
					client.notifyTerminalOnline(terminal);
				}
			}
			System.out.println("Adding terminal " + terminal.id);
			onlineTerminals.add(terminal);
			System.out.println("Terminal count: " + onlineTerminals.size());
			return terminal;
		}
	}

	public void terminalOffline(TradeTerminal terminal) {
		synchronized(client.sync_object) {
			client.notifyTerminalOffline(terminal);
			System.out.println("Logoff terminal " + terminal.id);
			if(terminal.id != null) {
				terminalCache.remove(terminal.id);
			}
			onlineTerminals.remove(terminal);
			System.out.println("Terminal count: " + onlineTerminals.size());
			terminal.isOnline = false;
			checkTerminalCount();
		}
	}

	private void checkTerminalCount() {
		if(onlineTerminals.isEmpty()) {
			client.logoffOwner(this);
		}
	}

	public void terminalDestroyed(TradeTerminal terminal) {
		synchronized (client.sync_object) {
			client.notifyTerminalOffline(terminal);
			System.out.println("Destroy terminal " + terminal.id);
			if(terminal.id != null) {
				terminalCache.remove(terminal.id);
			}
			onlineTerminals.remove(terminal);
			System.out.println("Terminal count: " + onlineTerminals.size());
			terminal.isOnline = false;
			checkTerminalCount();
		}
	}

	public void removeTerminal(TradeTerminal terminal) {
		System.out.println("Removing terminal for transfer " + terminal.id);
		if(terminal.id != null) {
			terminalCache.remove(terminal.id);
		}
		onlineTerminals.remove(terminal);
		System.out.println("Terminal count: " + onlineTerminals.size());
		checkTerminalCount();
	}

	public void addTerminal(TradeTerminal terminal) {
		System.out.println("Adding terminal from transfer " + terminal.id);
		if(terminal.id != null) {
			terminalCache.put(terminal.id, terminal);
		}
		onlineTerminals.add(terminal);
	}
}
