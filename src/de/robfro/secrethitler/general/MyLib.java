package de.robfro.secrethitler.general;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.Main;

public class MyLib {

	public void sendError(CommandSender receiver, String configPath) {
		configPath = "translation.error." + configPath;
		String text = Main.i.saves.config.getString(configPath);
		receiver.sendMessage(ChatColor.RED + text);
	}

	public void sendInfo(CommandSender receiver, String configPath) {
		configPath = "translation.info." + configPath;
		String text = Main.i.saves.config.getString(configPath);
		receiver.sendMessage(ChatColor.GREEN + text);
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
		return new Location(Main.i.getServer().getWorlds().get(0), ParseInt(split[0]), ParseInt(split[1]), ParseInt(split[2]));
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
		}
		return g;
	}

}
