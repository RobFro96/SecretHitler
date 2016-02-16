package de.robfro.secrethitler.world;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.game.Card;
import de.robfro.secrethitler.game.CardType;
import de.robfro.secrethitler.gamer.Gamer;

public class RoomMgr {

	public HashMap<String, Room> rooms;

	public RoomMgr() {
		rooms = new HashMap<>();
		FileConfiguration c = Main.i.saves.rooms;

		for (String key : c.getKeys(false)) {
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
			updateSign(r);
		}
	}

	// Wenn ein Spieler auf ein Schild klickt
	public void onClickedSign(PlayerInteractEvent e) {
		Gamer g = Main.i.mylib.getGamerFromSender(e.getPlayer());
		if (g == null)
			return;
		// Check: In der Lobby
		if (g.state != 0) {
			Main.i.mylib.sendError(g, "ingame");
			return;
		}

		Room room = null;
		for (Room r : rooms.values()) {
			if (r.sign.getLocation().distance(e.getClickedBlock().getLocation()) < .5f) {
				room = r;
				break;
			}
		}
		// CHECK: Raum wurde gefunden
		if (room == null)
			return;

		joinRoom(room, g);
	}

	// Spieler nominiert einen Spieler
	public boolean onCommandNOMINATE(CommandSender sender, Command command, String label, String[] args) {
		// Hole den Spieler
		Gamer g = Main.i.mylib.getGamerFromSender(sender);
		if (g == null)
			return true;

		// Check: Ingame!
		if (g.state != 1)
			return true;

		// Hole den Raum
		Room r = g.joinedRoom;
		if (r == null)
			return true;

		// Check: Argument vorhanden
		if (args.length != 1)
			return true;

		// Hole Nominierter
		Gamer nominee = Main.i.gamermgr.getGamer(args[0]);
		if (nominee == null)
			return true;


		if (r.gamestate == 0) {
			// President nominiert einen Kanzler
			// Check: President
			if (g != r.president)
				return true;

			// Check: Nominierter kann nominiert werden
			if (Main.i.vtmgr.canBeNominated(r, nominee) != 0)
				return true;

			Main.i.vtmgr.nominateAsChancellor(r, nominee);
		} else if (r.gamestate == 5) {
			// Who to invest
			// Check: President
			if (g != r.president)
				return true;

			if (nominee == g || nominee.investigated)
				return true;

			r.investigate(nominee);
		} else if (r.gamestate == 6) {
			// Wer nächster President?
			// Check: President
			if (g != r.president)
				return true;

			if (nominee == g)
				return true;

			r.specialElection(nominee);
		} else if (r.gamestate == 7) {
			// Wer soll hingerichtet werden?
			// Check: President
			if (g != r.president)
				return true;

			if (nominee == g)
				return true;

			r.executeGamer(nominee);
		}

		return true;
	}

	public boolean onPlayerDropItem(PlayerDropItemEvent e) {
		Gamer g = Main.i.mylib.getGamerFromName(e.getPlayer().getName());
		if (g == null)
			return false;
		if (g.state != 1)
			return false;
		if (g.joinedRoom == null)
			return false;
		if (g.joinedRoom.gamestate == 2) {
			// Der Präsident wirft eine Karte weg
			if (g.joinedRoom.president != g)
				return false;
			Card c = Main.i.mylib.getCardFromItemStack(e.getItemDrop().getItemStack());
			if (c == null)
				return false;
			if (c.type != CardType.POLICY)
				return false;
			e.setCancelled(true);
			// Warte bis das Event abgeklungen ist
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					g.joinedRoom.president_discard(c);
				}
			}, 5);
			return true;
		} else if (g.joinedRoom.gamestate == 3) {
			// Der Kanzler wirft eine Karte weg
			if (g.joinedRoom.chancell != g)
				return false;
			Card c = Main.i.mylib.getCardFromItemStack(e.getItemDrop().getItemStack());
			if (c == null)
				return false;
			if (c.type != CardType.POLICY)
				return false;
			e.setCancelled(true);
			// Warte bis das Event abgeklungen ist
			Main.i.delayedTask(new Runnable() {
				@Override
				public void run() {
					g.joinedRoom.chanc_discard(c);
				}
			}, 5);
			return true;
		}
		return false;
	}

	// Wenn der Kanzler ein Veto einreicht
	public boolean onCommandVETO(CommandSender sender, Command command, String label, String[] args) {
		// Hole den Spieler
		Gamer g = Main.i.mylib.getGamerFromSender(sender);
		if (g == null)
			return true;

		// Check: Ingame!
		if (g.state != 1)
			return true;

		// Hole den Raum
		Room r = g.joinedRoom;
		if (r == null)
			return true;

		// Wenn kein Argument: Veto-Anfrage
		if (args.length == 0) {
			// Richtige Stelle im Spiel
			if (r.gamestate != 3)
				return true;

			// Vetopower unlocked
			if (!r.veto_power)
				return true;

			// Er ist der Kanzler
			if (r.chancell != g)
				return true;

			r.chanc_veto();
			return true;
		} else if (args.length == 1) {
			// Presindent beantwortet die Veto-Anfrage
			// Richtige Stelle im Spiel
			if (r.gamestate != 4)
				return true;

			// Er ist President
			if (r.president != g)
				return true;

			if (args[0].equals("1")) {
				r.presd_veto_accept();
			} else {
				r.presd_veto_deny();
			}

			return true;
		}

		return true;
	}

	// Ein Spieler will diesen Raum joinen
	public void joinRoom(Room r, Gamer g) {
		if (!r.all_right)
			return;

		// CHECK: Raum spielt nicht
		if (r.playing) {
			Main.i.mylib.sendError(g, "room_ingame");
			return;
		}

		// CHECK: Noch ein Platz frei
		if (r.all_gamers.size() >= Main.i.saves.max_player) {
			Main.i.mylib.sendError(g, "room_full");
			return;
		}

		// Er joint
		g.state = 1;
		g.joinedRoom = r;
		r.gamers.add(g);
		r.all_gamers.add(g);
		r.sendMessage(Main.i.saves.config.getString("tr.waiting.join").replaceAll("#name", g.longName),
				ChatColor.YELLOW);

		if (!g.isDummy)
			g.player.teleport(r.spawn);

		updateSign(r);

		int maxtime = r.maxWaitTime(r.all_gamers.size());
		if (r.waiting_time > maxtime)
			r.waiting_time = maxtime;

		updateSidebar(r);
	}

	// Wenn ein Spieler das Spiel verlässt, muss er entfernt werden
	public void quitRoom(Room r, Gamer g) {
		FileConfiguration c = Main.i.saves.config;

		r.sendMessage(Main.i.saves.config.getString("tr.waiting.quit").replaceAll("#name", g.longName),
				ChatColor.YELLOW);

		if (r.playing) {
			// Wenn jmd im Spiel leavt
			if (r.all_gamers.contains(g)) {
				r.all_gamers.remove(g);
				r.gamers.remove(g);
				r.checkGameEnds(g, false);
				if (r.gamers.size() >= 3) {
					if (g == r.president || g == r.chancell)
						Main.i.vtmgr.setNewPresident(r);
				} else if (r.gamers.size() == 2) {
					r.sendMessage(c.getString("tr.game.end.lessplayer"), ChatColor.BLUE, true);
					r.openSecretRoles();
				} else {
					resetRoom(r);
				}
			}
		} else {
			r.gamers.remove(g);
			r.all_gamers.remove(g);
			r.waiting_time += c.getInt("config.wait.less_per_player");
			r.onTimerOneSecond();
		}

		updateSign(r);
		updateSidebar(r);
	}

	// Beschrifte das JoinSchild neu
	public void updateSign(Room r) {
		if (!r.all_right)
			return;
		FileConfiguration c = Main.i.saves.config;
		r.sign.setLine(0, ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + "[" + r.name + "]");
		r.sign.setLine(1, c.getString("tr.lobby.player") + ": " + r.all_gamers.size() + "/" + Main.i.saves.max_player);
		if (r.playing)
			r.sign.setLine(2, ChatColor.BOLD.toString() + ChatColor.DARK_RED + c.getString("tr.lobby.playing"));
		else
			r.sign.setLine(2, ChatColor.BOLD.toString() + ChatColor.GREEN + c.getString("tr.lobby.waiting"));
		r.sign.update();
	}

	// Setzt den Raum in einen Ausgangszustand zurück.
	public void resetRoom(Room r) {
		if (!r.all_right)
			return;

		// Entfernt Spieler, wenn vorhanden
		if (r.all_gamers != null) {
			for (Gamer g : r.all_gamers) {
				if (g.state != 0) {
					g.player.setScoreboard(Main.i.getServer().getScoreboardManager().getNewScoreboard());
					g.getInventory().clear();
					g.state = 0;
					g.joinedRoom = null;
					if (!g.isDummy)
						g.player.teleport(Main.i.saves.spawnPoint);
				}
			}
		}

		r.gamers = new ArrayList<>();
		r.all_gamers = new ArrayList<>();
		r.waiting_time = Main.i.saves.config.getInt("config.wait.wait_at_min");
		r.election_tracker = 0;
		Main.i.gamemgr.setBoardArrays(r, 5);
		r.fac_plc_placed = 0;
		r.lib_plc_placed = 0;
		r.playing = false;


		setElectionTracker(r);
		setItemFrames(r);
		updateSign(r);
	}

	// Setzt die entsprechende Items in die Frames
	public void setItemFrames(Room r) {
		for (int i = 0; i < r.facist_board.length; i++) {
			ItemFrame itmf = Main.i.mylib.getItemFrameInLocation(r.itemFrameLocations[i]);
			if (r.fac_plc_placed > i)
				itmf.setItem(Main.i.cardmgr.cards.get("plc_facist").getItemStack(false));
			else
				itmf.setItem(r.facist_board[i].getItemStack(false));
		}

		for (int i = 0; i < r.liberal_board.length; i++) {
			ItemFrame itmf = Main.i.mylib.getItemFrameInLocation(r.itemFrameLocations[i + r.facist_board.length]);
			if (r.lib_plc_placed > i)
				itmf.setItem(Main.i.cardmgr.cards.get("plc_liberal").getItemStack(false));
			else
				itmf.setItem(r.liberal_board[i].getItemStack(false));
		}

		// Setze die Veto-Power
		if (r.fac_plc_placed >= 5)
			r.veto_power = true;
	}

	// Setze die Blöcke des ElectionTrackers
	public void setElectionTracker(Room r) {
		for (int i = 0; i < r.electionTracker.length; i++) {
			if (r.election_tracker > i)
				Main.i.mylib.setBlock(r.et_material2, r.electionTracker[i]);
			else
				Main.i.mylib.setBlock(r.et_material1, r.electionTracker[i]);
		}
	}

	// Schreibe die Sidebar neu
	public void updateSidebar(Room r) {
		FileConfiguration c = Main.i.saves.config;
		r.sbar.clear();
		
		for (int i=0; i<r.all_gamers.size(); i++) {
			Gamer g = r.all_gamers.get(i);
			String s = g.longName;
			
			if (g.state != 1)
				s = c.getString("config.game.dead_color") + g.longName;
			else if (r.president == g)
				s = c.getString("config.game.presd_color") + g.longName + " " + c.getString("config.game.presd_abbr");
			else if (r.chancell == g)
				s = c.getString("config.game.chanc_color") + g.longName + " " + c.getString("config.game.chanc_abbr");
			else if (r.last_chancell == g)
				s = c.getString("config.game.last_color") + g.longName + " " + c.getString("config.game.lchanc_abbr");
			else if (r.last_president == g && r.gamers.size() > 5)
				s = c.getString("config.game.last_color") + g.longName + " " + c.getString("config.game.lpresd_abbr");
		
			r.sbar.setEntry(i, s);
		}
		
		for (Gamer g : r.all_gamers)
			r.sbar.addToPlayer(g.player);
	}
	

}
