package casino.game.card;

/**
 * 撲克牌類別
 * Playing Card Class
 */
public class Card {

    public enum Suit {
        HEARTS("Hearts", "♥"),
        DIAMONDS("Diamonds", "♦"),
        CLUBS("Clubs", "♣"),
        SPADES("Spades", "♠");

        private final String name;
        private final String symbol;

        Suit(String name, String symbol) {
            this.name = name;
            this.symbol = symbol;
        }

        public String getName() { return name; }
        public String getSymbol() { return symbol; }
    }

    public enum Rank {
        ACE("A", 11),
        TWO("2", 2),
        THREE("3", 3),
        FOUR("4", 4),
        FIVE("5", 5),
        SIX("6", 6),
        SEVEN("7", 7),
        EIGHT("8", 8),
        NINE("9", 9),
        TEN("10", 10),
        JACK("J", 10),
        QUEEN("Q", 10),
        KING("K", 10);

        private final String symbol;
        private final int value;

        Rank(String symbol, int value) {
            this.symbol = symbol;
            this.value = value;
        }

        public String getSymbol() { return symbol; }
        public int getValue() { return value; }
    }

    private final Suit suit;
    private final Rank rank;
    private boolean faceUp;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.faceUp = true;
    }

    public Suit getSuit() { return suit; }
    public Rank getRank() { return rank; }

    public int getValue() {
        return rank.getValue();
    }

    public boolean isFaceUp() { return faceUp; }
    public void setFaceUp(boolean faceUp) { this.faceUp = faceUp; }

    public boolean isAce() {
        return rank == Rank.ACE;
    }

    /**
     * 判斷是否為紅色牌（紅心、方塊）
     */
    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }

    @Override
    public String toString() {
        if (!faceUp) {
            return "[Hidden]";
        }
        return rank.getSymbol() + suit.getSymbol();
    }

    /**
     * 取得完整名稱
     */
    public String getFullName() {
        return rank.getSymbol() + " of " + suit.getName();
    }
}
