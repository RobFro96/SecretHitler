package de.robfro.secrethitler.game;

import java.util.HashMap;

public class CardMgr {
	
	public HashMap<String, Card> cards;
	
	public CardMgr() {
		cards = new HashMap<>();
		
		cards.put("plc_liberal", new Card("plc_liberal" ,CardType.POLICY));
		cards.put("plc_facist", new Card("plc_facist", CardType.POLICY));
		cards.put("rl_facist", new Card("rl_facist", CardType.ROLE));
		cards.put("rl_hitler", new Card("rl_hitler", CardType.ROLE));
		cards.put("rl_liberal", new Card("rl_liberal", CardType.ROLE));
		cards.put("vt_ja", new Card("vt_ja", CardType.VOTE));
		cards.put("vt_nein", new Card("vt_nein", CardType.VOTE));
		cards.put("brd_facempty", new Card("brd_facempty", CardType.BOARD));
		cards.put("brd_facend", new Card("brd_facend", CardType.BOARD));
		cards.put("brd_invest", new Card("brd_invest", CardType.BOARD));
		cards.put("brd_kill", new Card("brd_kill", CardType.BOARD));
		cards.put("brd_veto", new Card("brd_veto", CardType.BOARD));
		cards.put("brd_libempty", new Card("brd_libempty", CardType.BOARD));
		cards.put("brd_libend", new Card("brd_libend", CardType.BOARD));
		cards.put("brd_presd", new Card("brd_presd", CardType.BOARD));
		cards.put("brd_exam", new Card("brd_exam", CardType.BOARD));
	}
	
}
