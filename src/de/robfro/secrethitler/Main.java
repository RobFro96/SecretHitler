package de.robfro.secrethitler;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import de.robfro.secrethitler.game.CardMgr;
import de.robfro.secrethitler.game.GameMgr;
import de.robfro.secrethitler.game.PolicyMgr;
import de.robfro.secrethitler.game.PowerMgr;
import de.robfro.secrethitler.game.VotingMgr;
import de.robfro.secrethitler.gamer.GamerMgr;
import de.robfro.secrethitler.general.AdminTools;
import de.robfro.secrethitler.general.MyLib;
import de.robfro.secrethitler.general.MyListener;
import de.robfro.secrethitler.general.SaveMgr;
import de.robfro.secrethitler.world.RoomMgr;

public class Main extends JavaPlugin {

	public static Main i;
	
	// Instanzen der Helfs-Unterklassen
	public MyListener listener;
	public AdminTools admintools;
	public SaveMgr saves;
	public MyLib mylib;
	public RoomMgr rooms;
	public GamerMgr gamermgr;
	public CardMgr cardmgr;
	public GameMgr gamemgr;
	public VotingMgr vtmgr;
	public PolicyMgr plcmgr;
	public PowerMgr pwrmgr;
	
	@Override
	public void onEnable() {
		i = this;
		
		// Starte die Helfs-Unterklassen
		admintools = new AdminTools();
		saves = new SaveMgr();
		mylib = new MyLib();
		gamermgr = new GamerMgr();
		gamermgr.onPluginEnabled();
		cardmgr = new CardMgr();
		gamemgr = new GameMgr();
		vtmgr = new VotingMgr();
		plcmgr = new PolicyMgr();
		pwrmgr = new PowerMgr();
		rooms = new RoomMgr();
		rooms.load();
		
		// Starte den Listener
		listener = new MyListener();
		getServer().getPluginManager().registerEvents(listener, this);

		getLogger().info("SecretHitler started.");
			
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				listener.onTimerOneSecond();
			}
		}, 20L, 20L);
		
	}

	@Override
	public void onDisable() {
		gamermgr.saveAllGamers();
		HandlerList.unregisterAll(listener);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		switch (label.toLowerCase()) {
		case "test":
			return admintools.onCommandTEST(sender, command, label, args);
		case "room":
			return admintools.onCommandROOM(sender, command, label, args);
		case "dummy":
		case "d":
			return admintools.onCommandDUMMY(sender, command, label, args);
		case "chgnm":
			return gamermgr.onCommandCHGNM(sender, command, label, args);
		case "wait":
			return admintools.onCommandWAIT(sender, command, label, args);
		case "nominate":
			return rooms.onCommandNOMINATE(sender, command, label, args);
		case "veto":
			return rooms.onCommandVETO(sender, command, label, args);
		case "frz":
		case "freeze":
			return admintools.onCommandFRZ(sender, command, label, args);
		}
			
		
		return false;
	}
	
	public void delayedTask(Runnable r, long delay) {
		getServer().getScheduler().scheduleSyncDelayedTask(this, r, delay);
	}

}
