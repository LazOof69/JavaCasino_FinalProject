package casino.game.card;

import java.util.ArrayList;
import java.util.List;

/**
 * 手牌類別
 * Hand Class
 */
public class Hand {

    private List<Card> cards;

    public Hand() {
        cards = new ArrayList<>();
    }

    /**
     * 加入一張牌
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * 計算手牌總點數（21點規則）
     * A 可以算 1 或 11
     */
    public int getValue() {
        int value = 0;
        int aces = 0;

        for (Card card : cards) {
            if (card.isFaceUp()) {
                value += card.getValue();
                if (card.isAce()) {
                    aces++;
                }
            }
        }

        // 如果爆牌且有A，則將A從11改為1
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }

        return value;
    }

    /**
     * 計算顯示的點數（包含蓋牌時的估計）
     */
    public int getVisibleValue() {
        int value = 0;
        int aces = 0;

        for (Card card : cards) {
            if (card.isFaceUp()) {
                value += card.getValue();
                if (card.isAce()) {
                    aces++;
                }
            }
        }

        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }

        return value;
    }

    /**
     * 是否爆牌
     */
    public boolean isBusted() {
        return getValue() > 21;
    }

    /**
     * 是否為 Blackjack（兩張牌且21點）
     */
    public boolean isBlackjack() {
        return cards.size() == 2 && getValue() == 21;
    }

    /**
     * 清空手牌
     */
    public void clear() {
        cards.clear();
    }

    /**
     * 取得牌數
     */
    public int size() {
        return cards.size();
    }

    /**
     * 取得所有牌
     */
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * 翻開所有牌
     */
    public void revealAll() {
        for (Card card : cards) {
            card.setFaceUp(true);
        }
    }

    /**
     * 是否有蓋著的牌
     */
    public boolean hasHiddenCard() {
        for (Card card : cards) {
            if (!card.isFaceUp()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(cards.get(i).toString());
        }
        return sb.toString();
    }
}
