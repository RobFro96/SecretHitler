package de.robfro.secrethitler.gamer;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import de.robfro.secrethitler.Main;

public class Gamer {

	public Player player;
	public String name;
	public int state;	// 0...Lobby, 1...Ingame, 100+...itemFrames, 200+...ElectionTracker
	public String longName;
	
	
	public Gamer(Player player) {
		this.player = player;
		name = player.getName();
		state = 0;
	}
	
	public void loadYAML() {
		FileConfiguration c = Main.i.saves.players;
		longName = c.getString(name + ".longName", name);
	}
	
	public void saveYAML() {
		FileConfiguration c = Main.i.saves.players;
		c.set(name + ".longName", longName);
	}
	
}
