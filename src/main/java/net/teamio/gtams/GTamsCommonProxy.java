package net.teamio.gtams;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.teamio.gtams.network.PackageNewTradeRequest;
import net.teamio.gtams.network.PackageTerminalData;
import net.teamio.gtams.network.PackageTradeInfo;
import net.teamio.gtams.network.PackageTradeInfoRequest;
import net.teamio.gtams.network.PackageTerminalDataRequest;

public abstract class GTamsCommonProxy {

	public void registerPackets(SimpleNetworkWrapper network) {
		network.registerMessage(PackageTerminalData.HandlerServer.class, PackageTerminalDataRequest.class, 1, Side.SERVER);
		network.registerMessage(PackageTerminalData.HandlerClient.class, PackageTerminalData.class, 2, Side.CLIENT);
		network.registerMessage(PackageTradeInfo.HandlerServer.class, PackageTradeInfoRequest.class, 4, Side.SERVER);
		network.registerMessage(PackageTradeInfo.HandlerClient.class, PackageTradeInfo.class, 5, Side.CLIENT);
		network.registerMessage(PackageNewTradeRequest.HandlerServer.class, PackageNewTradeRequest.class, 6, Side.SERVER);
	}
}
