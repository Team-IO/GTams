package net.teamio.gtams.client.tasks;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.teamio.gtams.GTams;
import net.teamio.gtams.client.GTamsException;
import net.teamio.gtams.client.Task;
import net.teamio.gtams.client.entities2.Owner;

public class TaskRefreshPlayerInfo extends Task {

	private Owner owner;
	public long lastUpdate;

	public TaskRefreshPlayerInfo(Owner info) {
		this.owner = info;
	}

	@Override
	public void process() throws GTamsException {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		GameProfile prof = server.getPlayerProfileCache().getProfileByUUID(owner.id);
		owner.name = prof.getName();
		GTams.gtamsClient.updateOwnerInfo(owner);
		this.lastUpdate = System.currentTimeMillis();
	}

	@Override
	protected void doInSync() throws GTamsException {
	}

}
