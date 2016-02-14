package de.robfro.secrethitler.general;

import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;
import de.robfro.secrethitler.world.Room;

public class MyListener implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Main.i.gamermgr.onPlayerJoin(e);
		e.setJoinMessage("");
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Main.i.gamermgr.onPlayerQuit(e);
		e.setQuitMessage("");
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			e.setCancelled(Main.i.admintools.onPlayerClickBlock(e.getPlayer(), e.getClickedBlock().getLocation()));
		} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getState() instanceof Sign) {
				Main.i.rooms.onClickedSign(e);
			}
			Gamer g = Main.i.mylib.getGamerFromName(e.getPlayer().getName());
			if (g != null)
				g.onPlayerInteract(e);	
		} else if (e.getAction() == Action.RIGHT_CLICK_AIR) {
			Gamer g = Main.i.mylib.getGamerFromName(e.getPlayer().getName());
			if (g != null)
				g.onPlayerInteract(e);
		}
	}
	
	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player)
			e.setCancelled(Main.i.admintools.onPlayerClickBlock((Player)e.getRemover(), e.getEntity().getLocation()));
		if (e.getRemover().hasPermission("sh.admin"))
			return;
		e.setCancelled(true);
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
	
	public void onTimerOneSecond() {
		// Update die Räume
		for (Room r : Main.i.rooms.rooms.values())
			r.onTimerOneSecond();
	}
	
	
	// Player dürfen ItemFrames nicht manipulieren
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof ItemFrame) {
			if (e.getPlayer().hasPermission("sh.admin"))
				return;
			e.setCancelled(true);
		}
	}
	
	// Player dürfen keine Items fallen lassen
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if (Main.i.rooms.onPlayerDropItem(e)) {
			e.setCancelled(true);
			return;
		}
		if (e.getPlayer().hasPermission("sh.admin"))
			return;
		e.setCancelled(true);
		Main.i.mylib.sendError(e.getPlayer(), "drop_item");
	}
}
