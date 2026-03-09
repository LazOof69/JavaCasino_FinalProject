package casino.util;

import java.awt.Color;
import java.awt.Font;

/**
 * 遊戲常數定義
 * Game Constants Definition
 */
public class Constants {

    // 視窗設定
    public static final String GAME_TITLE = "Java Casino";
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;

    // 玩家設定
    public static final int INITIAL_BALANCE = 10000;  // 初始金額
    public static final int MIN_BET = 100;            // 最小下注
    public static final int MAX_BET = 10000;          // 最大下注

    // 顏色主題 (賭場風格)
    public static final Color CASINO_GREEN = new Color(0, 100, 0);      // 賭桌綠
    public static final Color CASINO_RED = new Color(139, 0, 0);        // 深紅色
    public static final Color CASINO_GOLD = new Color(255, 215, 0);     // 金色
    public static final Color CASINO_BLACK = new Color(20, 20, 20);     // 深黑色
    public static final Color CASINO_WHITE = new Color(245, 245, 245);  // 米白色

    // 字體設定
    public static final Font TITLE_FONT = new Font("Microsoft JhengHei", Font.BOLD, 36);
    public static final Font HEADER_FONT = new Font("Microsoft JhengHei", Font.BOLD, 24);
    public static final Font NORMAL_FONT = new Font("Microsoft JhengHei", Font.PLAIN, 16);
    public static final Font BUTTON_FONT = new Font("Microsoft JhengHei", Font.BOLD, 18);

    // 21點設定
    public static final int BLACKJACK_TARGET = 21;
    public static final int DEALER_STAND = 17;  // 莊家停牌點數

    // 老虎機設定
    public static final int SLOT_ROWS = 3;
    public static final int SLOT_COLS = 3;
    public static final String[] SLOT_SYMBOLS = {"7", "BAR", "Cherry", "Lemon", "Bell", "Star"};
    public static final int[] SLOT_PAYOUTS = {100, 50, 20, 10, 15, 25};  // 對應賠率

    // 輪盤設定
    public static final int ROULETTE_MAX_NUMBER = 36;
    public static final int STRAIGHT_UP_PAYOUT = 35;   // 單號賠率
    public static final int COLOR_PAYOUT = 1;          // 紅黑賠率
    public static final int EVEN_ODD_PAYOUT = 1;       // 奇偶賠率

    // 檔案路徑
    public static final String DATA_PATH = "resources/data/";
    public static final String PLAYERS_FILE = DATA_PATH + "players.json";
    public static final String LEADERBOARD_FILE = DATA_PATH + "leaderboard.json";

    // 禁止實例化
    private Constants() {}
}
