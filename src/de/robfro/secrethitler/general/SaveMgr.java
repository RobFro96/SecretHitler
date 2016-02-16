package de.robfro.secrethitler.general;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.robfro.secrethitler.Main;

public class SaveMgr {

	// config.yml - Alle Einstellungen und �bersetzung
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
	public Location spawnPoint;

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
		config.addDefault("tr.error.wrong_sender", "Die Console kann diesen Befehl nicht ausf�hren.");
		config.addDefault("tr.error.no_permission", "Du hast nicht die Rechte um diesen Befehl auszuf�hren.");
		config.addDefault("tr.error.number_args", "Die Anzahl der Argumente ist falsch.");
		config.addDefault("tr.error.not_a_number", "Dein Argument ist keine Zahl.");
		config.addDefault("tr.error.ingame", "Diese Aktion darf nicht w�hrend eines Spieles ausgef�hrt werden.");
		config.addDefault("tr.error.not_ingame", "Diese Aktion kann nur innerhalb eines Raumes ausgef�hrt werden.");
		config.addDefault("tr.error.playing", "Diese Aktion kann nur w�hrend der Wartezeit ausgef�hrt werden.");

		config.addDefault("tr.error.room_exists", "Dieser Name f�r einen Raum ist bereits belegt.");
		config.addDefault("tr.error.room_exists_not", "Dieser Raum existiert nicht.");
		config.addDefault("tr.error.if_not_exists", "Es existiert hier kein ItemFrame.");
		config.addDefault("tr.error.sign", "Es existiert hier kein Schild.");

		config.addDefault("tr.error.no_dummies", "Du hast keine Dummies.");

		config.addDefault("tr.error.room_ingame",
				"Dieser Raum kann nicht betreten werden, da dieser sich in einem Spiel befindet.");
		config.addDefault("tr.error.room_full", "Es gibt in diesen Raum keinen freien Platz mehr.");
		config.addDefault("tr.error.drop_item", "Items d�rfen nicht fallengelassen werden.");

		config.addDefault("tr.error.not_presd", "Nur den Pr�sident darf diesen Befehl ausf�hren.");
		config.addDefault("tr.error.not_a_player", "Das Argument verweist auf keinen Spieler.");
		config.addDefault("tr.error.cant_nominated", "Dieser Spieler kann nicht nominiert werden.");

		config.addDefault("tr.info.room_created", "Der Raum wurde erfolgreich erstellt.");
		config.addDefault("tr.info.room_spawn", "Der Spawnpunkt des Raumes wurde erfolgreich festgelegt.");
		config.addDefault("tr.info.room_material", "Das ElectionTracke-Material wurde erfolgreich festgelegt.");
		config.addDefault("tr.info.room_click", "Klicke links als n�chste auf: ");
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
		config.addDefault("tr.info.chgnm", "Dein seri�ser Name wurde erfolgreich ge�ndert.");
		config.addDefault("tr.info.all_pos", "Alle Positionen wurden gesetzt. Erst jetzt wurde gespeichert.");

		config.addDefault("tr.info.disable_dummy", "Du bist jetzt du selber.");
		config.addDefault("tr.info.change_dummy", "Du hast auf folgenden Dummy gewechselt: ");

		config.addDefault("tr.lobby.welcome", "Willkommen auf RobFros-Secret-Hitler-Server!");
		config.addDefault("tr.lobby.current_longname", "Aktueller seri�ser Name: ");
		config.addDefault("tr.lobby.change_longname", "[�ndern]");
		config.addDefault("tr.lobby.change_tooltip",
				"Ein Politiker ben�tigt |einen seri�sen Namen. |�ndere diesen hier.");
		config.addDefault("tr.lobby.change", "�ndere deinen Name, indem du ihn jetzt in den Chat eingibst.");
		config.addDefault("tr.lobby.change_info", "Dein seri�ser Name wurde erfolgreich ge�ndert.");
		config.addDefault("tr.lobby.player", "Spieler");
		config.addDefault("tr.lobby.waiting", "OFFEN");
		config.addDefault("tr.lobby.playing", "IM SPIEL");
		config.addDefault("tr.lobby.join", "#name hat den Server betreten.");
		config.addDefault("tr.lobby.quit", "#name hat dem Server verlassen.");

		config.addDefault("tr.waiting.join", "#name hat diesen Raum betreten.");
		config.addDefault("tr.waiting.quit", "#name hat diesen Raum verlassen.");

		config.addDefault("tr.pregame.started", "Das Spiel wird gestartet.");
		config.addDefault("tr.pregame.your_role", "Deine geheime Rolle in diesem Spiel ist: ");
		config.addDefault("tr.pregame.rl_hitler", ChatColor.DARK_RED + "Hitler");
		config.addDefault("tr.pregame.rl_facist", ChatColor.RED + "Faschist");
		config.addDefault("tr.pregame.rl_liberal", ChatColor.DARK_AQUA + "Liberaler");
		config.addDefault("tr.pregame.your_fuehrer", "Folgender Spieler ist Hitler: ");
		config.addDefault("tr.pregame.other_facists", "Folgende Spieler spielen ebenfalls als Faschist: ");

		config.addDefault("tr.game.pres_was_elected", "#prnm wurde zum Pr�sidenten gew�hlt.");
		config.addDefault("tr.game.nominate_chancell", "Als Pr�sindent musst du nun einen Kanzler nominieren.");
		config.addDefault("tr.game.nom.yes", "Nominiere diesen Spieler.");
		config.addDefault("tr.game.nom.president", "Der Pr�sident darf sich nicht nominieren.");
		config.addDefault("tr.game.nom.last_chanc", "Der letzte Kanzler darf nicht nominiert werden.");
		config.addDefault("tr.game.nom.last_presd", "Der letzte Pr�sident darf nicht nominiert werden.");
		config.addDefault("tr.game.vote", "#prnm schl�gt #chnm als Kanzler vor. Bitte stimmt ab.");
		config.addDefault("tr.game.votehelp", "Rechtsklick mit der entsprechende Wahlkarte.");
		config.addDefault("tr.game.vote_ja", "Du stimmst f�r den Kanzler.");
		config.addDefault("tr.game.vote_nein", "Du stimmst gegen den Kanzler.");
		config.addDefault("tr.game.result", "Die Wahl ist abgeschlossen. Hier siehst du das Wahlergebnis:");
		config.addDefault("tr.game.result_ja", ChatColor.GREEN + "JA");
		config.addDefault("tr.game.result_nein", ChatColor.RED + "NEIN");
		config.addDefault("tr.game.vote_sucessf", "#chnm wurde erfolgreich zum Kanzler gew�hlt.");
		config.addDefault("tr.game.vote_failed", "#chnm wurde nicht gew�hlt.");

		config.addDefault("tr.game.cards_shuffled", "Die Artikelkarten wurden neu gemischt.");
		config.addDefault("tr.game.et_full",
				"Es kam zu drei Fehlwahlen in Folge. Deshalb wird der oberste Artikel aufgedeckt und gelegt. Die pr�sidiale Macht verf�llt.");
		config.addDefault("tr.game.presd_draws",
				"#prnm zieht nun drei Artikelkarten und vernichtet eine davon.");
		config.addDefault("tr.game.presd_discard",
				"Du hast drei Artikelkarten gezogen. Du musst eine davon vernichten, indem du diese mit Q aus deinen Inventar wirfst.");
		config.addDefault("tr.game.chan_gets",
				"#prnm vernichtet eine Karte und gibt die anderen Beiden #chnm. Dieser muss nun entscheiden welcher Artikel gelegt wird.");
		config.addDefault("tr.game.chan_discard",
				"Du hast nun zwei Artikelkarten von #prnm bekommen. Wirf eine davon mit Q aus den Inventar, um diese zu vernichten. Die andere wird gelegt.");
		config.addDefault("tr.game.veto_power", "Du hast die M�glichkeit in Veto zu gehen: ");
		config.addDefault("tr.game.veto", "[VETO]");
		config.addDefault("tr.game.chan_places",
				"#chnm hat eine Karte vernichtet und die andere auf das Brett gelegt.");
		config.addDefault("tr.game.chanc_vetos", "#chnm reicht ein Veto ein. Nun muss #prnm best�tigen.");
		config.addDefault("tr.game.presd_veto",
				"Stimmst du den Veto con #chnm zu, um diese Runde kein Artikel zu legen? ");
		config.addDefault("tr.game.veto_accept", "[VETO ZUSTIMMEN]");
		config.addDefault("tr.game.veto_deny", "[VETO ABLEHNEN]");
		config.addDefault("tr.game.accept",
				"#prnm hat das Veto angenommen. Diese Runde wird kein Artikel gelegt.");
		config.addDefault("tr.game.deny",
				"#prnm hat das Veto abgelehnt. #chnm muss sich nun f�r ein Artikel entscheiden.");

		config.addDefault("tr.game.power.invest.info",
				"#prnm kann nun die Parteiangeh�rigkeit eines Spielers erfahren.");
		config.addDefault("tr.game.power.invest.who",
				"Von welchen Spieler willst du die Parteiangeh�rigkeit erfahren?");
		config.addDefault("tr.game.power.invest.this", "W�hle diesen Spieler.");
		config.addDefault("tr.game.power.invest.you", "Du kannst dich nicht selber �berpr�fen.");
		config.addDefault("tr.game.power.invest.was_invest", "Dieser Spieler wurde schon einmal �berpr�ft.");
		config.addDefault("tr.game.power.invest.invest", "#prnm �berpr�ft die Parteiangeh�rigkeit von #name.");
		config.addDefault("tr.game.power.invest.result", "#name geh�rt der #party Partei an.");
		config.addDefault("tr.game.power.invest.fac", "faschisten");
		config.addDefault("tr.game.power.invest.lib", "liberalen");

		config.addDefault("tr.game.power.presd.info", "#prnm darf nun den n�chsten Pr�sidenten bestimmen.");
		config.addDefault("tr.game.power.presd.who", "Welcher Spieler soll n�chster Pr�sident werden?");
		config.addDefault("tr.game.power.presd.this", "W�hle diesen Spieler.");
		config.addDefault("tr.game.power.presd.you", "Du kannst dich nicht selber w�hlen.");

		config.addDefault("tr.game.power.exam.info",
				"#prnm kann nun die n�chsten drei Artikel vom Stapel betrachten.");
		config.addDefault("tr.game.power.exam.result", "Die n�chsten drei Artikel sind: ");
		config.addDefault("tr.game.power.exam.fac", ChatColor.RED + "[Faschistisch] ");
		config.addDefault("tr.game.power.exam.lib", ChatColor.DARK_AQUA + "[Liberal] ");

		config.addDefault("tr.game.power.veto.info", "Das Vetorecht wurde freigeschalten.");

		config.addDefault("tr.game.power.kill.info", "#prnm muss nun einen Spieler hinrichten.");
		config.addDefault("tr.game.power.kill.who", "Welchen Spieler soll hingerichtet werden?");
		config.addDefault("tr.game.power.kill.this", "W�hle diesen Spieler.");
		config.addDefault("tr.game.power.kill.you", "Du kannst dich nicht selber hinrichten lassen.");
		config.addDefault("tr.game.power.kill.kill", "#name wurde hingerichtet.");

		config.addDefault("tr.game.end.libwin", "Die liberale Partei hat das Spiel gewonnen, da ");
		config.addDefault("tr.game.end.facwin", "Die faschistische Partei hat das Spiel gewonnen, da ");
		config.addDefault("tr.game.end.libplcs", "f�nf liberale Artikel liegen.");
		config.addDefault("tr.game.end.killhitler", "Hitler get�tet wurde.");
		config.addDefault("tr.game.end.facplcs", "sechs liberale Artikel liegen.");
		config.addDefault("tr.game.end.hitlerelected",
				"Hitler zum Kanzler gew�hlt wurde, nachdem mindestens drei faschistische Artikel lagen.");
		config.addDefault("tr.game.end.roles", "Hier siehst du die Rollenverteilung in diesem Spiel: ");
		config.addDefault("tr.game.end.lessplayer", "Das Spiel musste beendet werden, da zu wenige Spieler noch da sind.");
		config.addDefault("tr.game.warn_chancell", "Achtung! Es liegen drei Faschistische Artikel. Sollte nun Hitler zum Kanzler gew�hlt werden, so kommt es zur Machtergreifung.");

		
		config.addDefault("tr.command.test", "Teste als Admin die eines Unterprogramms.");
		config.addDefault("tr.command.room", "Bearbeite oder erstelle einen Raum.");
		config.addDefault("tr.command.dummy", "Wechsele auf eine Dummy oder zur�ck zu dir selbst.");
		config.addDefault("tr.command.chgnm", "�ndere deinen seri�sen Namen mit diesem Befehl.");
		config.addDefault("tr.command.wait", "Setze als Admin die Wartezeit innerhalb eines Raumes.");
		config.addDefault("tr.command.nominate", "Wird genutz, um einen Spieler zu nominieren.");
		config.addDefault("tr.command.veto", "Wird genutzt, um in Veto zu gehen.");

		config.addDefault("tr.maps.plc_liberal", "Liberaler Artikel");
		config.addDefault("tr.maps.plc_facist", "Faschistischer Artikel");
		config.addDefault("tr.maps.rl_facist", "Rolle: Faschist");
		config.addDefault("tr.maps.rl_hitler", "Rolle: Hitler");
		config.addDefault("tr.maps.rl_liberal", "Rolle: Liberaler");
		config.addDefault("tr.maps.vt_ja", "JA");
		config.addDefault("tr.maps.vt_nein", "NEIN");

		config.addDefault("config.allow_chat_in_lobby", true);
		config.addDefault("config.max_player_in_room", 10);
		config.addDefault("config.spawnpoint", "8,22,30");

		config.addDefault("config.wait.min_player", 5);
		config.addDefault("config.wait.wait_at_min", 60);
		config.addDefault("config.wait.less_per_player", 10);

		config.addDefault("config.maps.plc_liberal", 100);
		config.addDefault("config.maps.plc_facist", 101);
		config.addDefault("config.maps.rl_facist", 102);
		config.addDefault("config.maps.rl_hitler", 103);
		config.addDefault("config.maps.rl_liberal", 104);
		config.addDefault("config.maps.vt_ja", 105);
		config.addDefault("config.maps.vt_nein", 106);
		config.addDefault("config.maps.brd_facempty", 107);
		config.addDefault("config.maps.brd_facend", 108);
		config.addDefault("config.maps.brd_invest", 109);
		config.addDefault("config.maps.brd_kill", 110);
		config.addDefault("config.maps.brd_veto", 111);
		config.addDefault("config.maps.brd_libempty", 112);
		config.addDefault("config.maps.brd_libend", 113);
		config.addDefault("config.maps.brd_presd", 114);
		config.addDefault("config.maps.brd_exam", 115);

		config.addDefault("config.game.liberal_plcs", 6);
		config.addDefault("config.game.facist_plcs", 11);
		config.addDefault("config.game.presd_color", ChatColor.DARK_GREEN);
		config.addDefault("config.game.presd_abbr", "[P]");
		config.addDefault("config.game.chanc_color", ChatColor.GOLD);
		config.addDefault("config.game.chanc_abbr", "[K]");
		config.addDefault("config.game.last_color", ChatColor.GRAY);
		config.addDefault("config.game.dead_color", ChatColor.DARK_GRAY);
		config.addDefault("config.game.lchanc_abbr", "[lK]");
		config.addDefault("config.game.lpresd_abbr", "[lP]");

		config.options().copyDefaults(true);

		try {
			config.save(configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// L�d die speziellen Einstellungen
	private void loadSettings() {
		// Lade die Beschreibung der Commands
		for (String name : config.getConfigurationSection("tr.command").getKeys(false)) {
			Main.i.getCommand(name).setDescription(config.getString("tr.command." + name));
		}

		// Einstellungen
		allow_chat_in_lobby = config.getBoolean("config.allow_chat_in_lobby");
		max_player = config.getInt("config.max_player_in_room");
		spawnPoint = MyLib.ParseLocation(config.getString("config.spawnpoint"));
		if (spawnPoint == null) {
			Main.i.getLogger().warning("Spawnpoint is not defined.");
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
