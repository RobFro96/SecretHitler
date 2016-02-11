package de.robfro.secrethitler.game;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.ChatColor;

import de.robfro.secrethitler.Main;
import de.robfro.secrethitler.world.Room;

public class PoliciesDeck {

	private ArrayList<Card> draw, discard;
	
	public PoliciesDeck(int liberal, int facist) {
		draw = new ArrayList<>();
		discard = new ArrayList<>();
		
		for (int i=0; i<liberal; i++)
			draw.add(Main.i.cardmgr.cards.get("plc_liberal"));
		
		for (int i=0; i<facist; i++)
			draw.add(Main.i.cardmgr.cards.get("plc_facist"));
		
		shuffle();
	}
	
	private void shuffle() {
		Collections.shuffle(draw);
	}
	
	public Card getOneCard(Room r, boolean check) {
		Card c = draw.get(0);
		draw.remove(0);
		if (check)
			checkMoreThanThreeCards(r);
		return c;
	}
	
	public Card[] getThreeCards(Room r) {
		Card[] cards = new Card[3];
		cards[0] = getOneCard(r, false);
		cards[1] = getOneCard(r, false);
		cards[2] = getOneCard(r, false);
		checkMoreThanThreeCards(r);
		return cards;
	}
	
	private void checkMoreThanThreeCards(Room r) {
		if (draw.size() < 3) {
			draw.addAll(discard);
			shuffle();
			r.sendMessage(ChatColor.BLUE + Main.i.saves.config.getString("tr.game.cards_shuffled"));
		}
	}
	
	public Card[] showThreeCards() {
		Card[] cards = new Card[3];
		cards[0] = draw.get(0);
		cards[1] = draw.get(1);
		cards[2] = draw.get(2);
		return cards;
	}
}
