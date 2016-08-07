package net.teamio.gtams.client.tasks;

import java.util.UUID;

import net.teamio.gtams.GTams;
import net.teamio.gtams.client.GTamsException;
import net.teamio.gtams.client.Task;
import net.teamio.gtams.client.entities2.Owner;
import net.teamio.gtams.client.entities2.TradeTerminal;
import net.teamio.gtams.content.TraderTE;

public class TaskLogonTerminal extends Task {
	private TraderTE tileEntity;
	private UUID ownerId;
	private UUID terminalId;

	public TaskLogonTerminal(TraderTE tileEntity) {
		this.tileEntity = tileEntity;
		this.ownerId = tileEntity.ownerId;
		this.terminalId = tileEntity.terminalId;
	}

	@Override
	public void process() throws GTamsException {
		Owner owner = GTams.gtamsClient.getOwner(ownerId);
		TradeTerminal terminal = tileEntity.terminal;
		if (terminal == null) {
			terminal = owner.getTerminal(terminalId);
		} else {
			terminal.transferOwner(owner);
		}

		tileEntity.owner = owner;
		tileEntity.terminal = terminal;
	}

	@Override
	protected void doInSync() {
	}

}
