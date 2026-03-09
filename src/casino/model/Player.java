package casino.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家資料模型
 * Player Data Model
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;  // 實際應用中應加密儲存
    private int balance;
    private int totalGames;
    private int totalWins;
    private int totalLosses;
    private int highestBalance;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private LocalDateTime lastDailyBonus;  // 上次領取每日獎勵的時間
    private List<GameRecord> gameHistory;

    /**
     * 建構子
     */
    public Player(String username, String password, int initialBalance) {
        this.username = username;
        this.password = password;
        this.balance = initialBalance;
        this.totalGames = 0;
        this.totalWins = 0;
        this.totalLosses = 0;
        this.highestBalance = initialBalance;
        this.createdAt = LocalDateTime.now();
        this.lastLogin = LocalDateTime.now();
        this.gameHistory = new ArrayList<>();
    }

    // ========== 餘額操作 ==========

    /**
     * 下注（扣除餘額）
     */
    public boolean placeBet(int amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    /**
     * 贏錢（增加餘額）
     */
    public void addWinnings(int amount) {
        if (amount > 0) {
            balance += amount;
            if (balance > highestBalance) {
                highestBalance = balance;
            }
        }
    }

    /**
     * 記錄遊戲結果
     */
    public void recordGame(String gameType, int betAmount, int winAmount, boolean won) {
        totalGames++;
        if (won) {
            totalWins++;
        } else {
            totalLosses++;
        }

        GameRecord record = new GameRecord(gameType, betAmount, winAmount, won);
        gameHistory.add(record);
    }

    // ========== Getters & Setters ==========

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public int getHighestBalance() {
        return highestBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<GameRecord> getGameHistory() {
        return new ArrayList<>(gameHistory);
    }

    /**
     * 計算勝率
     */
    public double getWinRate() {
        if (totalGames == 0) return 0.0;
        return (double) totalWins / totalGames * 100;
    }

    /**
     * 檢查是否可以領取每日獎勵
     */
    public boolean canClaimDailyBonus() {
        if (lastDailyBonus == null) return true;
        LocalDateTime now = LocalDateTime.now();
        return now.toLocalDate().isAfter(lastDailyBonus.toLocalDate());
    }

    /**
     * 領取每日獎勵
     */
    public int claimDailyBonus() {
        if (canClaimDailyBonus()) {
            lastDailyBonus = LocalDateTime.now();
            int bonus = 1000 + (int)(Math.random() * 2000);  // 1000-3000 隨機獎勵
            addWinnings(bonus);
            return bonus;
        }
        return 0;
    }

    public LocalDateTime getLastDailyBonus() {
        return lastDailyBonus;
    }

    /**
     * 獲取玩家等級 (基於總遊戲次數)
     */
    public int getLevel() {
        if (totalGames < 10) return 1;
        if (totalGames < 30) return 2;
        if (totalGames < 60) return 3;
        if (totalGames < 100) return 4;
        if (totalGames < 150) return 5;
        if (totalGames < 220) return 6;
        if (totalGames < 300) return 7;
        if (totalGames < 400) return 8;
        if (totalGames < 500) return 9;
        return 10;  // Max level
    }

    /**
     * 獲取等級名稱
     */
    public String getLevelTitle() {
        String[] titles = {"Newbie", "Rookie", "Player", "Regular", "Veteran",
                          "Expert", "Master", "Champion", "Legend", "VIP"};
        return titles[getLevel() - 1];
    }

    /**
     * 獲取下一級所需遊戲次數
     */
    public int getGamesForNextLevel() {
        int[] thresholds = {10, 30, 60, 100, 150, 220, 300, 400, 500, Integer.MAX_VALUE};
        int level = getLevel();
        return thresholds[level - 1];
    }

    /**
     * 獲取等級進度 (0.0 - 1.0)
     */
    public double getLevelProgress() {
        int[] thresholds = {0, 10, 30, 60, 100, 150, 220, 300, 400, 500};
        int level = getLevel();
        if (level >= 10) return 1.0;
        int current = totalGames - thresholds[level - 1];
        int needed = thresholds[level] - thresholds[level - 1];
        return (double) current / needed;
    }

    @Override
    public String toString() {
        return String.format("Player{username='%s', balance=%d, games=%d, winRate=%.1f%%}",
                username, balance, totalGames, getWinRate());
    }
}
