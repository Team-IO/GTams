package net.teamio.gtams;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Config {

	public static Configuration config;
	public static Configuration config_token;


	private static String client_token;

	public static boolean snooping = true;
	public static boolean transmit_username = true;

	public static String server_host = "localhost";
	public static int server_port = 60405;


	public static void loadConfig(File file, File configDir) {
		File tokenConfig = new File(configDir.getParentFile(), "gtams_token.cfg");

		config = new Configuration(file);
		config_token = new Configuration(tokenConfig);

		config_token.getCategory("general").setComment("This file identifies your installation of GTams. Do not copy this file to modpacks, other computers, etc. In fact, do not copy it at all!");
		// Do not reload the token, it should not change anyways
		client_token = config_token.getString("token", "general", "", "Installation identifier. Do not change this, unless absolutely necessary.");

		config_token.save();

		loadConfig();
	}

	public static String getClientToken() {
		return client_token;
	}

	public static void setClientToken(String token) {
		client_token = token;
		config_token.get("general", "token", "").set(token);
		config_token.save();
	}

	public static void loadConfig() {
		server_host = config.getString("server_host", "general", "gtams.team-io.net", "Connection info, where to find the GTams Server to use. Defaults to the public Team I/O server.");
		server_port = config.getInt("server_port", "general", 60405, 1, 65535, "Connection info, on which port the GTams Server responds.");
		snooping = config.getBoolean("snooping", "general", true, "Allow collecting of (anonymous) data about your installation. Not required for this mod to function properly. We just like to know stuff :P");
		transmit_username = config.getBoolean("transmit_username", "general", true, "Allow transmission of usernames when notifying online presence. Not required for this mod to function properly, but very handy for assisting you if there is trouble.");

		config.save();
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		loadConfig();
	}
}
