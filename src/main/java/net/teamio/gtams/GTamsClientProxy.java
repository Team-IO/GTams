package net.teamio.gtams;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;

public class GTamsClientProxy extends GTamsCommonProxy {

	@Override
	public void registerRenderStuff() {
		OBJLoader.INSTANCE.addDomain(GTams.MODID.toLowerCase());

		ItemModelMesher itemModelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		ModelBakery.registerItemVariants(GTams.itemTrader, new ResourceLocation("gtams", "trader"));
		itemModelMesher.register(GTams.itemTrader, 0, new ModelResourceLocation("gtams:trader#inventory"));
	}

}
