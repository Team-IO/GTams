package net.teamio.gtams;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.teamio.gtams.client.GTamsClient;

@Mod(modid = GTams.MODID, version = GTams.VERSION)
public class GTams {
	// TODO: make gradle update this!
	public static final String MODID = "gtams";
	public static final String VERSION = "1.0-alpha1";

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GTamsClient.authenticate();
	}
}
