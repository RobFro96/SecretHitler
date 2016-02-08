package de.robfro.secrethitler.general;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.world.Room;

public class AdminTools {

	// Befehl zum Testen verschiedener Funktionen des Plugins
	public boolean onCommandTEST(CommandSender sender, Command command, String label, String[] args) {
		// CHECK: Admin
		if (!sender.hasPermission("sh.admin")) {
			Main.i.mylib.sendError(sender, "no_permission");
			return true;
		}

		// CHECK: Mind. 1 Argument
		if (args.length < 1) {
			Main.i.mylib.sendError(sender, "number_args");
			return true;
		}

		Gamer g = Main.i.gamermgr.getGamer(sender.getName());

		switch (args[0]) {
		case "board":
			if (g == null) {
				Main.i.mylib.sendError(sender, "wrong_sender");
				return true;
			}
			for (Entity e : g.player.getWorld().getNearbyEntities(g.player.getLocation(), 10, 10, 10)) {
				if (e instanceof ItemFrame)
					g.player.sendMessage(e.getLocation().toString());
			}
			return true;
		case "listgamers":
			String str = "";
			for (Gamer gamer : Main.i.gamermgr.gamers) {
				if (str != "")
					str += ", ";
				str += gamer.name;
			}
			sender.sendMessage(str);
			return true;
		case "tell":
			if (args.length < 3)
				return true;
			Gamer rec = Main.i.mylib.getGamerFromName(args[1]);
			if (rec == null)
				return true;
			rec.sendMessage(args[2]);
		}


		return true;
	}

	// Befehle zum Wechseln der Dummies
	public boolean onCommandDUMMY(CommandSender sender, Command command, String label, String[] args) {
		// CHECK: Admin
		if (!sender.hasPermission("sh.admin")) {
			Main.i.mylib.sendError(sender, "no_permission");
			return true;
		}

		Gamer g = Main.i.mylib.getGamerFromSender(sender);
		if (g == null)
			return true;

		if (g.master != null)
			g = g.master;

		if (g.dummies == null) {
			Main.i.mylib.sendError(sender, "no_dummies");
			return true;
		}

		// Der Gamer g ist nun sicher ein echter Spieler
		int changeTo = -1;

		// Wenn mehr als 1 Argument gegeben: Fehler
		if (args.length > 1) {
			Main.i.mylib.sendError(sender, "number_args");
			return true;
		}


		// Wenn ein Argument gegeben ist: interpretiere!
		if (args.length == 1) {
			changeTo = MyLib.ParseInt(args[0]);
			// Wenn das Argument keine Zahl ist, nicht in der richtigen
			// Größenordnung
			if (changeTo == Integer.MIN_VALUE || changeTo < -1 || changeTo >= g.dummies.size()) {
				changeTo = -1;
				for (int i = 0; i < g.dummies.size(); i++) {
					if (g.dummies.get(i).name.equals(args[0])) {
						changeTo = i;
						break;
					}
				}
			}
		}

		// Sichere das Inventar
		if (g.cDummy == -1) {
			g.inventory = Main.i.mylib.copyInventory(g.player.getInventory());
		} else {
			g.dummies.get(g.cDummy).inventory = Main.i.mylib.copyInventory(g.player.getInventory());
		}

		g.cDummy = changeTo;

		// Lade das Inventar / Nachricht
		if (g.cDummy == -1) {
			g.player.getInventory().setContents(g.inventory.getContents());
			Main.i.mylib.sendInfo(g, "disable_dummy");
		} else {
			g.player.getInventory().setContents(g.dummies.get(g.cDummy).inventory.getContents());
			g.sendMessage(ChatColor.GREEN + Main.i.saves.config.getString("tr.info.change_dummy")
					+ g.dummies.get(g.cDummy).name);
		}
		return true;
	}

	// Befehl zum Bearbeiten / Erstellen eines Raumes
	public boolean onCommandROOM(CommandSender sender, Command command, String label, String[] args) {
		// CHECK: Admin
		if (!sender.hasPermission("sh.admin")) {
			Main.i.mylib.sendError(sender, "no_permission");
			return true;
		}

		Gamer g = Main.i.mylib.getGamerFromSender(sender);
		if (g == null)
			return true;

		// CHECK: nicht ingame
		if (g.state == 1) {
			Main.i.mylib.sendError(sender, "ingame");
			return true;
		}

		// CHECK: 2 Argumente
		if (args.length != 2) {
			Main.i.mylib.sendError(sender, "number_args");
			return true;
		}

		if (args[1].equals("create") || args[1].equals("cr")) {
			// CHECK: Name existiert nicht
			if (Main.i.rooms.rooms.containsKey(args[0])) {
				Main.i.mylib.sendError(sender, "room_exists");
				return true;
			}

			Main.i.rooms.rooms.put(args[0], new Room(args[0]));
			Main.i.mylib.sendInfo(g, "room_created");

			Main.i.rooms.save();
			return true;
		}

		// CHECK: Name existiert
		if (!Main.i.rooms.rooms.containsKey(args[0])) {
			Main.i.mylib.sendError(sender, "room_exists_not");
			return true;
		}
		Room r = Main.i.rooms.rooms.get(args[0]);

		switch (args[1]) {
		case "spawn":
			r.spawn = g.player.getLocation();
			Main.i.rooms.save();
			Main.i.mylib.sendInfo(sender, "room_spawn");
			return true;
		case "itemFrame":
		case "if":
			g.state = 100;
			g.editingRoom = r;
			sendClickMessage(g);
			return true;
		case "electionTracker":
		case "et":
			g.state = 200;
			g.editingRoom = r;
			sendClickMessage(g);
			return true;
		case "sign":
			g.state = 300;
			g.editingRoom = r;
			sendClickMessage(g);
			return true;
		}

		return false;
	}

	// Sende den nächsten Block der angeklickt werden muss.
	public void sendClickMessage(Gamer g) {
		FileConfiguration c = Main.i.saves.config;
		int nr = g.state % 100;
		if (g.state >= 300)
			g.sendMessage(c.getString("tr.info.room_click") + c.getString("tr.info.room_sign"));
		else if (g.state >= 200)
			g.sendMessage(c.getString("tr.info.room_click") + c.getString("tr.info.room_et" + nr));
		else if (g.state >= 100)
			g.sendMessage(c.getString("tr.info.room_click") + c.getString("tr.info.room_if" + nr));
		
	}

	// Wenn Spieler auf eine Block klickt
	public boolean onPlayerClickBlock(Player player, Location loc) {
		Gamer g = Main.i.mylib.getGamerFromSender(player);
		if (g == null)
			return false;

		loc = MyLib.IntergerizeLocation(loc);
		
		if (g.state >= 300) {
			// JoinSchild
			Sign sign = Main.i.mylib.getSignInLocation(loc);
			
			if (sign == null) {
				Main.i.mylib.sendError(g, "sign_not_exists");
				return true;
			}
			
			g.editingRoom.signloc = loc;
			Main.i.mylib.sendInfo(g, "all_pos");
			g.state = 0;
			Main.i.rooms.save();
			return true;
			
		} else if (g.state >= 200) {
			// Election Tracker
			g.editingRoom.electionTracker[g.state % 100] = loc;
			g.state++;
			if (g.state == 203) {
				Main.i.mylib.sendInfo(g.player, "all_pos");
				g.state = 0;
				Main.i.rooms.save();
			} else {
				sendClickMessage(g);
			}
			return true;
		} else if (g.state >= 100) {
			// ItemFrame
			ItemFrame itemf = Main.i.mylib.getItemFrameInLocation(loc);
			
			if (itemf == null) {
				Main.i.mylib.sendError(g.player, "if_not_exists");
				return true;
			}
			
			g.editingRoom.itemFrameLocations[g.state % 100] = loc;
			g.state++;
			if (g.state == 111) {
				Main.i.mylib.sendInfo(g.player, "all_pos");
				g.state = 0;
				Main.i.rooms.save();
			} else {
				sendClickMessage(g);

			}
			return true;
		}

		return false;
	}
}
