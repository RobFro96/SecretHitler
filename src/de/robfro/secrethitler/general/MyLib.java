package de.robfro.secrethitler.general;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.game.Card;

public class MyLib {

	public void sendError(CommandSender receiver, String configPath) {
		configPath = "tr.error." + configPath;
		String text = Main.i.saves.config.getString(configPath);
		receiver.sendMessage(ChatColor.RED + text);
	}

	public void sendError(Gamer g, String configPath) {
		configPath = "tr.error." + configPath;
		String text = Main.i.saves.config.getString(configPath);
		g.sendMessage(ChatColor.RED + text);
	}

	public void sendInfo(CommandSender receiver, String configPath) {
		configPath = "tr.info." + configPath;
		String text = Main.i.saves.config.getString(configPath);
		receiver.sendMessage(ChatColor.GREEN + text);
	}

	public void sendInfo(Gamer g, String configPath) {
		configPath = "tr.info." + configPath;
		String text = Main.i.saves.config.getString(configPath);
		g.sendMessage(ChatColor.GREEN + text);
	}

	public static int ParseInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			return Integer.MIN_VALUE;
		}
	}

	public static int indexOf(char[] list, char c) {
		for (int i = 0; i < list.length; i++) {
			if (list[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public static int[] GetIntArray(String str, int len) {
		if (str == null)
			return null;
		String[] split = str.split(",");
		if (split.length != len && len != -1)
			return null;
		int[] result = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			result[i] = ParseInt(split[i]);
			if (result[i] == Integer.MIN_VALUE)
				return null;
		}
		return result;
	}

	public static String IntArrayToString(int[] array) {
		String str = "";
		for (int i = 0; i < array.length; i++) {
			if (!str.equals(""))
				str += ",";
			str += String.valueOf(array[i]);
		}
		return str;
	}

	public static int BooleanListToInt(boolean[] list) {
		int res = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i])
				res += Math.pow(2, i);
		}
		return res;
	}

	public static boolean[] IntToBooleanList(int n) {
		boolean[] list = new boolean[30];
		for (int i = 0; i < 30; i++) {
			list[i] = (n % 2) == 1;
			n = n / 2;
		}
		return list;
	}

	public static Location ParseLocation(String loc) {
		if (loc == null)
			return null;
		String[] split = loc.split(",");
		if (split.length != 3)
			return null;
		return new Location(Main.i.getServer().getWorlds().get(0), ParseInt(split[0]) + .5f, ParseInt(split[1]) + .5f,
				ParseInt(split[2]) + .5f);
	}

	public static String LocationToString(Location loc) {
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		return x + "," + y + "," + z;
	}

	public Gamer getGamerFromSender(CommandSender sender) {
		Gamer g = Main.i.gamermgr.getGamer(sender.getName());
		// Wenn null: Vllt hat die Console gesendet?
		if (g == null) {
			Main.i.mylib.sendError(sender, "wrong_sender");
			return null;
		}

		if (g.cDummy == -1)
			return g;

		if (g.dummies == null)
			return g;

		return g.dummies.get(g.cDummy);
	}

	public Gamer getGamerFromName(String name) {
		Gamer g = Main.i.gamermgr.getGamer(name);
		// Wenn null: Vllt hat die Console gesendet?
		if (g == null) {
			return null;
		}

		if (g.cDummy == -1)
			return g;

		if (g.dummies == null)
			return g;

		return g.dummies.get(g.cDummy);
	}

	public Inventory copyInventory(Inventory inv) {
		Inventory i = Main.i.getServer().createInventory(null, InventoryType.PLAYER);
		i.setContents(inv.getContents().clone());
		return i;
	}

	public static Location IntergerizeLocation(Location orig) {
		return new Location(orig.getWorld(), orig.getBlockX() + .5f, orig.getBlockY() + .5f, orig.getBlockZ() + .5f);
	}

	public ItemFrame getItemFrameInLocation(Location loc) {
		ArrayList<Entity> es = new ArrayList<>(loc.getWorld().getNearbyEntities(loc, .5f, .5f, .5f));
		if (es.size() != 1)
			return null;
		Entity e = es.get(0);
		if (!(e instanceof ItemFrame))
			return null;
		return (ItemFrame) e;
	}

	public Sign getSignInLocation(Location loc) {
		BlockState bs = loc.getBlock().getState();
		if (bs instanceof Sign)
			return (Sign) bs;
		return null;
	}

	public Card getCardFromItemStack(ItemStack is) {
		if (is.getType() != Material.MAP)
			return null;
		for (Card c : Main.i.cardmgr.cards.values()) {
			if (c.map_id == is.getDurability())
				return c;
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public void setBlock(String str, Location loc) {
		String[] split = str.split(":");
		if (split.length != 2) return;
		int id = MyLib.ParseInt(split[0]);
		if (id == Integer.MIN_VALUE) return;
		int dmg = MyLib.ParseInt(split[1]);
		if (dmg == Integer.MIN_VALUE) return;
		loc.getBlock().setTypeId(id);
		loc.getBlock().setData((byte)dmg);
	}

}
