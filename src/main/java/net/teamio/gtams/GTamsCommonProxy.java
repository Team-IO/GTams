package net.teamio.gtams;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.teamio.gtams.network.PackageOfferData;
import net.teamio.gtams.network.PackageOfferRequest;
import net.teamio.gtams.network.PackageTradeInfo;
import net.teamio.gtams.network.PackageTradeInfoRequest;

public abstract class GTamsCommonProxy {

	public void registerPackets(SimpleNetworkWrapper network) {
		network.registerMessage(PackageOfferData.HandlerServer.class, PackageOfferRequest.class, 1, Side.SERVER);
		network.registerMessage(PackageOfferData.HandlerClient.class, PackageOfferData.class, 2, Side.CLIENT);
		network.registerMessage(PackageTradeInfo.HandlerServer.class, PackageTradeInfoRequest.class, 4, Side.SERVER);
		network.registerMessage(PackageTradeInfo.HandlerClient.class, PackageTradeInfo.class, 5, Side.CLIENT);
	}
}
