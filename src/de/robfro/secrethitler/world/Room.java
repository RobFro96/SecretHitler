package de.robfro.secrethitler.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.game.Card;
import de.robfro.secrethitler.game.PoliciesDeck;
import de.robfro.secrethitler.game.Role;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.general.MyLib;
import de.robfro.secrethitler.general.Sidebar;
import mkremins.fanciful.FancyMessage;

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
			g.player.playSound(g.player.getLocation(), Sound.NOTE_PLING, 1f, pitch);
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

	
	
	
	
	
	// Gebe die Spieler Karten, trage diese in policies und plcsIS ein
	private void giveGamerCards(Gamer g, Card[] cards) {
		g.plcsIS = new ArrayList<>();
		g.policies = cards;
		String spcs = "";
		for (Card c : cards) {
			ItemStack is = c.getItemStack(true, spcs);
			spcs += " ";
			g.getInventory().addItem(is);
			g.plcsIS.add(is);
		}
	}

	// Entferne alle Karten den Spielers
	private void removerGamerCards(Gamer g) {
		for (ItemStack is : g.plcsIS) {
			g.getInventory().remove(is);
		}
		g.policies = null;
		g.plcsIS = null;
	}

	// Wenn der Präsident eine Karte wegwirft
	public void president_discard(Card c) {
		clearChat();
		FileConfiguration fc = Main.i.saves.config;

		// Entferne alle Policies aus den Inventar des Präsidenten
		Card[] cards = president.policies;
		removerGamerCards(president);

		// Lege die Karte auf den Discard-Stapel
		deck.dicardCard(c);

		// Gebe den Kanzler die überigen 2
		Card[] newCards = new Card[2];
		int i = 0;
		boolean weg = false;
		for (Card card : cards) {
			if (weg || card != c) {
				newCards[i] = card;
				i++;
			} else
				weg = true;
		}

		giveGamerCards(chancell, newCards);

		// Informiere die Spieler
		sendMessage(fc.getString("tr.game.chan_gets"), ChatColor.BLUE, true);

		// Informiere den Kanzler
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				chancell.sendMessage(formatMessage(fc.getString("tr.game.chan_discard"), ChatColor.BLUE, false));
				FancyMessage vetomsg = new FancyMessage("");
				if (veto_power) {
					vetomsg = vetomsg.then(fc.getString("tr.game.veto_power")).color(ChatColor.BLUE);
					vetomsg = vetomsg.then(fc.getString("tr.game.veto")).color(ChatColor.AQUA).command("/veto");
				}
				chancell.sendMessage(vetomsg);
			}
		}, 5);

		gamestate = 3;
	}

	// Wenn der Kanzler eine Karte wegwirft
	public void chanc_discard(Card remc) {
		clearChat();
		
		FileConfiguration fc = Main.i.saves.config;

		Card setc = chancell.policies[0];
		if (setc == remc)
			setc = chancell.policies[1];

		// Entferne alle Karten
		removerGamerCards(chancell);

		// Lege die entfernte auf den Discard-Stapel
		deck.dicardCard(remc);

		// Lege die andere auf das Board
		int fac_placed_before = fac_plc_placed;
		Main.i.gamemgr.placeCard(this, setc);

		sendMessage(fc.getString("tr.game.chan_places"), ChatColor.BLUE, true);

		gamestate = -1;

		if (checkGameEnds(null, true))
			return;

		Room r = this;
		
		if (fac_placed_before != fac_plc_placed) {
			// Es wurde eine faschistische Karte gelegt
			checkPresidentialPower();
		} else {
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					Main.i.vtmgr.setNewPresident(r);
				}
			}, 20 * 5);
		}
	}

	// Wenn der Kanzler auf den Veto-Button drückt
	public void chanc_veto() {
		clearChat();
		FileConfiguration c = Main.i.saves.config;

		sendMessage(c.getString("tr.game.chanc_vetos"), ChatColor.BLUE, true);

		president.sendMessage(formatMessage(c.getString("tr.game.presd_veto"), ChatColor.BLUE, false));

		FancyMessage msg = new FancyMessage(c.getString("tr.game.veto_accept")).color(ChatColor.AQUA)
				.command("/veto 1");
		msg = msg.then("   ");
		msg = msg.then(c.getString("tr.game.veto_deny")).color(ChatColor.AQUA).command("/veto 0");

		president.sendMessage(msg);

		gamestate = 4;
	}

	// Die Antwort des Präsidenten
	public void presd_veto_accept() {
		clearChat();
		sendMessage(Main.i.saves.config.getString("tr.game.accept"), ChatColor.BLUE, true);
		election_tracker++;
		Main.i.rooms.setElectionTracker(this);

		gamestate = -1;

		Room r = this;
		
		// Warte 5 sec
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				if (election_tracker == 3) {
					// Der ElectionTracker ist voll, Policy aufdecken!
					chancell = null;
					election_tracker_full();
				} else {
					Main.i.vtmgr.setNewPresident(r);
				}
			}
		}, 20 * 5);
	}
	public void presd_veto_deny() {
		clearChat();
		sendMessage(Main.i.saves.config.getString("tr.game.deny"), ChatColor.BLUE, true);
		gamestate = 3;
	}

	// Wenn eine faschitische Karte gelegt wird, wird die presidential power
	// überprüft
	private void checkPresidentialPower() {
		Room r = this;
		FileConfiguration c = Main.i.saves.config;
		Card card = facist_board[fac_plc_placed - 1];
		if (card == Main.i.cardmgr.cards.get("brd_invest")) {
			// Die Parteiangehörigkeit eines Spielers erfragen
			sendMessage(c.getString("tr.game.power.invest.info"), ChatColor.BLUE, true);
			president.sendAskForInvestigation(this);
			gamestate = 5;
		} else if (card == Main.i.cardmgr.cards.get("brd_presd")) {
			// Den nächsten Präsidenten wählen
			sendMessage(c.getString("tr.game.power.presd.info"), ChatColor.BLUE, true);
			president.sendAskForNextPresd(this);
			gamestate = 6;
		} else if (card == Main.i.cardmgr.cards.get("brd_exam")) {
			// Die nächsten drei Poltiken betrachten
			sendMessage(c.getString("tr.game.power.exam.info"), ChatColor.BLUE, true);
			String cards = "";
			for (Card plc : deck.showThreeCards()) {
				if (plc == Main.i.cardmgr.cards.get("plc_liberal"))
					cards += c.getString("tr.game.power.exam.lib");
				else
					cards += c.getString("tr.game.power.exam.fac");
			}

			president.sendMessage(
					formatMessage(c.getString("tr.game.power.exam.result") + cards, ChatColor.BLUE, false));
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					Main.i.vtmgr.setNewPresident(r);
				}
			}, 20 * 5);
		} else if (card == Main.i.cardmgr.cards.get("brd_kill") || card == Main.i.cardmgr.cards.get("brd_veto")) {
			// Töte einen Spieler
			if (card == Main.i.cardmgr.cards.get("brd_veto"))
				sendMessage(c.getString("tr.game.power.veto.info"), ChatColor.BLUE, true);
			sendMessage(c.getString("tr.game.power.kill.info"), ChatColor.BLUE, true);
			president.sendAskToExecute(this);
			gamestate = 7;
		} else {
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					Main.i.vtmgr.setNewPresident(r);
				}
			}, 20 * 5);
		}

	}

	// Investigate
	public void investigate(Gamer g) {
		clearChat();
		gamestate = -1;
		FileConfiguration c = Main.i.saves.config;
		sendMessage(c.getString("tr.game.power.invest.invest").replaceAll("#name", g.longName), ChatColor.BLUE, true);

		String party = c.getString("tr.game.power.invest.lib");
		if (g.role == Role.HITLER || g.role == Role.FACIST)
			party = c.getString("tr.game.power.invest.fac");

		president.sendMessage(formatMessage(
				c.getString("tr.game.power.invest.result").replaceAll("#name", g.name).replaceAll("#party", party),
				ChatColor.BLUE, false));

		Room r = this;
		
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				Main.i.vtmgr.setNewPresident(r);
			}
		}, 20 * 5);
	}

	// Special Election
	public void specialElection(Gamer g) {
		clearChat();
		gamestate = -1;
		// Wenn der nächste gewählt wurde, geht es normal weiter
		if (g == Main.i.gamemgr.nextGamer(this, president)) {
			Main.i.vtmgr.setNewPresident(this);
			return;
		}

		// Manipuliere alles
		special_election = true;
		last_chancell = chancell;
		chancell = null;
		last_president = president;
		president = g;
		sendMessage(Main.i.saves.config.getString("tr.game.pres_was_elected"), ChatColor.BLUE, true);

		Main.i.rooms.updateSidebar(this);
		
		president.sendChancellElectionMessage(this);
	}

	// Hinrichtung
	public void executeGamer(Gamer g) {
		clearChat();
		gamestate = -1;
		FileConfiguration c = Main.i.saves.config;
		sendMessage(c.getString("tr.game.power.kill.kill").replaceAll("#name", g.longName), ChatColor.BLUE, true);
		gamers.remove(g);
		g.state = 0;
		Main.i.rooms.updateSidebar(this);

		if (checkGameEnds(g, true))
			return;

		Room r = this;
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				Main.i.vtmgr.setNewPresident(r);
			}
		}, 20 * 5);


	}

	// Überprüfe alle Möglichkeiten des Endes des Spieles
	public boolean checkGameEnds(Gamer killed, boolean ignoreHitler) {
		// Wenn Hitler stirbt wird hier nicht beachtet!
		FileConfiguration c = Main.i.saves.config;
		int win = -1;
		String cause = "";
		if (lib_plc_placed >= 5) {
			win = 0;
			cause = c.getString("tr.game.end.libplcs");
		}
		if (killed != null) {
			if (killed.role == Role.HITLER) {
				win = 0;
				cause = c.getString("tr.game.end.killhitler");
			}
		}
		if (fac_plc_placed >= 6) {
			win = 1;
			cause = c.getString("tr.game.end.facplcs");
		}
		if (chancell != null && !ignoreHitler) {
			if (chancell.role == Role.HITLER && fac_plc_placed >= 3) {
				win = 1;
				cause = c.getString("tr.game.end.hitlerelected");
			}
		}

		if (win == -1)
			return false;

		if (win == 0) {
			sendMessage(c.getString("tr.game.end.libwin") + cause, ChatColor.BLUE, true);
		} else {
			sendMessage(c.getString("tr.game.end.facwin") + cause, ChatColor.BLUE, true);
		}

		Main.i.getLogger().info("Game started in " + name + ".");

		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				openSecretRoles();
			}
		}, 20 * 5);


		return true;
	}

	public void openSecretRoles() {
		FileConfiguration c = Main.i.saves.config;
		sendMessage(c.getString("tr.game.end.roles"), ChatColor.BLUE, true);
		String msg = "";
		int n = 0;

		// Sende allen Spieler das Wahlergebnis, Zeilenumbruch nach 2 Spielern
		for (Gamer g : all_gamers) {
			msg += ChatColor.RESET + g.longName + ": ";
			if (g.role == Role.FACIST)
				msg += c.getString("tr.pregame.rl_facist");
			else if (g.role == Role.HITLER)
				msg += c.getString("tr.pregame.rl_hitler");
			else
				msg += c.getString("tr.pregame.rl_liberal");

			msg += "   ";
			n++;
			if (n == 2) {
				sendMessage(msg, ChatColor.WHITE);
				msg = "";
				n = 0;
			}
		}
		if (msg != "")
			sendMessage(msg, ChatColor.WHITE);

		Room r = this;
		// Beende das Spiel
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				Main.i.rooms.resetRoom(r);
			}
		}, 20 * 5);
	}

	
}
