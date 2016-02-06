package de.robfro.secrethitler.general;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
			e.getPlayer().sendMessage(e.getClickedBlock().getLocation().toString());
		}
	}
	
	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		e.getRemover().sendMessage(e.getEntity().getLocation().toString());
	}
}
