package net.teamio.gtams.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.teamio.gtams.content.TraderTE;

public class GuiHandler implements IGuiHandler {

	MutableBlockPos posCache = new MutableBlockPos();

	public GuiHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID != 1) {
			//TODO: Log warning
		}
		TileEntity te = world.getTileEntity(posCache.setPos(x, y, z));
		if(te instanceof TraderTE) {
			return new ContainerTraderTE(player.inventory, (TraderTE) te);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID != 1) {
			//TODO: Log warning
		}
		TileEntity te = world.getTileEntity(posCache.setPos(x, y, z));
		if(te instanceof TraderTE) {
			return new GuiTraderTE(new ContainerTraderTE(player.inventory, (TraderTE) te));
		}
		return null;
	}

}
