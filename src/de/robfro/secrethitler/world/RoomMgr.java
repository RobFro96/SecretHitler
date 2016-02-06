package de.robfro.secrethitler.world;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;

public class RoomMgr {
	
	public HashMap<String, Room> rooms;
	
	public RoomMgr() {
		rooms = new HashMap<>();
		FileConfiguration c = Main.i.saves.rooms;
		
		for (String key: c.getKeys(false)) {
			rooms.put(key, new Room(c, key));
		}
	}
	
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
		
		if (args[0].equals("create") || args[0].equals("cr")) {
			// CHECK: Name existiert nicht
			if (Main.i.rooms.rooms.containsKey(args[1])) {
				Main.i.mylib.sendError(sender, "room_exists");
				return true;
			}
			
			Main.i.rooms.rooms.put(args[1], new Room(args[1]));
			Main.i.mylib.sendInfo(sender, "room_created");
			
			save();
		}
		
		// CHECK: Name existiert
		if (! Main.i.rooms.rooms.containsKey(args[1])) {
			Main.i.mylib.sendError(sender, "room_exists_not");
			return true;
		}
		Room r = Main.i.rooms.rooms.get(args[1]);
		
		switch (args[0]) {
		case "spawn":
			r.spawn = g.player.getLocation();
			save();
			Main.i.mylib.sendInfo(sender, "room_spawn");
			return true;
		case "itemFrame":
		case "if":
			g.state = 100;
			sendClickMessage(g);
			return true;
		case "electionTracker":
		case "et":
			g.state = 200;
			sendClickMessage(g);
			return true;
		}
		
		return false;
	}
	
	public void save() {
		for (Room r : rooms.values()) {
			r.saveYAML();
		}
		Main.i.saves.saveRooms();
	}
	
	public void sendClickMessage(Gamer g) {
		FileConfiguration c = Main.i.saves.config;
		int nr = g.state % 100;
		if (g.state >= 100 && g.state < 200)
			g.player.sendMessage(c.getString("translation.info.room_click") + c.getString("translation.info.room_if" + nr));
		else if (g.state >= 200)
			g.player.sendMessage(c.getString("translation.info.room_click") + c.getString("translation.info.room_et" + nr));
	}
}
