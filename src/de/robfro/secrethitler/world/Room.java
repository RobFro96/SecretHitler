package de.robfro.secrethitler.world;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.general.MyLib;

public class Room {
	
	// Konstaten: Anzahl der ItemFrames, ElectionTrackerBlöcke
	public static final int ItemFrameCount = 11;
	public static final int ElectionTrackerCount = 3;
	
	// Eigenschaften eines Raumes
	public String name;
	public Location[] itemFrameLocations;
	public Location[] electionTracker;
	public Location spawn, signloc;
	public Sign sign;
	public boolean all_right;
	
	// Spielerbezogenene Eigenschaften
	public ArrayList<Gamer> gamers;
	public boolean playing = false;
	public int waiting_time;
	
	
	// Lade einen Raum aus der YAML
	public Room(FileConfiguration c, String key) {
		name = key;	
		all_right = true;
		
		itemFrameLocations = new Location[ItemFrameCount];
		for (int i = 0; i<ItemFrameCount; i++) {
			Location l = MyLib.ParseLocation(c.getString(key + ".itemFrame" + i));
			if (l != null) {
				ItemFrame itemf = Main.i.mylib.getItemFrameInLocation(l);
				if (itemf != null)
					itemFrameLocations[i] = l;
				else {
					Main.i.getLogger().warning("ItemFrameLocation" + i + " has no ItemFrame in " + key + ".");
					all_right = false;
				}
			} else {
				Main.i.getLogger().warning("ItemFrameLocation" + i + " is not defined in " + key + ".");
				all_right = false;
			}
		}
		
		electionTracker = new Location[ElectionTrackerCount];
		for (int i = 0; i<ElectionTrackerCount; i++) {
			Location l = MyLib.ParseLocation(c.getString(key + ".electionTracker" + i));
			if (l != null) {
				electionTracker[i] = l;
			} else {
				Main.i.getLogger().warning("ElectionTracker" + i + " is not defined in " + key + ".");
				all_right = false;
			}
		}
		
		spawn = MyLib.ParseLocation(c.getString(key + ".spawn"));
		if (spawn == null) {
			Main.i.getLogger().warning("Spawn is not defined in " + key + ".");
			all_right = false;
		}
		
		signloc = MyLib.ParseLocation(c.getString(key + ".sign"));
		if (signloc != null) {
			sign = Main.i.mylib.getSignInLocation(signloc);
			if (sign == null) {
				Main.i.getLogger().warning("There is no sign in " + key + ".");
				all_right = false;
			}
		} else {
			Main.i.getLogger().warning("Sign is not defined in " + key + ".");
			all_right = false;
		}
		resetRoom();
	}
	
	// Erstelle einen neuen Raum
	public Room(String name) {
		this.name = name;
		itemFrameLocations = new Location[ItemFrameCount];
		electionTracker = new Location[ElectionTrackerCount];
		spawn = null;
		
		resetRoom();
	}
	
	// Speichere den Raum
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
		if (signloc != null) {
			c.set(name + ".sign", MyLib.LocationToString(signloc));
		}
	}

	// Beschrifte das JoinSchild neu
	public void updateSign() {
		if (!all_right)
			return;
		FileConfiguration c = Main.i.saves.config;
		sign.setLine(0, ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + "[" + name + "]");
		sign.setLine(1, c.getString("tr.lobby.player") + ": " + gamers.size() + "/" + Main.i.saves.max_player);
		if (playing)
			sign.setLine(2, ChatColor.BOLD.toString() + ChatColor.DARK_RED + c.getString("tr.lobby.playing"));
		else
			sign.setLine(2, ChatColor.BOLD.toString() + ChatColor.GREEN + c.getString("tr.lobby.waiting"));
		sign.update();
	}

	// Setzt den Raum in einen Ausgangszustand zurück.
	private void resetRoom() {
		if (!all_right)
			return;
		
		gamers = new ArrayList<>();
		waiting_time = Main.i.saves.config.getInt("config.wait.wait_at_min");
	}
	
	// Ein Spieler will diesen Raum joinen
	public void join(Gamer g) {
		if (!all_right)
			return;
		
		// CHECK: Raum spielt nicht
		if (playing) {
			Main.i.mylib.sendError(g, "room_ingame");
			return;
		}
				
		// CHECK: Noch ein Platz frei
		if (gamers.size() >= Main.i.saves.max_player) {
			Main.i.mylib.sendError(g, "room_full");
			return;
		}
		
		// Er joint
		g.state = 1;
		g.joinedRoom = this;
		gamers.add(g);
		sendMessage(ChatColor.YELLOW + Main.i.saves.config.getString("tr.waiting.join").replaceAll("#name", g.longName));
		
		if (! g.isDummy)
			g.player.teleport(spawn);
		
		updateSign();
	}
	
	// Sendet eine Nachricht an alle Spieler in diesem Raum
	public void sendMessage(String msg) {
		for (Gamer g : gamers)
			g.sendMessage(msg);
	}

	// Nach einer Sekunde wird wird die Zeit verringert
	public void onTimerOneSecond() {
		FileConfiguration c = Main.i.saves.config;
		if (gamers.size() < c.getInt("config.wait.min_player")) {
			waiting_time = c.getInt("config.wait.wait_at_min");
		}
		else {
			waiting_time--;
			if (waiting_time <= 5)
				sendTone();
		}
		setLevel(waiting_time);
	}
	
	// Sende an alle Spieler einen Ton
	private void sendTone() {
		for (Gamer g : gamers)
			g.player.playSound(g.player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
	}
	
	// Update die Levelanzeige aller Spieler
	private void setLevel(int lvl) {
		for (Gamer g : gamers)
			g.player.setLevel(lvl);
	}
	
}
