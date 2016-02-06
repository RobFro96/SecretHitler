package de.robfro.secrethitler.general;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.gamer.Gamer;

public class AdminTools {
	
	public boolean onCommandTEST(CommandSender sender, Command command, String label, String[] args) {
		// CHECK: Admin
		if (!sender.hasPermission("sh.admin")) {
			Main.i.mylib.sendError(sender, "no_permission");
			return true;
		}
		
		// CHECK: Mind. 1 Argument
		if (args.length < 1) {
			Main.i.mylib.sendError(sender, "number_args");
			return true;
		}
		
		Gamer g = Main.i.gamermgr.getGamer(sender.getName());
		
		switch (args[0]) {
		case "board":
			if (g == null) {
				Main.i.mylib.sendError(sender, "wrong_sender");
				return true;
			}
			for (Entity e : g.player.getWorld().getNearbyEntities(g.player.getLocation(), 10, 10, 10)) {
				if (e instanceof ItemFrame)
					g.player.sendMessage(e.getLocation().toString());
			}
		}
		
		
		return true;
	}
	
}
