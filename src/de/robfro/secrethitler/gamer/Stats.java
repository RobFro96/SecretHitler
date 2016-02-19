package de.robfro.secrethitler.gamer;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.game.Role;

public class Stats {
	
	private int gamesAsLib, gamesAsFac, gamesAsHitler, wonAsLib, wonAsFac, wonAsHitler;
	public int tPresd, tChanc, tKillesSb, tWasKilled, tlast;
	
	public Stats() {
		gamesAsLib = 0;
		gamesAsFac = 0;
		gamesAsHitler = 0;
		wonAsLib = 0;
		wonAsFac = 0;
		wonAsHitler = 0;
		tPresd = 0;
		tChanc = 0;
		tKillesSb = 0;
		tWasKilled = 0;
		tlast = 0;
	}
	
	public Stats(FileConfiguration c, String path) {
		gamesAsLib = c.getInt(path + ".stats.gamesAsLib", 0);
		gamesAsFac = c.getInt(path + ".stats.gamesAsFac", 0);
		gamesAsHitler = c.getInt(path + ".stats.gamesAsHitler", 0);
		wonAsLib = c.getInt(path + ".stats.wonAsLib", 0);
		wonAsFac = c.getInt(path + ".stats.wonAsFac", 0);
		wonAsHitler = c.getInt(path + ".stats.wonAsHitler", 0);
		tPresd = c.getInt(path + ".stats.tPresd", 0);
		tChanc = c.getInt(path + ".stats.tChanc", 0);
		tKillesSb = c.getInt(path + ".stats.tKillesSb", 0);
		tWasKilled = c.getInt(path + ".stats.tWasKilled", 0);
		tlast = c.getInt(path + ".stats.tlast", 0);
	}
	
	public void save(FileConfiguration c, String path) {
		c.set(path + ".stats.gamesAsLib", gamesAsLib);
		c.set(path + ".stats.gamesAsFac", gamesAsFac);
		c.set(path + ".stats.gamesAsHitler", gamesAsHitler);
		c.set(path + ".stats.wonAsLib", wonAsLib);
		c.set(path + ".stats.wonAsFac", wonAsFac);
		c.set(path + ".stats.wonAsHitler", wonAsHitler);
		c.set(path + ".stats.tPresd", tPresd);
		c.set(path + ".stats.tChanc", tChanc);
		c.set(path + ".stats.tKillesSb", tKillesSb);
		c.set(path + ".stats.tWasKilled", tWasKilled);
		c.set(path + ".stats.tlast", tlast);
	}
	
	public void endGame(int win, Gamer g) {
		if (g.role == Role.FACIST) {
			gamesAsFac++;
			if (win == 1)
				wonAsFac ++;
		} else if (g.role == Role.HITLER) {
			gamesAsHitler++;
			if (win == 1)
				wonAsHitler++;
		} else {
			gamesAsLib++;
			if (win == 0)
				wonAsLib++;
		}
	}
	
	private int getAllGames() {
		return gamesAsFac + gamesAsHitler + gamesAsLib;
	}
	
	private int getAllWon() {
		return wonAsFac + wonAsHitler + wonAsLib;
	}
	
	private String precent(int num, int denom) {
		if (denom == 0)
			return "-- %";
		float pc = 100f * num / (float)denom;
		return Math.round(pc) + " %";
	}
	
	public void showStats(Gamer g, Gamer g2) {
		FileConfiguration c = Main.i.saves.config;
		g.sendMessage(ChatColor.BLUE.toString() + ChatColor.BOLD + c.getString("tr.stats.title").replaceAll("#name", g2.longName));
		g.sendMessage(ChatColor.BLUE + c.getString("tr.stats.games") + ChatColor.WHITE + getAllGames());
		g.sendMessage(ChatColor.BLUE + "    " + c.getString("tr.stats.lib") + ChatColor.WHITE + gamesAsLib + " (" + precent(gamesAsLib, getAllGames()) + 
				")    " + ChatColor.BLUE + c.getString("tr.stats.fac") + ChatColor.WHITE + gamesAsFac + " (" + precent(gamesAsFac, getAllGames()) + ")" );
		g.sendMessage(ChatColor.BLUE + "    " + c.getString("tr.stats.hitler") + ChatColor.WHITE + gamesAsHitler + " (" + precent(gamesAsHitler, getAllGames()) + ")");
	
		g.sendMessage(ChatColor.BLUE + c.getString("tr.stats.won") + ChatColor.WHITE + getAllWon() + " (" + precent(getAllWon(), getAllGames()) + ")");
		g.sendMessage(ChatColor.BLUE + "    " + c.getString("tr.stats.lib") + ChatColor.WHITE + wonAsLib + " (" + precent(wonAsLib, getAllWon()) + 
				")    " + ChatColor.BLUE + c.getString("tr.stats.fac") + ChatColor.WHITE + wonAsFac + " (" + precent(wonAsFac, getAllWon()) + ")" );
		g.sendMessage(ChatColor.BLUE + "    " + c.getString("tr.stats.hitler") + ChatColor.WHITE + wonAsHitler + " (" + precent(wonAsHitler, getAllWon()) + ")");
		
		g.sendMessage(ChatColor.BLUE + c.getString("tr.stats.tpresd") + ChatColor.WHITE + tPresd + "   " + 
				ChatColor.BLUE + c.getString("tr.stats.tchanc") + ChatColor.WHITE + tChanc);
		g.sendMessage(ChatColor.BLUE + c.getString("tr.stats.killedsb") + ChatColor.WHITE + tKillesSb + "   " + 
				ChatColor.BLUE + c.getString("tr.stats.waskilled") + ChatColor.WHITE + tWasKilled);
		g.sendMessage(ChatColor.BLUE + c.getString("tr.stats.tlast") + ChatColor.WHITE + tlast);
	}
}
