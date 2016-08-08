package net.teamio.gtams.client.entities;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.teamio.gtams.Config;
import net.teamio.gtams.GTams;

public class EAuthenticateRequest {

	public String token;
	public String version;
	public String mcVersion;
	public String branding;
	public String language;

	public EAuthenticateRequest() {
		token = Config.getClientToken();
		version = GTams.VERSION;
		mcVersion = MinecraftForge.MC_VERSION;
		branding = FMLCommonHandler.instance().getModName();
		language = FMLCommonHandler.instance().getCurrentLanguage();
	}

}
