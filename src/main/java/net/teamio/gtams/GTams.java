package net.teamio.gtams;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.teamio.gtams.client.GTamsClient;
import net.teamio.gtams.content.TraderBlock;
import net.teamio.gtams.gui.GuiHandler;

@Mod(modid = GTams.MODID, version = GTams.VERSION)
public class GTams {
	// TODO: make gradle update this!
	public static final String MODID = "gtams";
	public static final String VERSION = "1.0-alpha1";
	private static final String CHANNEL_NAME = "gtams.net";

	@Instance(MODID)
	public static GTams INSTANCE;

	@SidedProxy(clientSide = "net.teamio.gtams.GTamsClientProxy", serverSide = "net.teamio.gtams.GTamsServerProxy")
	public static GTamsCommonProxy proxy;

	public static TraderBlock blockTrader;
	public static SimpleNetworkWrapper channel;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		CreativeTabs creativeTab = new CreativeTabs("taam.creativetab") {

			@Override
			public Item getTabIconItem() {
				// TODO Auto-generated method stub
				return Items.BOOK;
			}

		};

		blockTrader = new TraderBlock();
		blockTrader.setRegistryName(MODID, "trader");
		blockTrader.setUnlocalizedName("gtams.trader");
		blockTrader.setCreativeTab(creativeTab);
		GameRegistry.register(blockTrader);
		ItemBlock itemTrader = new ItemBlock(blockTrader);
		itemTrader.setRegistryName(MODID, "trader");
		itemTrader.setUnlocalizedName("gtams.trader");
		itemTrader.setCreativeTab(creativeTab);
		GameRegistry.register(itemTrader);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GTamsClient gtamsClient = new GTamsClient(Config.server_host, Config.server_port);
		gtamsClient.authenticate();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
		proxy.registerPackets(channel);
	}
}
