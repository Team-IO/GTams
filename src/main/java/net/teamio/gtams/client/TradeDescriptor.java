package net.teamio.gtams.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TradeDescriptor {

	public String itemName;
	public int damage;
	public String nbtHash;

	public TradeDescriptor() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param itemName
	 * @param damage
	 * @param nbtHash
	 */
	public TradeDescriptor(String itemName, int damage, String nbtHash) {
		this.itemName = itemName;
		this.damage = damage;
		this.nbtHash = nbtHash;
	}

	/**
	 * @param itemName
	 * @param damage
	 * @param nbtHash
	 */
	public TradeDescriptor(ItemStack stack) {
		ResourceLocation key = GameRegistry.findRegistry(Item.class).getKey(stack.getItem());
		this.itemName = key.toString();
		this.damage = stack.getItemDamage();
		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) {
			this.nbtHash = "";
		} else {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-256");
				ByteArrayOutputStream bo = new ByteArrayOutputStream();

				CompressedStreamTools.write(tag, new DataOutputStream(bo));
				md.update(bo.toByteArray());
				bo.close();

				byte[] digest = md.digest();
				this.nbtHash = "TODO";

				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < digest.length; i++) {
					sb.append(Integer.toHexString(digest[i] & 0xFF));
				}
				nbtHash = sb.toString();
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
