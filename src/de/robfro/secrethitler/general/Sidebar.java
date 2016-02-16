package de.robfro.secrethitler.general;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import de.robfro.secrethitler.Main;


public class Sidebar {

	private int size;
	private Scoreboard scoreb;
	private Objective obj;
	private ArrayList<String> entries;
	
	public Sidebar(int size, String name, String title) {
		this.size = size;
		
		scoreb = Main.i.getServer().getScoreboardManager().getNewScoreboard();
		
		obj = scoreb.registerNewObjective(name, "");
		obj.setDisplayName(title);
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		entries = new ArrayList<>();
		
		for (int i=0; i<size; i++) {
			entries.add(getSpaces(i));
		}
		
		for (int i=0; i<size; i++) {
			Score s = obj.getScore(entries.get(i));
			s.setScore(size - i - 1);
		}
	}
	
	public void clear() {
		for (int i=0; i<size; i++) {
			setEntry(i, getSpaces(i));
		}
	}
	
	private String getSpaces(int len) {
		String ret = "";
		for (int i=0; i<len; i++)
			ret += " ";
		return ret;
	}
	
	public void setTitle(String title) {
		obj.setDisplayName(title);
	}
	
	public void addToPlayer(Player p) {
		p.setScoreboard(scoreb);
	}
	
	public void setEntry(int id, String value) {
		obj.getScoreboard().resetScores(entries.get(id));
		
		entries.set(id, value);
		Score s = obj.getScore(entries.get(id));
		s.setScore(size - id - 1);
	}
	
}
