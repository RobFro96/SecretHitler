package de.robfro.secrethitler.world;

import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;

import de.robfro.secrethitler.Main;

public class RoomMgr {
	
	public HashMap<String, Room> rooms;
	
	public RoomMgr() {
		rooms = new HashMap<>();
		FileConfiguration c = Main.i.saves.rooms;
		
		for (String key: c.getKeys(false)) {
			rooms.put(key, new Room(c, key));
		}
		
		updateAllSigns();
	}
	
	// Speichern der Räume
	public void save() {
		for (Room r : rooms.values()) {
			r.saveYAML();
		}
		Main.i.saves.saveRooms();
	}
	
	// Update alle JoinSchilder
	public void updateAllSigns() {
		for (Room r : rooms.values()) {
			r.updateSign();
		}
	}
	
}
