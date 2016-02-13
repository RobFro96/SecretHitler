package de.robfro.secrethitler.game;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.robfro.secrethitler.Main;

public class Card {

	public String name;
	public CardType type;
	public int map_id;
	public String item_name;
	
	public Card(String name, CardType type) {
		this.name = name;
		this.type = type;
		
		map_id = Main.i.saves.config.getInt("config.maps." + name);
		item_name = Main.i.saves.config.getString("tr.maps." + name);
	}
	
	public ItemStack getItemStack(boolean name_item) {
		ItemStack is = new ItemStack(Material.MAP);
		ItemMeta im = is.getItemMeta();
		is.setDurability((short)map_id);
		if (item_name != null && name_item)
			im.setDisplayName(item_name);
		is.setItemMeta(im);
		return is;
	}
	
	public ItemStack getItemStack(boolean name_item, String suffix) {
		ItemStack is = new ItemStack(Material.MAP);
		ItemMeta im = is.getItemMeta();
		is.setDurability((short)map_id);
		if (item_name != null && name_item)
			im.setDisplayName(item_name + suffix);
		is.setItemMeta(im);
		return is;
	}
	
}
