package de.robfro.secrethitler.general;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.robfro.secrethitler.Main;

public class SaveMgr {
	
	// config.yml - Alle Einstellungen und Übersetzung
	private File configFile;
	public FileConfiguration config;
	
	// rooms.yml
	private File roomFile;
	public FileConfiguration rooms;
	
	// players.yml
	private File playersFile;
	public FileConfiguration players;

	public SaveMgr() {
		// CONFIG
		configFile = new File(Main.i.getDataFolder(), "config.yml");
		configFile.delete();
		config = YamlConfiguration.loadConfiguration(configFile);
		setupDefaults();
		loadSettings();
		
		// ROOMS
		roomFile = new File(Main.i.getDataFolder(), "rooms.yml");
		rooms = YamlConfiguration.loadConfiguration(roomFile);
		
		//PLAYERS
		playersFile = new File(Main.i.getDataFolder(), "players.yml");
		players = YamlConfiguration.loadConfiguration(playersFile);
	}

	// Erstellt die Defaults der config.yml
	private void setupDefaults() {
		config.addDefault("translation.error.wrong_sender", "Die Console kann diesen Befehl nicht ausführen.");
		config.addDefault("translation.error.no_permission", "Du hast nicht die Rechte um diesen Befehl auszuführen.");
		config.addDefault("translation.error.number_args", "Die Anzahl der Argumente ist falsch.");
		config.addDefault("translation.error.not_a_number", "Dein Argument ist keine Zahl.");
		config.addDefault("translation.error.ingame", "Dieser Befehl darf nicht während eines Spieles ausgeführt werden.");
		
		config.addDefault("translation.error.room_exists", "Dieser Name für einen Raum ist bereits belegt.");
		config.addDefault("translation.error.room_exists_not", "Dieser Raum existiert nicht.");
		
		config.addDefault("translation.info.room_created", "Der Raum wurde erfolgreich erstellt.");
		config.addDefault("translation.info.room_spawn", "Der Spawnpunkt des Raumes wurde erfolgreich festgelegt.");
		config.addDefault("translation.info.room_click", "Klicke links als nächste auf: ");
		config.addDefault("translation.info.room_if0", "ItemFrameFacists1");
		config.addDefault("translation.info.room_if1", "ItemFrameFacists2");
		config.addDefault("translation.info.room_if2", "ItemFrameFacists3");
		config.addDefault("translation.info.room_if3", "ItemFrameFacists4");
		config.addDefault("translation.info.room_if4", "ItemFrameFacists5");
		config.addDefault("translation.info.room_if5", "ItemFrameFacists6");
		config.addDefault("translation.info.room_if6", "ItemFrameLiberal1");
		config.addDefault("translation.info.room_if7", "ItemFrameLiberal2");
		config.addDefault("translation.info.room_if8", "ItemFrameLiberal3");
		config.addDefault("translation.info.room_if9", "ItemFrameLiberal4");
		config.addDefault("translation.info.room_if10", "ItemFrameLiberal5");
		config.addDefault("translation.info.room_et0", "ElectionTracker1");
		config.addDefault("translation.info.room_et1", "ElectionTracker2");
		config.addDefault("translation.info.room_et2", "ElectionTracker3");
		
		config.addDefault("translation.command.test", "Teste als Admin die eines Unterprogramms.");
		config.addDefault("translation.command.room", "Bearbeite oder erstelle einen Raum.");
		
		config.options().copyDefaults(true);
		
		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// Läd die speziellen Einstellungen
	private void loadSettings() {
		// Lade die Beschreibung der Commands
		for (String name : config.getConfigurationSection("translation.command").getKeys(false)) {
			Main.i.getCommand(name).setDescription(config.getString("translation.command." + name));
		}
	}
	
	public void saveRooms() {
		try {
			rooms.save(roomFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void savePlayers() {
		try {
			players.save(playersFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
