package casino.gui;

import casino.model.Player;
import casino.service.PlayerService;
import casino.util.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * 主視窗 - 管理所有面板切換
 * Main Frame - Manages all panel switching
 */
public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // 各個面板
    private LoginPanel loginPanel;
    private LobbyPanel lobbyPanel;
    private BlackjackPanel blackjackPanel;
    private SlotMachinePanel slotMachinePanel;
    private RoulettePanel roulettePanel;
    private DiceGamePanel diceGamePanel;
    private LuckyWheelPanel luckyWheelPanel;

    // 面板名稱常數
    public static final String LOGIN_PANEL = "LOGIN";
    public static final String LOBBY_PANEL = "LOBBY";
    public static final String BLACKJACK_PANEL = "BLACKJACK";
    public static final String SLOT_PANEL = "SLOT";
    public static final String ROULETTE_PANEL = "ROULETTE";
    public static final String DICE_PANEL = "DICE";
    public static final String LUCKY_WHEEL_PANEL = "LUCKY_WHEEL";

    public MainFrame() {
        initializeFrame();
        initializePanels();
    }

    /**
     * 初始化視窗設定
     */
    private void initializeFrame() {
        setTitle(Constants.GAME_TITLE);
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  // 視窗置中
        setResizable(false);

        // 設定圖示 (可選)
        // setIconImage(new ImageIcon("resources/images/icons/casino.png").getImage());
    }

    /**
     * 初始化所有面板
     */
    private void initializePanels() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 建立各面板
        loginPanel = new LoginPanel(this);
        lobbyPanel = new LobbyPanel(this);
        blackjackPanel = new BlackjackPanel(this);
        slotMachinePanel = new SlotMachinePanel(this);
        roulettePanel = new RoulettePanel(this);
        diceGamePanel = new DiceGamePanel(this);
        luckyWheelPanel = new LuckyWheelPanel(this);

        // 加入主面板
        mainPanel.add(loginPanel, LOGIN_PANEL);
        mainPanel.add(lobbyPanel, LOBBY_PANEL);
        mainPanel.add(blackjackPanel, BLACKJACK_PANEL);
        mainPanel.add(slotMachinePanel, SLOT_PANEL);
        mainPanel.add(roulettePanel, ROULETTE_PANEL);
        mainPanel.add(diceGamePanel, DICE_PANEL);
        mainPanel.add(luckyWheelPanel, LUCKY_WHEEL_PANEL);

        add(mainPanel);

        // 預設顯示登入面板
        showPanel(LOGIN_PANEL);
    }

    /**
     * 切換到指定面板
     */
    public void showPanel(String panelName) {
        // 如果切換到大廳，更新玩家資訊
        if (LOBBY_PANEL.equals(panelName)) {
            lobbyPanel.updatePlayerInfo();
        }

        cardLayout.show(mainPanel, panelName);
    }

    /**
     * 登入成功後的處理
     */
    public void onLoginSuccess() {
        showPanel(LOBBY_PANEL);
    }

    /**
     * 登出處理
     */
    public void onLogout() {
        PlayerService.getInstance().logout();
        showPanel(LOGIN_PANEL);
    }

    /**
     * 取得當前玩家
     */
    public Player getCurrentPlayer() {
        return PlayerService.getInstance().getCurrentPlayer();
    }

    /**
     * 更新大廳的玩家資訊顯示
     */
    public void refreshLobby() {
        lobbyPanel.updatePlayerInfo();
    }
}
