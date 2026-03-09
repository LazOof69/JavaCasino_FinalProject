package casino.gui;

import casino.model.Player;
import casino.util.AnimationManager;
import casino.util.Constants;
import casino.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 遊戲大廳面板 (優化版)
 * Game Lobby Panel (Enhanced)
 */
public class LobbyPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel welcomeLabel;
    private JLabel balanceLabel;
    private JLabel statsLabel;
    private JLabel levelLabel;
    private JProgressBar levelProgressBar;

    public LobbyPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(15, 15, 25));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createGamesPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 80, 0),
                        getWidth(), 0, new Color(0, 50, 0));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

                // 金邊
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(3));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // 左側：歡迎訊息和等級
        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 0, 3));
        leftPanel.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, Player!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setForeground(Constants.CASINO_WHITE);

        // 等級顯示
        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        levelPanel.setOpaque(false);

        levelLabel = new JLabel("Lv.1 Newbie");
        levelLabel.setFont(new Font("Arial", Font.BOLD, 14));
        levelLabel.setForeground(Constants.CASINO_GOLD);

        levelProgressBar = new JProgressBar(0, 100);
        levelProgressBar.setValue(0);
        levelProgressBar.setPreferredSize(new Dimension(120, 12));
        levelProgressBar.setStringPainted(false);
        levelProgressBar.setBackground(new Color(40, 40, 50));
        levelProgressBar.setForeground(new Color(50, 205, 50));
        levelProgressBar.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 90), 1));

        levelPanel.add(levelLabel);
        levelPanel.add(levelProgressBar);

        statsLabel = new JLabel("Games: 0 | Wins: 0 | Win Rate: 0%");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(180, 180, 180));

        leftPanel.add(welcomeLabel);
        leftPanel.add(levelPanel);
        leftPanel.add(statsLabel);

        // 右側：餘額顯示
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JLabel coinIcon = new JLabel("");
        coinIcon.setFont(new Font("Arial", Font.BOLD, 36));
        coinIcon.setForeground(Constants.CASINO_GOLD);

        balanceLabel = new JLabel("$10,000");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 36));
        balanceLabel.setForeground(Constants.CASINO_GOLD);

        rightPanel.add(coinIcon);
        rightPanel.add(balanceLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createGamesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 21點
        panel.add(createGameCard(
                "BLACKJACK",
                "21",
                "Classic 21 card game",
                "Beat the dealer!",
                new Color(139, 0, 0),
                new Color(80, 0, 0),
                MainFrame.BLACKJACK_PANEL
        ));

        // 老虎機
        panel.add(createGameCard(
                "SLOTS",
                "777",
                "Lucky spinning reels",
                "Jackpot waiting!",
                new Color(180, 140, 0),
                new Color(120, 90, 0),
                MainFrame.SLOT_PANEL
        ));

        // 輪盤
        panel.add(createGameCard(
                "ROULETTE",
                "O",
                "European roulette",
                "Pick your lucky number!",
                new Color(0, 100, 0),
                new Color(0, 60, 0),
                MainFrame.ROULETTE_PANEL
        ));

        // 骰子遊戲
        panel.add(createGameCard(
                "DICE",
                "##",
                "Roll the dice",
                "Bet on the outcome!",
                new Color(100, 50, 150),
                new Color(60, 30, 90),
                MainFrame.DICE_PANEL
        ));

        // 幸運轉盤
        panel.add(createGameCard(
                "LUCKY WHEEL",
                "*",
                "Spin to win prizes",
                "Free daily spin!",
                new Color(200, 50, 100),
                new Color(120, 30, 60),
                MainFrame.LUCKY_WHEEL_PANEL
        ));

        // 空白佔位 (保持對稱)
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        panel.add(placeholder);

        return panel;
    }

    private JPanel createGameCard(String title, String icon, String desc1, String desc2,
                                   Color color1, Color color2, String panelName) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            private boolean hovered = false;
            private float hoverProgress = 0f;
            private Timer hoverTimer;

            {
                hoverTimer = new Timer(16, e -> {
                    if (hovered && hoverProgress < 1f) {
                        hoverProgress = Math.min(1f, hoverProgress + 0.1f);
                        repaint();
                    } else if (!hovered && hoverProgress > 0f) {
                        hoverProgress = Math.max(0f, hoverProgress - 0.1f);
                        repaint();
                    } else if ((hovered && hoverProgress >= 1f) || (!hovered && hoverProgress <= 0f)) {
                        hoverTimer.stop();
                    }
                });

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovered = true;
                        hoverTimer.start();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovered = false;
                        hoverTimer.start();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 陰影
                int shadowOffset = (int) (5 + hoverProgress * 5);
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fill(new RoundRectangle2D.Double(shadowOffset, shadowOffset, w - shadowOffset, h - shadowOffset, 25, 25));

                // 卡片背景
                Color currentColor1 = hovered ? color1.brighter() : color1;
                Color currentColor2 = hovered ? color2.brighter() : color2;
                GradientPaint cardGradient = new GradientPaint(0, 0, currentColor1, 0, h, currentColor2);
                g2.setPaint(cardGradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 5, h - 5, 25, 25));

                // 頂部高光
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRect(10, 5, w - 25, 3);

                // 邊框
                float borderAlpha = 0.5f + hoverProgress * 0.5f;
                g2.setColor(new Color(255, 215, 0, (int) (borderAlpha * 255)));
                g2.setStroke(new BasicStroke(2 + hoverProgress));
                g2.draw(new RoundRectangle2D.Double(1, 1, w - 7, h - 7, 25, 25));

                g2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 圖示
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 72));

        // 標題
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Constants.CASINO_WHITE);

        // 描述
        JPanel descPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        descPanel.setOpaque(false);

        JLabel desc1Label = new JLabel(desc1, SwingConstants.CENTER);
        desc1Label.setFont(new Font("Arial", Font.PLAIN, 14));
        desc1Label.setForeground(new Color(220, 220, 220));

        JLabel desc2Label = new JLabel(desc2, SwingConstants.CENTER);
        desc2Label.setFont(new Font("Arial", Font.ITALIC, 12));
        desc2Label.setForeground(new Color(180, 180, 180));

        descPanel.add(desc1Label);
        descPanel.add(desc2Label);

        // PLAY 按鈕
        JButton playButton = new JButton("PLAY NOW") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color btnColor = getModel().isRollover() ? Constants.CASINO_GOLD.brighter() :
                        getModel().isPressed() ? Constants.CASINO_GOLD.darker() : Constants.CASINO_GOLD;

                g2.setColor(btnColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

                g2.setColor(Color.BLACK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        playButton.setFont(new Font("Arial", Font.BOLD, 16));
        playButton.setPreferredSize(new Dimension(160, 45));
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setFocusPainted(false);
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playButton.addActionListener(e -> {
            AnimationManager.bounce(card, () -> mainFrame.showPanel(panelName));
        });

        // 組裝
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(descPanel, BorderLayout.SOUTH);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setOpaque(false);
        buttonWrapper.add(playButton);

        card.add(contentPanel, BorderLayout.CENTER);
        card.add(buttonWrapper, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);

        JButton dailyBonusBtn = UIHelper.createPrimaryButton("Daily Bonus");
        dailyBonusBtn.addActionListener(e -> claimDailyBonus());

        JButton addMoneyBtn = UIHelper.createSuccessButton("Add Chips");
        addMoneyBtn.addActionListener(e -> addChips());

        JButton historyBtn = UIHelper.createSecondaryButton("History");
        historyBtn.addActionListener(e -> showHistory());

        JButton logoutBtn = UIHelper.createDangerButton("Logout");
        logoutBtn.addActionListener(e -> mainFrame.onLogout());

        panel.add(dailyBonusBtn);
        panel.add(addMoneyBtn);
        panel.add(historyBtn);
        panel.add(logoutBtn);

        return panel;
    }

    public void updatePlayerInfo() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null) {
            welcomeLabel.setText("Welcome, " + player.getUsername() + "!");

            // 餘額動畫
            String currentText = balanceLabel.getText().replace("$", "").replace(",", "");
            try {
                int currentBalance = Integer.parseInt(currentText);
                if (currentBalance != player.getBalance()) {
                    AnimationManager.countUp(balanceLabel, currentBalance, player.getBalance(), 500, "$", "");
                }
            } catch (NumberFormatException e) {
                balanceLabel.setText("$" + String.format("%,d", player.getBalance()));
            }

            // 更新等級顯示
            levelLabel.setText("Lv." + player.getLevel() + " " + player.getLevelTitle());
            int progressPercent = (int) (player.getLevelProgress() * 100);
            levelProgressBar.setValue(progressPercent);

            // 根據等級改變顏色
            Color levelColor = getLevelColor(player.getLevel());
            levelLabel.setForeground(levelColor);
            levelProgressBar.setForeground(levelColor);

            statsLabel.setText(String.format("Games: %d | Wins: %d | Win Rate: %.1f%%",
                    player.getTotalGames(), player.getTotalWins(), player.getWinRate()));
        }
    }

    private Color getLevelColor(int level) {
        switch (level) {
            case 1: return new Color(150, 150, 150);  // 灰
            case 2: return new Color(100, 200, 100);  // 綠
            case 3: return new Color(100, 150, 255);  // 藍
            case 4: return new Color(180, 100, 255);  // 紫
            case 5: return new Color(255, 150, 50);   // 橙
            case 6: return new Color(255, 100, 100);  // 紅
            case 7: return new Color(255, 50, 150);   // 粉紅
            case 8: return new Color(50, 255, 255);   // 青
            case 9: return new Color(255, 215, 0);    // 金
            case 10: return new Color(255, 255, 255); // 白(VIP)
            default: return Constants.CASINO_GOLD;
        }
    }

    private void claimDailyBonus() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null) {
            if (player.canClaimDailyBonus()) {
                int oldBalance = player.getBalance();
                int bonus = player.claimDailyBonus();
                casino.service.PlayerService.getInstance().savePlayers();

                AnimationManager.countUp(balanceLabel, oldBalance, player.getBalance(), 800, "$", "");

                JOptionPane.showMessageDialog(this,
                        "You received $" + String.format("%,d", bonus) + " daily bonus!",
                        "Daily Bonus",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "You already claimed today's bonus!\nCome back tomorrow.",
                        "Daily Bonus",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void addChips() {
        String[] options = {"$1,000", "$5,000", "$10,000"};
        int choice = JOptionPane.showOptionDialog(this,
                "Select amount to add:",
                "Add Chips",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice >= 0) {
            int[] amounts = {1000, 5000, 10000};
            Player player = mainFrame.getCurrentPlayer();
            if (player != null) {
                int oldBalance = player.getBalance();
                player.addWinnings(amounts[choice]);
                AnimationManager.countUp(balanceLabel, oldBalance, player.getBalance(), 800, "$", "");
                JOptionPane.showMessageDialog(this,
                        "Added " + options[choice] + " chips!",
                        "Success ✓",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void showHistory() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null && !player.getGameHistory().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════ Recent Games ═══════════\n\n");

            var history = player.getGameHistory();
            int start = Math.max(0, history.size() - 10);
            for (int i = history.size() - 1; i >= start; i--) {
                var record = history.get(i);
                String icon = record.isWon() ? "✓" : "✗";
                sb.append(String.format("%s %s\n", icon, record.toString()));
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setBackground(new Color(30, 30, 40));
            textArea.setForeground(Color.WHITE);
            textArea.setCaretColor(Color.WHITE);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(550, 350));
            scrollPane.setBorder(BorderFactory.createLineBorder(Constants.CASINO_GOLD, 2));

            JOptionPane.showMessageDialog(this, scrollPane, "Game History", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No game history yet!\nStart playing to see your records.",
                    "History",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
