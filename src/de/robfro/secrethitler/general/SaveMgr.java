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

	// Einstellungen
	public boolean allow_chat_in_lobby;
	public int max_player;

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

		// PLAYERS
		playersFile = new File(Main.i.getDataFolder(), "players.yml");
		players = YamlConfiguration.loadConfiguration(playersFile);
	}

	// Erstellt die Defaults der config.yml
	private void setupDefaults() {
		config.addDefault("tr.error.wrong_sender", "Die Console kann diesen Befehl nicht ausführen.");
		config.addDefault("tr.error.no_permission", "Du hast nicht die Rechte um diesen Befehl auszuführen.");
		config.addDefault("tr.error.number_args", "Die Anzahl der Argumente ist falsch.");
		config.addDefault("tr.error.not_a_number", "Dein Argument ist keine Zahl.");
		config.addDefault("tr.error.ingame", "Diese Aktion darf nicht während eines Spieles ausgeführt werden.");

		config.addDefault("tr.error.room_exists", "Dieser Name für einen Raum ist bereits belegt.");
		config.addDefault("tr.error.room_exists_not", "Dieser Raum existiert nicht.");
		config.addDefault("tr.error.if_not_exists", "Es existiert hier kein ItemFrame.");
		config.addDefault("tr.error.sign", "Es existiert hier kein Schild.");

		config.addDefault("tr.error.no_dummies", "Du hast keine Dummies.");

		config.addDefault("tr.error.room_ingame",
				"Dieser Raum kann nicht betreten werden, da dieser sich in einem Spiel befindet.");
		config.addDefault("tr.error.room_full", "Es gibt in diesen Raum keinen freien Platz mehr.");

		config.addDefault("tr.info.room_created", "Der Raum wurde erfolgreich erstellt.");
		config.addDefault("tr.info.room_spawn", "Der Spawnpunkt des Raumes wurde erfolgreich festgelegt.");
		config.addDefault("tr.info.room_click", "Klicke links als nächste auf: ");
		config.addDefault("tr.info.room_if0", "ItemFrameFacists1");
		config.addDefault("tr.info.room_if1", "ItemFrameFacists2");
		config.addDefault("tr.info.room_if2", "ItemFrameFacists3");
		config.addDefault("tr.info.room_if3", "ItemFrameFacists4");
		config.addDefault("tr.info.room_if4", "ItemFrameFacists5");
		config.addDefault("tr.info.room_if5", "ItemFrameFacists6");
		config.addDefault("tr.info.room_if6", "ItemFrameLiberal1");
		config.addDefault("tr.info.room_if7", "ItemFrameLiberal2");
		config.addDefault("tr.info.room_if8", "ItemFrameLiberal3");
		config.addDefault("tr.info.room_if9", "ItemFrameLiberal4");
		config.addDefault("tr.info.room_if10", "ItemFrameLiberal5");
		config.addDefault("tr.info.room_et0", "ElectionTracker1");
		config.addDefault("tr.info.room_et1", "ElectionTracker2");
		config.addDefault("tr.info.room_et2", "ElectionTracker3");
		config.addDefault("tr.info.room_sign", "JoinSign");
		config.addDefault("tr.info.chgnm", "Dein seriöser Name wurde erfolgreich geändert.");
		config.addDefault("tr.info.all_pos", "Alle Positionen wurden gesetzt. Erst jetzt wurde gespeichert.");

		config.addDefault("tr.info.disable_dummy", "Du bist jetzt du selber.");
		config.addDefault("tr.info.change_dummy", "Du hast auf folgenden Dummy gewechselt: ");

		config.addDefault("tr.lobby.welcome", "Willkommen auf RobFros-Secret-Hitler-Server!");
		config.addDefault("tr.lobby.current_longname", "Aktueller seriöser Name: ");
		config.addDefault("tr.lobby.change_longname", "[Ändern]");
		config.addDefault("tr.lobby.change_tooltip",
				"Ein Politiker benötigt |einen seriösen Namen. |Ändere diesen hier.");
		config.addDefault("tr.lobby.change", "Ändere deinen Name, indem du ihn jetzt in den Chat eingibst.");
		config.addDefault("tr.lobby.change_info", "Dein seriöser Name wurde erfolgreich geändert.");
		config.addDefault("tr.lobby.player", "Spieler");
		config.addDefault("tr.lobby.waiting", "OFFEN");
		config.addDefault("tr.lobby.playing", "IM SPIEL");
		
		config.addDefault("tr.waiting.join", "#name hat diesen Raum betreten.");


		config.addDefault("tr.command.test", "Teste als Admin die eines Unterprogramms.");
		config.addDefault("tr.command.room", "Bearbeite oder erstelle einen Raum.");
		config.addDefault("tr.command.dummy", "Wechsele auf eine Dummy oder zurück zu dir selbst.");
		config.addDefault("tr.command.chgnm", "Ändere deinen seriösen Namen mit diesem Befehl.");

		config.addDefault("config.allow_chat_in_lobby", true);
		config.addDefault("config.max_player_in_room", 10);
		config.addDefault("config.wait.min_player", 5);
		config.addDefault("config.wait.wait_at_min", 60);
		config.addDefault("config.wait.wait_at_max", 15);

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
		for (String name : config.getConfigurationSection("tr.command").getKeys(false)) {
			Main.i.getCommand(name).setDescription(config.getString("tr.command." + name));
		}

		// Einstellungen
		allow_chat_in_lobby = config.getBoolean("config.allow_chat_in_lobby");
		max_player = config.getInt("config.max_player_in_room");
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
