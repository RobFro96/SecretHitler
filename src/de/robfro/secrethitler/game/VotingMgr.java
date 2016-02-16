package de.robfro.secrethitler.game;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.world.Room;

public class VotingMgr {

	// Bestimmt den neuen Presidenten
		public void setNewPresident(Room r) {
			// Übertrage den Kanzler
			if (r.chancell != null) {
				r.last_chancell = r.chancell;
				r.chancell = null;
			}

			if (r.special_election) {
				// Wenn es zu eine SpecialElection kam, muss der nächste nach den
				// President zuvor genommen werden
				r.special_election = false;
				Gamer next = Main.i.gamemgr.nextGamer(r, r.last_president);
				r.last_president = r.president;
				r.president = next;
			} else if (r.president == null) {
				// Wenn das Spiel neu gestartet wurde, wird der President gelost
				r.president = r.gamers.get(new Random(System.nanoTime()).nextInt(r.gamers.size()));
			} else {
				Gamer next = Main.i.gamemgr.nextGamer(r, r.president);
				r.last_president = r.president;
				r.president = next;
			}

			r.sendMessage(Main.i.saves.config.getString("tr.game.pres_was_elected"), ChatColor.BLUE, true);

			r.president.sendChancellElectionMessage(r);
			
			Main.i.rooms.updateSidebar(r);
		}

		// Gibt als Zahl den Grund der Nicht-nominierung zurück
		public int canBeNominated(Room r, Gamer g) {
			// Spieler ist Präsident
			if (g == r.president)
				return 1;
			// Spieler ist letzter Kanzler
			if (g == r.last_chancell)
				return 2;
			// Mehr als 5 Spieler: Spieler ist letzter Präsident
			if (g == r.last_president && r.gamers.size() > 5)
				return 3;
			return 0;
		}

		// Wenn der Pres. einen Kanzler nominiert werden alle zur Wahl aufgerufen
		public void nominateAsChancellor(Room r, Gamer nominee) {
			r.clearChat();
			
			r.chancell = nominee;
			FileConfiguration c = Main.i.saves.config;
			r.sendMessage(c.getString("tr.game.vote"), ChatColor.BLUE, true);
			r.sendMessage(c.getString("tr.game.votehelp"), ChatColor.BLUE);
			r.gamestate = 1;

			// Lösche alle Wahlabgaben
			for (Gamer g : r.gamers)
				g.vote = -1;
			
			Main.i.rooms.updateSidebar(r);
		}

		// Wenn ein Spieler wählt, wird überprüft, ob alle abgestimmt haben.
		public void updateVoting(Room r) {
			for (Gamer g : r.gamers) {
				if (g.vote == -1)
					return;
			}
			FileConfiguration c = Main.i.saves.config;
			
			r.clearChat();
			
			// Alle Spieler haben ihre Stimme abgegeben
			r.gamestate = -1;
			r.sendMessage(c.getString("tr.game.result"), ChatColor.BLUE, true);
			String msg = "";
			int n = 0;
			int jas = 0;
			// Sende allen Spieler das Wahlergebnis, Zeilenumbruch nach 2 Spielern
			for (Gamer g : r.gamers) {
				msg += ChatColor.RESET + g.longName + ": ";
				if (g.vote == 0)
					msg += c.getString("tr.game.result_nein");
				else {
					msg += c.getString("tr.game.result_ja");
					jas++;
				}
				msg += "   ";
				n++;
				if (n == 2) {
					r.sendMessage(msg, ChatColor.WHITE);
					msg = "";
					n = 0;
				}
			}
			if (msg != "")
				r.sendMessage(msg, ChatColor.WHITE);

			if (jas > r.gamers.size() / 2) {
				// Wahl ist angenommen
				r.sendMessage(c.getString("tr.game.vote_sucessf"), ChatColor.BLUE);
				voting_sucessf(r);
			} else {
				// Wahl wurde abgelehnt
				r.sendMessage(c.getString("tr.game.vote_failed"), ChatColor.BLUE);
				voting_failed(r);
			}
		}

		// Wenn die Wahl abgelehnt wurde
		public void voting_failed(Room r) {
			r.election_tracker++;
			Main.i.rooms.setElectionTracker(r);
			r.chancell = null;
			Main.i.rooms.updateSidebar(r);

			// Warte 5 sec
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					if (r.election_tracker == 3) {
						// Der ElectionTracker ist voll, Policy aufdecken!
						election_tracker_full(r);
					} else {
						setNewPresident(r);
					}
				}
			}, 20 * 5);
		}

		// Wenn die Wahl angenommen wird
		public void voting_sucessf(Room r) {
			// Check: Spiel zu ende durch die Wahl von Hitler
			if (r.checkGameEnds(null, false))
				return;

			Main.i.rooms.updateSidebar(r);
			
			// Setze den ET zurück
			r.election_tracker = 0;
			Main.i.rooms.setElectionTracker(r);

			r.sendMessage(Main.i.saves.config.getString("tr.game.presd_draws"), ChatColor.BLUE, true);
			Card[] cards = r.deck.getThreeCards(r);
			r.giveGamerCards(r.president, cards);
			r.gamestate = 2;
			r.president.sendMessage(
					r.formatMessage(Main.i.saves.config.getString("tr.game.presd_discard"), ChatColor.BLUE, false));
		}

		// Wenn der ElectionTracker voll ist wird ein Artikel gelegt
		public void election_tracker_full(Room r) {
			r.sendMessage(Main.i.saves.config.getString("tr.game.et_full"), ChatColor.BLUE, true);
			Card c = r.deck.getOneCard(r, true);

			Main.i.gamemgr.placeCard(r, c);

			r.election_tracker = 0;
			r.last_chancell = null;

			Main.i.rooms.setElectionTracker(r);

			if (r.checkGameEnds(null, true))
				return;

			// Beginne eine neue Runde!
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					Main.i.vtmgr.setNewPresident(r);
				}
			}, 5 * 20);
		}

}
