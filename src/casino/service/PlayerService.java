package casino.service;

import casino.model.Player;
import casino.util.Constants;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 玩家服務 - 單例模式
 * Player Service - Singleton Pattern
 */
public class PlayerService {

    private static PlayerService instance;
    private Map<String, Player> players;  // username -> Player
    private Player currentPlayer;         // 當前登入的玩家

    private PlayerService() {
        players = new HashMap<>();
        loadPlayers();
    }

    /**
     * 取得單例實例
     */
    public static synchronized PlayerService getInstance() {
        if (instance == null) {
            instance = new PlayerService();
        }
        return instance;
    }

    // ========== 帳號管理 ==========

    /**
     * 註冊新玩家
     */
    public boolean register(String username, String password) {
        // 驗證輸入
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.length() < 4) {
            return false;
        }

        // 檢查是否已存在
        if (players.containsKey(username.toLowerCase())) {
            return false;
        }

        // 建立新玩家
        Player newPlayer = new Player(username, password, Constants.INITIAL_BALANCE);
        players.put(username.toLowerCase(), newPlayer);
        savePlayers();
        return true;
    }

    /**
     * 登入驗證
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) {
            return false;
        }

        Player player = players.get(username.toLowerCase());
        if (player != null && player.getPassword().equals(password)) {
            player.setLastLogin(LocalDateTime.now());
            currentPlayer = player;
            savePlayers();
            return true;
        }
        return false;
    }

    /**
     * 登出
     */
    public void logout() {
        if (currentPlayer != null) {
            savePlayers();
            currentPlayer = null;
        }
    }

    /**
     * 取得當前玩家
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * 檢查用戶名是否存在
     */
    public boolean isUsernameTaken(String username) {
        return players.containsKey(username.toLowerCase());
    }

    // ========== 排行榜 ==========

    /**
     * 依餘額排序的排行榜
     */
    public List<Player> getLeaderboardByBalance() {
        List<Player> leaderboard = new ArrayList<>(players.values());
        leaderboard.sort((a, b) -> Integer.compare(b.getBalance(), a.getBalance()));
        return leaderboard;
    }

    /**
     * 依勝率排序的排行榜
     */
    public List<Player> getLeaderboardByWinRate() {
        List<Player> leaderboard = new ArrayList<>(players.values());
        leaderboard.sort((a, b) -> Double.compare(b.getWinRate(), a.getWinRate()));
        return leaderboard;
    }

    /**
     * 依遊戲次數排序的排行榜
     */
    public List<Player> getLeaderboardByGames() {
        List<Player> leaderboard = new ArrayList<>(players.values());
        leaderboard.sort((a, b) -> Integer.compare(b.getTotalGames(), a.getTotalGames()));
        return leaderboard;
    }

    // ========== 資料持久化 ==========

    /**
     * 儲存玩家資料
     */
    @SuppressWarnings("unchecked")
    public void savePlayers() {
        try {
            File dataDir = new File(Constants.DATA_PATH);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(Constants.PLAYERS_FILE))) {
                oos.writeObject(players);
            }
        } catch (IOException e) {
            System.err.println("Error saving players: " + e.getMessage());
        }
    }

    /**
     * 載入玩家資料
     */
    @SuppressWarnings("unchecked")
    private void loadPlayers() {
        File file = new File(Constants.PLAYERS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file))) {
                players = (Map<String, Player>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading players: " + e.getMessage());
                players = new HashMap<>();
            }
        }
    }

    /**
     * 取得所有玩家數量
     */
    public int getPlayerCount() {
        return players.size();
    }
}
