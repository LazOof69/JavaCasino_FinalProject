package casino.game.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 牌組類別
 * Deck Class
 */
public class Deck {

    private List<Card> cards;

    public Deck() {
        initializeDeck();
    }

    /**
     * 初始化一副完整的撲克牌（52張）
     */
    private void initializeDeck() {
        cards = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    /**
     * 洗牌
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * 發一張牌
     */
    public Card deal() {
        if (cards.isEmpty()) {
            initializeDeck();
            shuffle();
        }
        return cards.remove(cards.size() - 1);
    }

    /**
     * 發一張蓋著的牌
     */
    public Card dealFaceDown() {
        Card card = deal();
        card.setFaceUp(false);
        return card;
    }

    /**
     * 取得剩餘牌數
     */
    public int remaining() {
        return cards.size();
    }

    /**
     * 重置牌組
     */
    public void reset() {
        initializeDeck();
        shuffle();
    }

    /**
     * 是否為空
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
