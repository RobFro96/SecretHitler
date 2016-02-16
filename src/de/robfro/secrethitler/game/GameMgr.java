package de.robfro.secrethitler.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.world.Room;

public class GameMgr {

	// Starte das Spiel
	public void start(Room r) {
		// Sende der Konsole und allen Mitspielern die Nachricht, dass das Spiel
		// startet.
		FileConfiguration c = Main.i.saves.config;
		Main.i.getLogger().info("Game started in " + r.name + ".");
		r.sendMessage(c.getString("tr.pregame.started"), ChatColor.BLUE, true);

		r.setLevel(0, 0);
		r.playing = true;
		Main.i.rooms.updateSign(r);

		// Lösche alle Items, gebe die Vote-Karten
		for (Gamer g : r.all_gamers) {
			g.getInventory().clear();
			g.getInventory().addItem(Main.i.cardmgr.cards.get("vt_ja").getItemStack(true));
			g.getInventory().addItem(Main.i.cardmgr.cards.get("vt_nein").getItemStack(true));
		}

		// Initialisiere das Brett
		setBoardArrays(r, r.all_gamers.size());

		r.deck = new PoliciesDeck(c.getInt("config.game.liberal_plcs"), c.getInt("config.game.facist_plcs"));
		r.fac_plc_placed = 0;
		r.lib_plc_placed = 0;
		r.election_tracker = 0;
		r.president = null;
		r.last_president = null;
		r.chancell = null;
		r.last_chancell = null;
		r.veto_power = false;
		r.special_election = false;

		r.gamestate = -1;

		Main.i.rooms.setItemFrames(r);
		Main.i.rooms.setElectionTracker(r);

		// Vergeben der Rollen
		setRoles(r);
		giveRoleCards(r);

		r.clearChat();

		// Sende die Nachricht an alle Spieler
		for (Gamer g : r.gamers)
			g.sendRoleMessage(r.gamers);

		// Nach 5s wird der erste President bestimmt.
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				Main.i.vtmgr.setNewPresident(r);
			}
		}, 20 * 5);

	}

	// Errechnet das facist_board-Array
	public void setBoardArrays(Room r, int size) {
		r.facist_board = new Card[6];
		switch (size) {
		case 5:
		case 6:
			r.facist_board[0] = Main.i.cardmgr.cards.get("brd_facempty");
			// facist_board[0] = Main.i.cardmgr.cards.get("brd_kill");
			r.facist_board[1] = Main.i.cardmgr.cards.get("brd_facempty");
			r.facist_board[2] = Main.i.cardmgr.cards.get("brd_exam");
			r.facist_board[3] = Main.i.cardmgr.cards.get("brd_kill");
			r.facist_board[4] = Main.i.cardmgr.cards.get("brd_veto");
			r.facist_board[5] = Main.i.cardmgr.cards.get("brd_facend");
			break;
		case 7:
		case 8:
			r.facist_board[0] = Main.i.cardmgr.cards.get("brd_facempty");
			r.facist_board[1] = Main.i.cardmgr.cards.get("brd_invest");
			r.facist_board[2] = Main.i.cardmgr.cards.get("brd_presd");
			r.facist_board[3] = Main.i.cardmgr.cards.get("brd_kill");
			r.facist_board[4] = Main.i.cardmgr.cards.get("brd_veto");
			r.facist_board[5] = Main.i.cardmgr.cards.get("brd_facend");
			break;
		case 9:
		case 10:
			r.facist_board[0] = Main.i.cardmgr.cards.get("brd_invest");
			r.facist_board[1] = Main.i.cardmgr.cards.get("brd_invest");
			r.facist_board[2] = Main.i.cardmgr.cards.get("brd_presd");
			r.facist_board[3] = Main.i.cardmgr.cards.get("brd_kill");
			r.facist_board[4] = Main.i.cardmgr.cards.get("brd_veto");
			r.facist_board[5] = Main.i.cardmgr.cards.get("brd_facend");
			break;
		}

		r.liberal_board = new Card[5];
		r.liberal_board[0] = Main.i.cardmgr.cards.get("brd_libempty");
		r.liberal_board[1] = Main.i.cardmgr.cards.get("brd_libempty");
		r.liberal_board[2] = Main.i.cardmgr.cards.get("brd_libempty");
		r.liberal_board[3] = Main.i.cardmgr.cards.get("brd_libempty");
		r.liberal_board[4] = Main.i.cardmgr.cards.get("brd_libend");
	}

	// Errechne und setze die Rollen der einzelnen Spieler
	public void setRoles(Room r) {
		ArrayList<Role> list = new ArrayList<>();

		list.add(Role.HITLER);

		switch (r.gamers.size()) {
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

		while (list.size() < r.gamers.size())
			list.add(Role.LIBERAL);

		Collections.shuffle(list, new Random(System.nanoTime()));

		for (int i = 0; i < r.gamers.size(); i++) {
			r.gamers.get(i).role = list.get(i);
		}
	}

	// Gebe den Spielern die entsprechende Karte
	public void giveRoleCards(Room r) {
		for (Gamer g : r.gamers) {
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

	// Gibt den nächsten Spieler am Tisch aus
	public Gamer nextGamer(Room r, Gamer prev) {
		boolean next = false;
		for (Gamer g : r.gamers) {
			if (g == prev)
				next = true;
			else if (next)
				return g;
		}
		return r.gamers.get(0);
	}

	// Lege eine Karte auf das Brett, checkEndGame muss noch gemacht werden!
	public void placeCard(Room r, Card c) {
		if (c == Main.i.cardmgr.cards.get("plc_liberal"))
			r.lib_plc_placed++;
		else {
			r.fac_plc_placed++;
			if (r.fac_plc_placed == 3)
				r.sendMessage(Main.i.saves.config.getString("tr.game.warn_chancell"), ChatColor.BLUE);
		}
		Main.i.rooms.setItemFrames(r);
	}


}
