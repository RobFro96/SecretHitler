package de.robfro.secrethitler.gamer;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.robfro.secrethitler.Main;



public class GamerMgr {

	public ArrayList<Gamer> gamers;

	public GamerMgr() {
		gamers = new ArrayList<>();
	}

	// Wenn Plugin gestartet wird (reload) und Spieler auf den Server waren,
	// werden Gamer für diese erstellt
	public void onPluginEnabled() {
		for (Player p : Main.i.getServer().getOnlinePlayers()) {
			Gamer g = new Gamer(p);
			g.loadYAML();
			if (getGamer(g.name) == null)
				gamers.add(g);
		}
	}

	// Erstelle den Gamer für einen Spieler, der gerade gejoint ist
	public void onPlayerJoin(PlayerJoinEvent e) {
		Gamer g = new Gamer(e.getPlayer());
		g.loadYAML();
		if (getGamer(g.name) == null)
			gamers.add(g);

		sendLobbyMessage(
				ChatColor.YELLOW + Main.i.saves.config.getString("tr.lobby.join").replaceAll("#name", g.longName));

		g.sendWelcomeMessage();

		if (Main.i.saves.spawnPoint != null)
			g.player.teleport(Main.i.saves.spawnPoint);
		if (!g.player.hasPermission("sh.admin")) {
			g.player.getInventory().clear();
			g.player.setGameMode(GameMode.ADVENTURE);
		}
		g.player.setLevel(0);
		g.player.setExp(0);
	}

	// Lösche den Gamer für den Spieler, der gerade geleavt ist
	public void onPlayerQuit(PlayerQuitEvent e) {
		for (Gamer g : gamers) {
			if (g.name.equals(e.getPlayer().getName())) {
				g.saveYAML();
				gamers.remove(g);

				if (g.joinedRoom != null)
					Main.i.rooms.quitRoom(g.joinedRoom, g);

				if (g.dummies != null) {
					for (Gamer d : g.dummies) {
						d.saveYAML();
						gamers.remove(d);
						if (d.joinedRoom != null)
							Main.i.rooms.quitRoom(d.joinedRoom, d);
					}
				}

				sendLobbyMessage(ChatColor.YELLOW
						+ Main.i.saves.config.getString("tr.lobby.quit").replaceAll("#name", g.longName));

				break;
			}
		}
		Main.i.saves.savePlayers();
	}

	// Hole den Gamer mit Hilfe des Spielernames
	public Gamer getGamer(String name) {
		for (Gamer g : gamers) {
			if (g.name.equals(name))
				return g;
		}
		return null;
	}

	// Speichere alle Spieler ab, wird gemacht, wenn das Plugin beendet wird
	public void saveAllGamers() {
		for (Gamer g : gamers) {
			g.saveYAML();
		}
		Main.i.saves.savePlayers();
	}

	// Befehl: Ändere seriösen Name
	public boolean onCommandCHGNM(CommandSender sender, Command command, String label, String[] args) {
		Gamer g = Main.i.mylib.getGamerFromSender(sender);
		if (g == null)
			return true;

		if (g.state != 0) {
			Main.i.mylib.sendError(g, "ingame");
			return false;
		}

		g.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + Main.i.saves.config.getString("tr.lobby.change"));
		g.inputLongName = true;

		return true;
	}

	// ChatManager!
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		Gamer g = getGamer(e.getPlayer().getName());
		if (g == null)
			return;

		e.setCancelled(true);

		// Check Flags
		if (g.inputLongName) {
			g.longName = e.getMessage();
			g.inputLongName = false;
			Main.i.mylib.sendInfo(g, "chgnm");
			return;
		}

		// In der Lobby
		if (g.state == 0) {
			sendLobbyMessage("<" + g.longName + "> " + e.getMessage());
		} else if (g.state == 1) {
			if (g.joinedRoom == null)
				return;
			g.joinedRoom.sendMessage("<" + g.longName + "> " + e.getMessage(), ChatColor.WHITE);
		}
	}

	public void sendLobbyMessage(String msg) {
		// Check: Chatten ist erlaubt in der Lobby
		if (!Main.i.saves.allow_chat_in_lobby)
			return;
		for (Gamer r : gamers) {
			if (r.state == 0) {
				r.sendMessage(msg);
			}
		}
	}
}
