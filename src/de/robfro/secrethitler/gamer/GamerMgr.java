package de.robfro.secrethitler.gamer;

import java.util.ArrayList;

import org.bukkit.entity.Player;
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
	}

	// Lösche den Gamer für den Spieler, der gerade geleavt ist
	public void onPlayerQuit(PlayerQuitEvent e) {
		for (Gamer g : gamers) {
			if (g.name.equals(e.getPlayer().getName())) {
				g.saveYAML();
				gamers.remove(g);
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
	
}
