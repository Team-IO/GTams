package net.teamio.gtams.client.entities;

import java.util.UUID;

public class EClientData {

	public UUID id;
	public boolean online;

	/**
	 * @param id
	 * @param online
	 */
	public EClientData(UUID id, boolean online) {
		this.id = id;
		this.online = online;
	}

}
