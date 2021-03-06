package net.teamio.gtams.client.entities2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class TradeDescriptor {

	public String itemName;
	public int damage;
	public String nbtHash;
	public byte[] nbt;

	public TradeDescriptor() {
	}

	public TradeDescriptor(String itemName, int damage) {
		this.itemName = itemName;
		this.damage = damage;
		this.nbtHash = "";
	}

	public TradeDescriptor(String itemName, int damage, String nbtHash, byte[] nbt) {
		this.itemName = itemName;
		this.damage = damage;
		this.nbtHash = nbtHash;
		this.nbt = nbt;
	}

	public TradeDescriptor(Item item, int damage) {
		ResourceLocation key = Item.REGISTRY.getNameForObject(item);
		this.itemName = key.toString();
		this.damage = damage;
		this.nbtHash = "";
	}

	public TradeDescriptor(Item item, int damage, String nbtHash, byte[] nbt) {
		ResourceLocation key = Item.REGISTRY.getNameForObject(item);
		this.itemName = key.toString();
		this.damage = damage;
		this.nbtHash = nbtHash;
		this.nbt = nbt;
	}

	/**
	 * @param itemName
	 * @param damage
	 * @param nbtHash
	 */
	public TradeDescriptor(ItemStack stack) {
		ResourceLocation key = Item.REGISTRY.getNameForObject(stack.getItem());
		this.itemName = key.toString();
		this.damage = stack.getItemDamage();
		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) {
			this.nbtHash = "";
		} else {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-256");
				try(ByteArrayOutputStream bo = new ByteArrayOutputStream()) {
					CompressedStreamTools.write(tag, new DataOutputStream(bo));
					nbt = bo.toByteArray();
					md.update(nbt);
				}

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

	public ItemStack toItemStack() {
		Item item = Item.REGISTRY.getObject(new ResourceLocation(itemName));
		if(item == null) {
			return null;
		}
		ItemStack itemStack = new ItemStack(item, 1, damage);
		if(nbt != null) {
			try(ByteArrayInputStream bi = new ByteArrayInputStream(nbt)) {
				NBTTagCompound tag = CompressedStreamTools.read(new DataInputStream(bi));
				itemStack.setTagCompound(tag);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return itemStack;
	}

	@Override
	public String toString() {
		if(nbtHash.isEmpty()) {
			return itemName + "@" + Integer.toString(damage);
		}
		return itemName + "@" + Integer.toString(damage) + " +{NBT}";
	}

	public String toFilename() {
		if(nbtHash.isEmpty()) {
			return itemName + "@" + Integer.toString(damage);
		}
		return itemName + "@" + Integer.toString(damage) + "{" + nbtHash + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + damage;
		result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
		result = prime * result + ((nbtHash == null) ? 0 : nbtHash.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradeDescriptor other = (TradeDescriptor) obj;
		if (damage != other.damage)
			return false;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
		if (nbtHash == null) {
			if (other.nbtHash != null)
				return false;
		} else if (!nbtHash.equals(other.nbtHash))
			return false;
		return true;
	}
}
