package de.robfro.secrethitler.game;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.world.Room;

public class PowerMgr {

	// Wenn eine faschitische Karte gelegt wird, wird die presidential power
	// überprüft
	public void checkPresidentialPower(Room r) {
		FileConfiguration c = Main.i.saves.config;
		Card card = r.facist_board[r.fac_plc_placed - 1];
		if (card == Main.i.cardmgr.cards.get("brd_invest")) {
			// Die Parteiangehörigkeit eines Spielers erfragen
			r.sendMessage(c.getString("tr.game.power.invest.info"), ChatColor.BLUE, true);
			r.president.sendAskForInvestigation(r);
			r.gamestate = 5;
		} else if (card == Main.i.cardmgr.cards.get("brd_presd")) {
			// Den nächsten Präsidenten wählen
			r.sendMessage(c.getString("tr.game.power.presd.info"), ChatColor.BLUE, true);
			r.president.sendAskForNextPresd(r);
			r.gamestate = 6;
		} else if (card == Main.i.cardmgr.cards.get("brd_exam")) {
			// Die nächsten drei Poltiken betrachten
			r.sendMessage(c.getString("tr.game.power.exam.info"), ChatColor.BLUE, true);
			String cards = "";
			for (Card plc : r.deck.showThreeCards()) {
				if (plc == Main.i.cardmgr.cards.get("plc_liberal"))
					cards += c.getString("tr.game.power.exam.lib");
				else
					cards += c.getString("tr.game.power.exam.fac");
			}

			r.president.sendMessage(
					r.formatMessage(c.getString("tr.game.power.exam.result") + cards, ChatColor.BLUE, false));
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					Main.i.vtmgr.setNewPresident(r);
				}
			}, 20 * 5);
		} else if (card == Main.i.cardmgr.cards.get("brd_kill") || card == Main.i.cardmgr.cards.get("brd_veto")) {
			// Töte einen Spieler
			if (card == Main.i.cardmgr.cards.get("brd_veto"))
				r.sendMessage(c.getString("tr.game.power.veto.info"), ChatColor.BLUE, true);
			r.sendMessage(c.getString("tr.game.power.kill.info"), ChatColor.BLUE, true);
			r.president.sendAskToExecute(r);
			r.gamestate = 7;
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
	public void investigate(Room r, Gamer g) {
		r.clearChat();
		r.gamestate = -1;
		FileConfiguration c = Main.i.saves.config;
		r.sendMessage(c.getString("tr.game.power.invest.invest").replaceAll("#name", g.longName), ChatColor.BLUE, true);

		String party = c.getString("tr.game.power.invest.lib");
		if (g.role == Role.HITLER || g.role == Role.FACIST)
			party = c.getString("tr.game.power.invest.fac");

		r.president.sendMessage(r.formatMessage(
				c.getString("tr.game.power.invest.result").replaceAll("#name", g.name).replaceAll("#party", party),
				ChatColor.BLUE, false));

		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				Main.i.vtmgr.setNewPresident(r);
			}
		}, 20 * 5);
	}

	// Special Election
	public void specialElection(Room r, Gamer g) {
		r.clearChat();
		r.gamestate = -1;
		// Wenn der nächste gewählt wurde, geht es normal weiter
		if (g == Main.i.gamemgr.nextGamer(r, r.president)) {
			Main.i.vtmgr.setNewPresident(r);
			return;
		}

		// Manipuliere alles
		r.special_election = true;
		r.last_chancell = r.chancell;
		r.chancell = null;
		r.last_president = r.president;
		r.president = g;
		r.sendMessage(Main.i.saves.config.getString("tr.game.pres_was_elected"), ChatColor.BLUE, true);

		Main.i.rooms.updateSidebar(r);

		r.president.sendChancellElectionMessage(r);
	}

	// Hinrichtung
	public void executeGamer(Room r, Gamer g) {
		r.clearChat();
		r.gamestate = -1;
		FileConfiguration c = Main.i.saves.config;
		r.sendMessage(c.getString("tr.game.power.kill.kill").replaceAll("#name", g.longName), ChatColor.BLUE, true);
		r.gamers.remove(g);
		g.state = 0;
		Main.i.rooms.updateSidebar(r);

		if (Main.i.gamemgr.checkGameEnds(r, g, true))
			return;

		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				Main.i.vtmgr.setNewPresident(r);
			}
		}, 20 * 5);


	}


}
