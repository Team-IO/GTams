package net.teamio.gtams.content;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.teamio.gtams.GTams;

public class TraderBlock extends Block {

	public static AxisAlignedBB bounds = new AxisAlignedBB(0, 0, 0, 1, 1 + 2/16f, 1);

	public TraderBlock() {
		super(Material.IRON, MapColor.BLUE);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TraderTE();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return bounds;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		playerIn.openGui(GTams.INSTANCE, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		TraderTE te = (TraderTE) worldIn.getTileEntity(pos);
		te.setOwner(placer.getPersistentID());
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TraderTE te = (TraderTE) worldIn.getTileEntity(pos);
		te.onBlockBreak();
	}
}
