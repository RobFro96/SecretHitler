package de.robfro.secrethitler.world;

import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerInteractEvent;

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
	
	// Wenn ein Spieler auf ein Schild klickt
	public void onClickedSign(PlayerInteractEvent e) {
		Gamer g = Main.i.mylib.getGamerFromSender(e.getPlayer());
		if (g==null)
			return;
		// Check: In der Lobby
		if (g.state != 0) {
			Main.i.mylib.sendError(g, "ingame");
			return;
		}
		
		Room room = null;
		for (Room r : rooms.values()) {
			if (r.sign.getLocation().distance(e.getClickedBlock().getLocation())<.5f) {
				room = r;
				break;
			}
		}
		// CHECK: Raum wurde gefunden
		if (room == null)
			return;
		
		room.join(g);
	}
	
}
