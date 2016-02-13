package de.robfro.secrethitler.gamer;


import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.game.Card;
import de.robfro.secrethitler.game.CardType;
import de.robfro.secrethitler.game.Role;
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
	
	// Ingame
	public Room joinedRoom;
	public Role role;
	public boolean investigated;
	public int vote; // -1..not voted, 0..nein, 1..ja
	public Card[] policies;
	public ArrayList<ItemStack> plcsIS;
	
	
	// EchterSpieler Konstruktor
	public Gamer(Player player) {
		this.player = player;

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
		if (!isDummy && cDummy != -1)
			player.sendMessage(ChatColor.GRAY + "@me: " + ChatColor.RESET + text);
		else if (!isDummy || master.isCurrentDummy(this))
			player.sendMessage(text);
		else
			player.sendMessage(ChatColor.GRAY + "@" + name + ": " + ChatColor.RESET + text);
	}
	
	public void sendMessage(FancyMessage msg) {
		if (isDummy && ! master.isCurrentDummy(this))
			Main.i.admintools.changeDummy(master, master.dummies.indexOf(this));
		msg.send(player);
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

	// Methode f¸r den Master: pr¸ft, ob gegebener Dummy aktiv ist.
	public boolean isCurrentDummy(Gamer dummy) {
		if (cDummy >= 0 && cDummy < dummies.size())
			return dummies.get(cDummy).name.equals(dummy.name);
		else
			return false;
	}

	// Sende eine Willkommensnachricht
	public void sendWelcomeMessage() {
		FileConfiguration c = Main.i.saves.config;
		// ‹berschrift
		new FancyMessage(c.getString("tr.lobby.welcome")).style(ChatColor.BOLD).color(ChatColor.BLUE)
				.send(player);

		// Seriˆser Name
		new FancyMessage(c.getString("tr.lobby.current_longname"))
		.then(longName).color(ChatColor.GRAY)
		.then(" ")
		.then(c.getString("tr.lobby.change_longname")).color(ChatColor.AQUA).tooltip(c.getString("tr.lobby.change_tooltip").split("\\|")).command("/chgnm").send(player);
	}
	
	// Sende die Nachricht ¸ber die Rolle
	public void sendRoleMessage(ArrayList<Gamer> gamers) {
		FileConfiguration c = Main.i.saves.config;
		
		String hitler = "";
		String facists = "";
		
		for (Gamer g : gamers) {
			if (g != this) {
				if (g.role == Role.HITLER)
					hitler = g.longName;
				else if (g.role == Role.FACIST) {
					if (facists != "")
						facists += ", ";
					facists += g.longName;						
				}
			}
		}
		
		if (role == Role.HITLER) {
			sendMessage(ChatColor.BLUE + c.getString("tr.pregame.your_role") + c.getString("tr.pregame.rl_hitler"));
			// Wenn 5 oder 6 Spieler weiﬂ Hitler, wer der Facist ist
			if (gamers.size() < 7)
				sendMessage(ChatColor.BLUE + c.getString("tr.pregame.other_facists") + ChatColor.RESET + facists);
		} else if (role == Role.FACIST) {
			// Faschisten kennen immer Hitler und die anderen Faschisten
			sendMessage(ChatColor.BLUE + c.getString("tr.pregame.your_role") + c.getString("tr.pregame.rl_facist"));
			sendMessage(ChatColor.BLUE + c.getString("tr.pregame.your_fuehrer") + ChatColor.RESET + hitler);
			// Bei 5,6 Spieler gibts es keinen anderen Faschisten
			if (gamers.size() > 6)
				sendMessage(ChatColor.BLUE + c.getString("tr.pregame.other_facists") + ChatColor.RESET + facists);
		} else if (role == Role.LIBERAL) {
			// Liberale wissen nichts
			sendMessage(ChatColor.BLUE + c.getString("tr.pregame.your_role") + c.getString("tr.pregame.rl_liberal"));
		}
	}
	
	// Der Spieler kann in Chat ausw‰hlen, welchen Kanzler er w‰hlen will
	public void sendChancellElectionMessage(Room r) {
		FileConfiguration c = Main.i.saves.config;
		sendMessage(ChatColor.BLUE + c.getString("tr.game.nominate_chancell"));
		
		FancyMessage msg = new FancyMessage("");
		
		for (Gamer g : r.gamers) {
			msg = msg.then("[" + g.longName + "]");
			switch (r.canBeNominated(g)) {
			case 0:
				msg = msg.color(ChatColor.AQUA);
				msg = msg.command("/nominate " + g.name);
				msg = msg.tooltip(c.getString("tr.game.nom.yes"));
				break;
			case 1:
				msg = msg.color(ChatColor.GRAY);
				msg = msg.tooltip(c.getString("tr.game.nom.president"));
				break;
			case 2:
				msg = msg.color(ChatColor.GRAY);
				msg = msg.tooltip(c.getString("tr.game.nom.last_chanc"));
				break;
			case 3:
				msg = msg.color(ChatColor.GRAY);
				msg = msg.tooltip(c.getString("tr.game.nom.last_presd"));
				break;
			}
			msg = msg.then("  ");
		}
		
		sendMessage(msg);
		r.gamestate = 0;
	}

	// Wenn ein Spieler einen Rechtsklick macht
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (state != 1)
			return;
		if (joinedRoom == null)
			return;
		if (joinedRoom.gamestate == 1) {
			// Es wird abgestimmt.
			Card c = Main.i.mylib.getCardFromItemStack(e.getItem());
			if (c == null)
				return;
			if (c.type != CardType.VOTE) {
				sendMessage(ChatColor.RED + Main.i.saves.config.getString("tr.game.votehelp"));
				return;
			}
			if (c == Main.i.cardmgr.cards.get("vt_ja")) {
				vote = 1;
				sendMessage(ChatColor.GREEN + Main.i.saves.config.getString("tr.game.vote_ja"));
				joinedRoom.updateVoting();
			} else if (c == Main.i.cardmgr.cards.get("vt_nein")) {
				vote = 0;
				sendMessage(ChatColor.GREEN + Main.i.saves.config.getString("tr.game.vote_nein"));
				joinedRoom.updateVoting();
			}
		}
	}
}
