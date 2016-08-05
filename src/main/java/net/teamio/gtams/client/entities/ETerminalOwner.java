package net.teamio.gtams.client.entities;

import java.util.UUID;

public class ETerminalOwner {

	public UUID id;
	public UUID owner;

	/**
	 * @param id
	 * @param online
	 */
	public ETerminalOwner(UUID id, UUID owner) {
		this.id = id;
		this.owner = owner;
	}

}
