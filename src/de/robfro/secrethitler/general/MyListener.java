package de.robfro.secrethitler.general;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import de.robfro.secrethitler.Main;

public class MyListener implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Main.i.gamermgr.onPlayerJoin(e);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Main.i.gamermgr.onPlayerQuit(e);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			e.setCancelled(Main.i.admintools.onPlayerClickBlock(e.getPlayer(), e.getClickedBlock().getLocation()));
		}
	}
	
	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player)
			e.setCancelled(Main.i.admintools.onPlayerClickBlock((Player)e.getRemover(), e.getEntity().getLocation()));
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		Main.i.gamermgr.onAsyncPlayerChat(e);
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if (e.toWeatherState())
			e.setCancelled(true);
	}
}
