package casino.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 遊戲紀錄
 * Game Record
 */
public class GameRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String gameType;
    private int betAmount;
    private int winAmount;
    private boolean won;
    private LocalDateTime playedAt;

    public GameRecord(String gameType, int betAmount, int winAmount, boolean won) {
        this.gameType = gameType;
        this.betAmount = betAmount;
        this.winAmount = winAmount;
        this.won = won;
        this.playedAt = LocalDateTime.now();
    }

    // ========== Getters ==========

    public String getGameType() {
        return gameType;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public int getWinAmount() {
        return winAmount;
    }

    public boolean isWon() {
        return won;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    /**
     * 取得淨損益
     */
    public int getNetProfit() {
        return won ? (winAmount - betAmount) : -betAmount;
    }

    @Override
    public String toString() {
        String result = won ? "Win" : "Lose";
        return String.format("[%s] %s - Bet: $%d, Result: %s, Net: %+d",
                playedAt.format(FORMATTER), gameType, betAmount, result, getNetProfit());
    }
}
