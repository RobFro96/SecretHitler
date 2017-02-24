package de.robfro.secrethitler.world;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.game.Card;
import de.robfro.secrethitler.game.PoliciesDeck;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.general.MyLib;
import de.robfro.secrethitler.general.Sidebar;

public class Room {

	// Konstaten: Anzahl der ItemFrames, ElectionTrackerBlöcke
	public static final int ItemFrameCount = 11;
	public static final int ElectionTrackerCount = 3;

	// Eigenschaften eines Raumes
	public String name;
	public Location[] itemFrameLocations;
	public Location[] electionTracker;
	public Location spawn, signloc, hmloc;
	public Sign sign;
	public String et_material1, et_material2;
	public boolean all_right;

	// Spielerbezogenene Eigenschaften
	public ArrayList<Gamer> gamers;
	//public ArrayList<Gamer> killed;
	public ArrayList<Gamer> all_gamers;
	public boolean playing = false;
	public int waiting_time;

	// Ingame
	public Card[] facist_board;
	public Card[] liberal_board;
	public PoliciesDeck deck;
	public int fac_plc_placed, lib_plc_placed;
	public int election_tracker;
	public int gamestate; // 0..Nominate Chancellor
							// 1..Wahl vergeben
							// 2..Präsident eliminiert
							// 3..Kanzler eliminiert, veto
							// 4..Präsident bestätigt veto
							// 5..welchen Spieler investen?
							// 6..welchen Spieler zum Presidentn wählen?
							// 7..welchen Spieler exicuten?

	// Roles
	public Gamer president, chancell;
	public Gamer last_president, last_chancell;
	public boolean special_election;
	public boolean veto_power;
	
	public Sidebar sbar;
	
	// Für die Stats:
	public long lastUpdate;

	// Lade einen Raum aus der YAML
	public Room(FileConfiguration c, String key) {
		gamers = new ArrayList<>();
		all_gamers = new ArrayList<>();
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
		
		hmloc = MyLib.ParseLocation(c.getString(key + ".hm"));

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

		et_material1 = c.getString(key + ".et_material1", "35:15");
		et_material2 = c.getString(key + ".et_material2", "35:14");

		Main.i.rooms.resetRoom(this);
		
		sbar = new Sidebar(10, name, ChatColor.BOLD + name);
	}

	// Erstelle einen neuen Raum
	public Room(String name) {
		this.name = name;
		itemFrameLocations = new Location[ItemFrameCount];
		electionTracker = new Location[ElectionTrackerCount];
		spawn = null;
		et_material1 = "35:15";
		et_material2 = "35:14";

		Main.i.rooms.resetRoom(this);
		
		sbar = new Sidebar(10, name, ChatColor.BOLD + name);
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

		c.set(name + ".et_material1", et_material1);
		c.set(name + ".et_material2", et_material2);
		c.set(name + ".hm", MyLib.LocationToString(hmloc));
	}

	// Sendet eine Nachricht an alle Spieler in diesem Raum
	public void sendMessage(String msg, ChatColor color) {
		sendMessage(msg, color, false);
	}

	public void sendMessage(String msg, ChatColor color, boolean bold) {
		msg = formatMessage(msg, color, bold);
		for (Gamer g : all_gamers)
			g.sendMessage(msg);
	}

	public void clearChat() {
		for (Gamer g : all_gamers)
			for (int i=0; i<3; i++)
				g.sendMessage(" ");
	}
	
	public String formatMessage(String msg, ChatColor color, boolean bold) {
		FileConfiguration c = Main.i.saves.config;
		String clr = color.toString();
		if (bold)
			clr += ChatColor.BOLD.toString();
		msg = clr + msg;
		if (president != null)
			msg = msg.replaceAll("#prnm", c.getString("config.game.presd_color") + president.longName + clr);
		if (chancell != null)
			msg = msg.replaceAll("#chnm", c.getString("config.game.chanc_color") + chancell.longName + clr);
		return msg;
	}

	// Nach einer Sekunde wird wird die Zeit verringert
	public void onTimerOneSecond() {
		if (playing)
			return;
		FileConfiguration c = Main.i.saves.config;
		if (all_gamers.size() < c.getInt("config.wait.min_player")) {
			waiting_time = c.getInt("config.wait.wait_at_min");
		} else {
			waiting_time--;

			if (waiting_time <= 0) {
				sendTone(2f);
				Main.i.gamemgr.start(this);
			}

			if (waiting_time <= 5)
				sendTone(1f);

		}
		setLevel(waiting_time, c.getInt("config.wait.wait_at_min"));
	}

	// Sende an alle Spieler einen Ton
	private void sendTone(float pitch) {
		for (Gamer g : all_gamers)
			g.player.playSound(g.player.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), 1f, pitch);
	}

	// Update die Levelanzeige aller Spieler
	public void setLevel(int lvl, int max) {
		for (Gamer g : all_gamers) {
			g.player.setLevel(lvl);
			if (lvl <= max)
				g.player.setExp(((float) lvl) / max);
			else
				g.player.setExp(1f);
		}
	}

	// Gibt die maximale Wartezeit bei der Spieleranzahl zurück
	public int maxWaitTime(int cnt) {
		FileConfiguration c = Main.i.saves.config;
		int min = c.getInt("config.wait.min_player");
		if (cnt < min)
			return c.getInt("config.wait.wait_at_min");
		return c.getInt("config.wait.wait_at_min") - (cnt - min) * c.getInt("config.wait.less_per_player");
	}

	public void setHead(String name, boolean rocket) {
		if (hmloc == null)
			return;
		ArmorStand as = Main.i.mylib.getArmorStandInLocation(hmloc);
		if (as == null)
			return;
		ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (byte)3);
		SkullMeta meta = (SkullMeta)is.getItemMeta();
		meta.setOwner(name);
		is.setItemMeta(meta);
		as.setHelmet(is);
		
		if (rocket) {
			Location loc = as.getLocation();
			loc.add(0, 2f, 0);
			Firework fw = (Firework) as.getWorld().spawnEntity(loc, EntityType.FIREWORK);
			FireworkMeta fm = fw.getFireworkMeta();
				 
			FireworkEffect effect = FireworkEffect.builder().withColor(Color.RED).with(Type.BALL_LARGE).build();
			fm.addEffect(effect);
			fm.setPower(0);
			fw.setFireworkMeta(fm);
		}
	}
	
	
	
}
