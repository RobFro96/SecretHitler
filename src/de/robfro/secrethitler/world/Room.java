package de.robfro.secrethitler.world;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;


import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.game.Card;
import de.robfro.secrethitler.game.Role;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.general.MyLib;

public class Room {

	// Konstaten: Anzahl der ItemFrames, ElectionTrackerBl�cke
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

	// Ingame
	private Card[] facist_board;
	private Card[] liberal_board;
	private int fac_plc_placed, lib_plc_placed;

	// Lade einen Raum aus der YAML
	public Room(FileConfiguration c, String key) {
		name = key;
		all_right = true;

		itemFrameLocations = new Location[ItemFrameCount];
		for (int i = 0; i < ItemFrameCount; i++) {
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
		for (int i = 0; i < ElectionTrackerCount; i++) {
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
		for (int i = 0; i < ItemFrameCount; i++) {
			if (itemFrameLocations[i] != null) {
				c.set(name + ".itemFrame" + i, MyLib.LocationToString(itemFrameLocations[i]));
			}
		}
		for (int i = 0; i < ElectionTrackerCount; i++) {
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

	// Setzt den Raum in einen Ausgangszustand zur�ck.
	private void resetRoom() {
		if (!all_right)
			return;

		// Entfernt Spieler, wenn vorhanden
		if (gamers != null) {
			for (Gamer g : gamers) {
				g.getInventory().clear();
				g.state = 0;
				g.joinedRoom = null;
			}
		}

		gamers = new ArrayList<>();
		waiting_time = Main.i.saves.config.getInt("config.wait.wait_at_min");

		updateSign();
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
		sendMessage(
				ChatColor.YELLOW + Main.i.saves.config.getString("tr.waiting.join").replaceAll("#name", g.longName));

		if (!g.isDummy)
			g.player.teleport(spawn);

		updateSign();

		int maxtime = maxWaitTime(gamers.size());
		if (waiting_time > maxtime)
			waiting_time = maxtime;
	}

	// Wenn ein Spieler das Spiel verl�sst, muss er entfernt werden
	public void quit(Gamer g) {
		FileConfiguration c = Main.i.saves.config;

		sendMessage(
				ChatColor.YELLOW + Main.i.saves.config.getString("tr.waiting.quit").replaceAll("#name", g.longName));

		if (playing) {
			// Wenn jmd im Spiel leavt
		} else {
			gamers.remove(g);
			waiting_time += c.getInt("config.wait.less_per_player");
			onTimerOneSecond();
		}

		updateSign();
	}

	// Sendet eine Nachricht an alle Spieler in diesem Raum
	public void sendMessage(String msg) {
		for (Gamer g : gamers)
			g.sendMessage(msg);
	}

	// Nach einer Sekunde wird wird die Zeit verringert
	public void onTimerOneSecond() {
		if (playing)
			return;
		FileConfiguration c = Main.i.saves.config;
		if (gamers.size() < c.getInt("config.wait.min_player")) {
			waiting_time = c.getInt("config.wait.wait_at_min");
		} else {
			waiting_time--;

			if (waiting_time <= 0) {
				sendTone(2f);
				start();
			}

			if (waiting_time <= 5)
				sendTone(1f);

		}
		setLevel(waiting_time, c.getInt("config.wait.wait_at_min"));
	}

	// Sende an alle Spieler einen Ton
	private void sendTone(float pitch) {
		for (Gamer g : gamers)
			g.player.playSound(g.player.getLocation(), Sound.NOTE_PLING, 1f, pitch);
	}

	// Update die Levelanzeige aller Spieler
	private void setLevel(int lvl, int max) {
		for (Gamer g : gamers) {
			g.player.setLevel(lvl);
			if (lvl <= max)
				g.player.setExp(((float) lvl) / max);
			else
				g.player.setExp(1f);
		}
	}

	// Gibt die maximale Wartezeit bei der Spieleranzahl zur�ck
	public int maxWaitTime(int cnt) {
		FileConfiguration c = Main.i.saves.config;
		int min = c.getInt("config.wait.min_player");
		if (cnt < min)
			return c.getInt("config.wait.wait_at_min");
		return c.getInt("config.wait.wait_at_min") - (cnt - min) * c.getInt("config.wait.less_per_player");
	}

	// Starte das Spiel
	private void start() {
		Main.i.getLogger().info("Game started in " + name + ".");
		setLevel(0, 0);
		playing = true;
		updateSign();

		// L�sche alle Items, gebe die Vote-Karten
		for (Gamer g : gamers) {
			g.getInventory().clear();
			g.getInventory().addItem(Main.i.cardmgr.cards.get("vt_ja").getItemStack(true));
			g.getInventory().addItem(Main.i.cardmgr.cards.get("vt_nein").getItemStack(true));
		}

		// Initialisiere das Brett
		setBoardArrays();

		fac_plc_placed = 0;
		lib_plc_placed = 0;
		
		setItemFrames();
		
		// Vergeben der Rollen
		setRoles();
		giveRoleCards();
	}

	// Errechnet das facist_board-Array
	private void setBoardArrays() {
		facist_board = new Card[6];
		switch (gamers.size()) {
		case 5:
		case 6:
			facist_board[0] = Main.i.cardmgr.cards.get("brd_facempty");
			facist_board[1] = Main.i.cardmgr.cards.get("brd_facempty");
			facist_board[2] = Main.i.cardmgr.cards.get("brd_exam");
			facist_board[3] = Main.i.cardmgr.cards.get("brd_kill");
			facist_board[4] = Main.i.cardmgr.cards.get("brd_veto");
			facist_board[5] = Main.i.cardmgr.cards.get("brd_facend");
			break;
		case 7:
		case 8:
			facist_board[0] = Main.i.cardmgr.cards.get("brd_facempty");
			facist_board[1] = Main.i.cardmgr.cards.get("brd_invest");
			facist_board[2] = Main.i.cardmgr.cards.get("brd_presd");
			facist_board[3] = Main.i.cardmgr.cards.get("brd_kill");
			facist_board[4] = Main.i.cardmgr.cards.get("brd_veto");
			facist_board[5] = Main.i.cardmgr.cards.get("brd_facend");
			break;
		case 9:
		case 10:
			facist_board[0] = Main.i.cardmgr.cards.get("brd_invest");
			facist_board[1] = Main.i.cardmgr.cards.get("brd_invest");
			facist_board[2] = Main.i.cardmgr.cards.get("brd_presd");
			facist_board[3] = Main.i.cardmgr.cards.get("brd_kill");
			facist_board[4] = Main.i.cardmgr.cards.get("brd_veto");
			facist_board[5] = Main.i.cardmgr.cards.get("brd_facend");
			break;
		}

		liberal_board = new Card[5];
		liberal_board[0] = Main.i.cardmgr.cards.get("brd_libempty");
		liberal_board[1] = Main.i.cardmgr.cards.get("brd_libempty");
		liberal_board[2] = Main.i.cardmgr.cards.get("brd_libempty");
		liberal_board[3] = Main.i.cardmgr.cards.get("brd_libempty");
		liberal_board[4] = Main.i.cardmgr.cards.get("brd_libend");
	}

	// Setzt die entsprechende Items in die Frames
	private void setItemFrames() {
		for (int i = 0; i < facist_board.length; i++) {
			ItemFrame itmf = Main.i.mylib.getItemFrameInLocation(itemFrameLocations[i]);
			if (fac_plc_placed > i)
				itmf.setItem(Main.i.cardmgr.cards.get("plc_facist").getItemStack(false));
			else
				itmf.setItem(facist_board[i].getItemStack(false));
		}

		for (int i = 0; i < liberal_board.length; i++) {
			ItemFrame itmf = Main.i.mylib.getItemFrameInLocation(itemFrameLocations[i + facist_board.length]);
			if (lib_plc_placed > i)
				itmf.setItem(Main.i.cardmgr.cards.get("plc_liberal").getItemStack(false));
			else
				itmf.setItem(liberal_board[i].getItemStack(false));
		}
	}

	// Errechne und setze die Rollen der einzelnen Spieler
	private void setRoles() {
		ArrayList<Role> list = new ArrayList<>();
		
		list.add(Role.HITLER);
		
		switch (gamers.size()) {
		case 5:
		case 6:
			list.add(Role.FACIST);
			break;
		case 7:
		case 8:
			list.add(Role.FACIST);
			list.add(Role.FACIST);
			break;
		case 9:
		case 10:
			list.add(Role.FACIST);
			list.add(Role.FACIST);
			list.add(Role.FACIST);
			break;
		}
		
		while (list.size() < gamers.size())
			list.add(Role.LIBERAL);
		
		Collections.shuffle(list);
		
		for (int i = 0; i<gamers.size(); i++) {
			gamers.get(i).role = list.get(i);
		}
	}

	// Gebe den Spielern die entsprechende Karte
	private void giveRoleCards() {
		for (Gamer g : gamers) {
			switch (g.role) {
			case FACIST:
				g.getInventory().addItem(Main.i.cardmgr.cards.get("rl_facist").getItemStack(true));
				break;
			case HITLER:
				g.getInventory().addItem(Main.i.cardmgr.cards.get("rl_hitler").getItemStack(true));
				break;
			case LIBERAL:
				g.getInventory().addItem(Main.i.cardmgr.cards.get("rl_liberal").getItemStack(true));
				break;
			}
		}
	}
}
