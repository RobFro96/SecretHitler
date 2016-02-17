package de.robfro.secrethitler.game;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.world.Room;
import mkremins.fanciful.FancyMessage;

public class PolicyMgr {

	// Gebe die Spieler Karten, trage diese in policies und plcsIS ein
	public void giveGamerCards(Gamer g, Card[] cards) {
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
	public void removerGamerCards(Gamer g) {
		for (ItemStack is : g.plcsIS) {
			g.getInventory().remove(is);
		}
		g.policies = null;
		g.plcsIS = null;
	}

	// Wenn der Präsident eine Karte wegwirft
	public void president_discard(Room r, Card c) {
		r.clearChat();
		FileConfiguration fc = Main.i.saves.config;

		// Entferne alle Policies aus den Inventar des Präsidenten
		Card[] cards = r.president.policies;
		removerGamerCards(r.president);

		// Lege die Karte auf den Discard-Stapel
		r.deck.dicardCard(c);

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

		giveGamerCards(r.chancell, newCards);

		// Informiere die Spieler
		r.sendMessage(fc.getString("tr.game.chan_gets"), ChatColor.BLUE, true);

		// Informiere den Kanzler
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				r.chancell.sendMessage(r.formatMessage(fc.getString("tr.game.chan_discard"), ChatColor.BLUE, false));
				FancyMessage vetomsg = new FancyMessage("");
				if (r.veto_power) {
					vetomsg = vetomsg.then(fc.getString("tr.game.veto_power")).color(ChatColor.BLUE);
					vetomsg = vetomsg.then(fc.getString("tr.game.veto")).color(ChatColor.AQUA).command("/veto");
				}
				r.chancell.sendMessage(vetomsg);
			}
		}, 5);

		r.gamestate = 3;
	}

	// Wenn der Kanzler eine Karte wegwirft
	public void chanc_discard(Room r, Card remc) {
		r.clearChat();

		FileConfiguration fc = Main.i.saves.config;

		Card setc = r.chancell.policies[0];
		if (setc == remc)
			setc = r.chancell.policies[1];

		// Entferne alle Karten
		removerGamerCards(r.chancell);

		// Lege die entfernte auf den Discard-Stapel
		r.deck.dicardCard(remc);

		// Lege die andere auf das Board
		int fac_placed_before = r.fac_plc_placed;
		Main.i.gamemgr.placeCard(r, setc);

		r.sendMessage(fc.getString("tr.game.chan_places"), ChatColor.BLUE, true);

		r.gamestate = -1;

		if (Main.i.gamemgr.checkGameEnds(r, null, true))
			return;


		if (fac_placed_before != r.fac_plc_placed) {
			// Es wurde eine faschistische Karte gelegt
			Main.i.pwrmgr.checkPresidentialPower(r);
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
	public void chanc_veto(Room r) {
		r.clearChat();
		FileConfiguration c = Main.i.saves.config;

		r.sendMessage(c.getString("tr.game.chanc_vetos"), ChatColor.BLUE, true);

		r.president.sendMessage(r.formatMessage(c.getString("tr.game.presd_veto"), ChatColor.BLUE, false));

		FancyMessage msg = new FancyMessage(c.getString("tr.game.veto_accept")).color(ChatColor.AQUA)
				.command("/veto 1");
		msg = msg.then("   ");
		msg = msg.then(c.getString("tr.game.veto_deny")).color(ChatColor.AQUA).command("/veto 0");

		r.president.sendMessage(msg);

		r.gamestate = 4;
	}

	// Die Antwort des Präsidenten
	public void presd_veto_accept(Room r) {
		r.deck.dicardCard(r.chancell.policies[0]);
		r.deck.dicardCard(r.chancell.policies[1]);

		// Entferne alle Karten
		removerGamerCards(r.chancell);

		r.clearChat();
		r.sendMessage(Main.i.saves.config.getString("tr.game.accept"), ChatColor.BLUE, true);
		r.election_tracker++;
		Main.i.rooms.setElectionTracker(r);

		r.gamestate = -1;

		// Warte 5 sec
		Main.i.delayedTask(new Runnable() {
			@Override
			public void run() {
				if (r.election_tracker == 3) {
					// Der ElectionTracker ist voll, Policy aufdecken!
					r.chancell = null;
					Main.i.vtmgr.election_tracker_full(r);
				} else {
					Main.i.vtmgr.setNewPresident(r);
				}
			}
		}, 20 * 5);
	}
	public void presd_veto_deny(Room r) {
		r.clearChat();
		r.sendMessage(Main.i.saves.config.getString("tr.game.deny"), ChatColor.BLUE, true);
		r.gamestate = 3;
	}


}
