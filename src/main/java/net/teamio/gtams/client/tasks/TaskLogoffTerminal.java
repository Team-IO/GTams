package net.teamio.gtams.client.tasks;

import java.util.UUID;

import net.teamio.gtams.GTams;
import net.teamio.gtams.client.GTamsException;
import net.teamio.gtams.client.Task;
import net.teamio.gtams.client.entities2.Owner;
import net.teamio.gtams.client.entities2.TradeTerminal;
import net.teamio.gtams.content.TraderTE;

public class TaskLogoffTerminal extends Task {

	private UUID ownerId;
	private boolean destroy;
	private TradeTerminal terminal;

	public TaskLogoffTerminal(TraderTE tileEntity, boolean destroy) {
		this.destroy = destroy;
		ownerId = tileEntity.ownerId;
		terminal = tileEntity.terminal;
	}

	@Override
	public void process() throws GTamsException {
		Owner owner = GTams.gtamsClient.getOwner(ownerId);

		// TODO: Use ID where possible, in case this is not loaded yet
		if (destroy) {
			owner.terminalDestroyed(terminal);
		} else {
			owner.terminalOffline(terminal);
		}
	}

	@Override
	protected void doInSync() {
	}

}
