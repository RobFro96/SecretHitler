package de.robfro.secrethitler.world;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.general.MyLib;

public class Room {
	
	public static final int ItemFrameCount = 11;
	public static final int ElectionTrackerCount = 3;
	
	public String name;
	public Location[] itemFrameLocations;
	public Location[] electionTracker;
	public Location spawn;
	
	public Room(FileConfiguration c, String key) {
		name = key;
		
		itemFrameLocations = new Location[ItemFrameCount];
		for (int i = 0; i<ItemFrameCount; i++) {
			Location l = MyLib.ParseLocation(c.getString(key + ".itemFrame" + i));
			if (l != null) {
				itemFrameLocations[i] = l;
			} else {
				Main.i.getLogger().warning("ItemFrameLocation" + i + " is not defined in " + key + ".");
			}
		}
		
		electionTracker = new Location[ElectionTrackerCount];
		for (int i = 0; i<ElectionTrackerCount; i++) {
			Location l = MyLib.ParseLocation(c.getString(key + ".electionTracker" + i));
			if (l != null) {
				electionTracker[i] = l;
			} else {
				Main.i.getLogger().warning("ElectionTracker" + i + " is not defined in " + key + ".");
			}
		}
		
		spawn = MyLib.ParseLocation(c.getString(key + ".spawn"));
		if (spawn == null) {
			Main.i.getLogger().warning("Spawn is not defined in " + key + ".");
		}
	}
	
	public Room(String name) {
		this.name = name;
		itemFrameLocations = new Location[ItemFrameCount];
		electionTracker = new Location[ElectionTrackerCount];
		spawn = null;
	}
	
	public void saveYAML() {
		FileConfiguration c = Main.i.saves.rooms;
		for (int i = 0; i<ItemFrameCount; i++) {
			if (itemFrameLocations[i] != null) {
				c.set(name + ".itemFrame" + i, MyLib.LocationToString(itemFrameLocations[i]));
			}
		}
		for (int i = 0; i<ElectionTrackerCount; i++) {
			if (electionTracker[i] != null) {
				c.set(name + ".electionTracker" + i, MyLib.LocationToString(electionTracker[i]));
			}
		}
		if (spawn != null) {
			c.set(name + ".spawn", MyLib.LocationToString(spawn));
		}
	}

}
