package de.robfro.secrethitler.gamer;


import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.world.Room;
import mkremins.fanciful.FancyMessage;

public class Gamer {

	public Player player;
	public String name;
	public int state; // 0...Lobby, 1...Ingame, 100+...itemFrames,
						// 200+...ElectionTracker, 300...Sign
	public String longName;

	public boolean isDummy = false;
	public Inventory inventory;

	// DummyMaster Properties:
	public ArrayList<Gamer> dummies;
	public int cDummy = -1;

	// Dummy Properties:
	public Gamer master;
	
	// Flags
	public boolean inputLongName = false;
	public Room editingRoom;

	// EchterSpieler Konstruktor
	public Gamer(Player player) {
		this.player = player;
		player.getInventory().clear();

		name = player.getName();
		state = 0;

		isDummy = true;
		dummies = new ArrayList<>();
		cDummy = -1;
	}

	// Dummy Konstruktor
	public Gamer(String name, Gamer master) {
		this.master = master;
		player = master.player;
		this.name = name;
		state = 0;

		isDummy = true;
		inventory = Main.i.getServer().createInventory(null, InventoryType.PLAYER);
	}

	// Lade Daten aus der player.yml
	public void loadYAML() {
		FileConfiguration c = Main.i.saves.players;
		longName = c.getString(name + ".longName", name);
		isDummy = c.getBoolean(name + ".isDummy", false);
		String dlist = c.getString(name + ".dummies", null);

		if (dlist != null && !isDummy) {
			for (String dname : dlist.split(",")) {
				Gamer d = new Gamer(dname, this);
				d.loadYAML();
				d.isDummy = true;
				dummies.add(d);
				Main.i.gamermgr.gamers.add(d);
			}
		}

	}

	// Speichere Daten in player.yml
	public void saveYAML() {
		FileConfiguration c = Main.i.saves.players;
		c.set(name + ".longName", longName);
		c.set(name + ".isDummy", isDummy);

		if (dummies != null) {
			String dummylist = "";
			for (Gamer d : dummies) {
				if (!dummylist.equals(""))
					dummylist += ",";
				dummylist += d.name;
			}
			if (dummies.size() > 0)
				c.set(name + ".dummies", dummylist);
		}
	}

	// Sende eine Nachricht an den Spieler, wenn ein Dummy angesprochen wird
	// kommt an @Dummy davor
	public void sendMessage(String text) {
		if (!isDummy || master.isCurrentDummy(this))
			player.sendMessage(text);
		else
			player.sendMessage(ChatColor.GRAY + "@" + name + ": " + ChatColor.RESET + text);
	}

	// Hole das Inventar des Spielers, gib das FakeInventar aus, wenn der Dummy
	// angesprochen wird
	public Inventory getInventory() {
		if (!isDummy || master.isCurrentDummy(this))
			return player.getInventory();
		else {
			return inventory;
		}
	}

	// Methode für den Master: prüft, ob gegebener Dummy aktiv ist.
	public boolean isCurrentDummy(Gamer dummy) {
		if (cDummy >= 0 && cDummy < dummies.size())
			return dummies.get(cDummy).name.equals(dummy.name);
		else
			return false;
	}

	// Sende eine Willkommensnachricht
	public void sendWelcomeMessage() {
		FileConfiguration c = Main.i.saves.config;
		// Überschrift
		new FancyMessage(c.getString("tr.lobby.welcome")).style(ChatColor.BOLD).color(ChatColor.BLUE)
				.send(player);

		// Seriöser Name
		new FancyMessage(c.getString("tr.lobby.current_longname"))
		.then(longName).color(ChatColor.GRAY)
		.then(" ")
		.then(c.getString("tr.lobby.change_longname")).color(ChatColor.AQUA).tooltip(c.getString("tr.lobby.change_tooltip").split("\\|")).command("/chgnm").send(player);
	}

	

}
