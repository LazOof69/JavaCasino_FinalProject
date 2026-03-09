package casino.gui;

import casino.model.Player;
import casino.service.PlayerService;
import casino.util.AnimationManager;
import casino.util.Constants;
import casino.util.UIHelper;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

/**
 * 老虎機遊戲面板 (帶動畫效果)
 * Slot Machine Game Panel (with animations)
 */
public class SlotMachinePanel extends JPanel {

    private MainFrame mainFrame;
    private Random random;

    // 老虎機符號 (使用純文字避免字體問題)
    private static final String[] SYMBOLS = {"7", "BAR", "X", "O", "A", "W", "$"};
    private static final Color[] SYMBOL_COLORS = {
            new Color(255, 0, 0),      // 7 - 紅
            new Color(255, 140, 0),    // BAR - 橙
            new Color(220, 20, 60),    // X - 紅
            new Color(255, 255, 0),    // O - 黃
            new Color(255, 215, 0),    // A - 金
            new Color(0, 255, 255),    // W - 青
            new Color(185, 242, 255)   // $ - 藍
    };
    private static final int[] PAYOUTS = {100, 50, 20, 10, 15, 25, 200};

    // 累積獎池 (Progressive Jackpot)
    private static int jackpot = 50000;  // 初始獎池
    private static final int JACKPOT_CONTRIBUTION = 5;  // 每次下注貢獻5%到獎池
    private JLabel jackpotLabel;

    // 每個輪子的符號條帶 (模擬真實老虎機)
    // Each reel has its own strip - symbols are indices into SYMBOLS array
    private static final int[][] REEL_STRIPS = {
            // Reel 0 (左輪) - 7和$比較少出現
            {1, 2, 3, 4, 5, 2, 3, 4, 1, 5, 2, 3, 4, 5, 1, 3, 4, 2, 5, 3, 0, 4, 1, 6},
            // Reel 1 (中輪)
            {2, 3, 4, 5, 1, 3, 4, 2, 5, 3, 4, 1, 5, 2, 4, 3, 5, 1, 4, 2, 0, 3, 6, 5},
            // Reel 2 (右輪)
            {3, 4, 5, 1, 2, 4, 5, 3, 1, 4, 2, 5, 3, 4, 1, 2, 5, 4, 3, 1, 0, 5, 2, 6}
    };

    // UI 元件
    private ReelPanel[][] reelPanels;
    private JLabel balanceLabel;
    private JLabel messageLabel;
    private JLabel lastWinLabel;
    private JButton spinButton;
    private JSpinner betSpinner;
    private boolean spinning = false;

    public SlotMachinePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.random = new Random();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 0, 50));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createSlotMachine(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(139, 0, 0),
                        getWidth(), 0, new Color(80, 0, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = UIHelper.createSecondaryButton("<< Back");
        backButton.setPreferredSize(new Dimension(100, 35));
        backButton.addActionListener(e -> mainFrame.showPanel(MainFrame.LOBBY_PANEL));

        // 中間區域：標題和獎池
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("*** LUCKY SLOTS ***", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Constants.CASINO_GOLD);

        // 獎池顯示
        JPanel jackpotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        jackpotPanel.setOpaque(false);

        JLabel jackpotTitle = new JLabel("JACKPOT: ");
        jackpotTitle.setFont(new Font("Arial", Font.BOLD, 18));
        jackpotTitle.setForeground(new Color(255, 100, 100));

        jackpotLabel = new JLabel("$" + String.format("%,d", jackpot));
        jackpotLabel.setFont(new Font("Arial", Font.BOLD, 22));
        jackpotLabel.setForeground(new Color(255, 215, 0));

        jackpotPanel.add(jackpotTitle);
        jackpotPanel.add(jackpotLabel);

        centerPanel.add(titleLabel);
        centerPanel.add(jackpotPanel);

        balanceLabel = new JLabel("$10,000");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 22));
        balanceLabel.setForeground(Constants.CASINO_GOLD);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(balanceLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createSlotMachine() {
        JPanel machinePanel = new JPanel(new BorderLayout(20, 0));
        machinePanel.setOpaque(false);

        // 老虎機外殼
        JPanel slotFrame = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 金屬外框
                GradientPaint metalGradient = new GradientPaint(0, 0, new Color(80, 80, 80),
                        getWidth(), getHeight(), new Color(40, 40, 40));
                g2.setPaint(metalGradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));

                // 內部深色背景
                g2.setColor(new Color(20, 20, 30));
                g2.fill(new RoundRectangle2D.Double(15, 15, getWidth() - 30, getHeight() - 30, 20, 20));

                // 光澤效果
                g2.setColor(new Color(255, 255, 255, 30));
                g2.fillRect(20, 20, getWidth() - 40, 5);

                g2.dispose();
            }
        };
        slotFrame.setOpaque(false);
        slotFrame.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 3x3 轉盤
        JPanel reelsContainer = new JPanel(new GridLayout(3, 3, 8, 8));
        reelsContainer.setOpaque(false);

        reelPanels = new ReelPanel[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                reelPanels[row][col] = new ReelPanel();
                reelsContainer.add(reelPanels[row][col]);
            }
        }

        // 中線指示器
        JPanel lineIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        0, new float[]{10, 5}, 0));
                int y = getHeight() / 2;
                g2.drawLine(10, y, getWidth() - 10, y);

                // 箭頭
                g2.setStroke(new BasicStroke(3));
                g2.fillPolygon(new int[]{0, 15, 15}, new int[]{y, y - 10, y + 10}, 3);
                g2.fillPolygon(new int[]{getWidth(), getWidth() - 15, getWidth() - 15},
                        new int[]{y, y - 10, y + 10}, 3);
                g2.dispose();
            }
        };
        lineIndicator.setOpaque(false);
        lineIndicator.setPreferredSize(new Dimension(30, 100));

        slotFrame.add(reelsContainer, BorderLayout.CENTER);

        // 賠率表
        JPanel payoutPanel = createPayoutTable();

        machinePanel.add(payoutPanel, BorderLayout.WEST);
        machinePanel.add(slotFrame, BorderLayout.CENTER);

        return machinePanel;
    }

    private JPanel createPayoutTable() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 50));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));
                g2.dispose();
            }
        };
        panel.setLayout(new GridLayout(SYMBOLS.length + 1, 1, 5, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(180, 300));
        panel.setOpaque(false);

        JLabel title = new JLabel("$ PAYOUTS $", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Constants.CASINO_GOLD);
        panel.add(title);

        for (int i = 0; i < SYMBOLS.length; i++) {
            JLabel row = new JLabel(SYMBOLS[i] + " × 3  =  " + PAYOUTS[i] + "×", SwingConstants.CENTER);
            row.setFont(new Font("Arial", Font.BOLD, 14));
            row.setForeground(SYMBOL_COLORS[i]);
            panel.add(row);
        }

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(60, 0, 0),
                        0, getHeight(), new Color(30, 0, 0));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 訊息區
        JPanel msgPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        msgPanel.setOpaque(false);

        messageLabel = new JLabel("Place your bet and SPIN!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 22));
        messageLabel.setForeground(Constants.CASINO_GOLD);

        lastWinLabel = new JLabel(" ", SwingConstants.CENTER);
        lastWinLabel.setFont(new Font("Arial", Font.BOLD, 18));
        lastWinLabel.setForeground(Color.WHITE);

        msgPanel.add(messageLabel);
        msgPanel.add(lastWinLabel);

        // 控制區
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlRow.setOpaque(false);

        JLabel betLabel = new JLabel("BET:");
        betLabel.setFont(new Font("Arial", Font.BOLD, 18));
        betLabel.setForeground(Constants.CASINO_WHITE);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 100, 5000, 100);
        betSpinner = new JSpinner(spinnerModel);
        betSpinner.setFont(new Font("Arial", Font.BOLD, 16));
        ((JSpinner.DefaultEditor) betSpinner.getEditor()).getTextField().setColumns(5);

        // 快速下注按鈕
        JButton minBtn = createQuickBetButton("MIN", 100);
        JButton halfBtn = createQuickBetButton("1/2", -1);  // -1 表示半數餘額
        JButton doubleBtn = createQuickBetButton("x2", -2);  // -2 表示加倍
        JButton maxBtn = createQuickBetButton("MAX", 5000);

        // 大型 SPIN 按鈕
        spinButton = new JButton("SPIN!") {
            private float glowPhase = 0;
            private Timer glowTimer;

            {
                glowTimer = new Timer(50, e -> {
                    glowPhase += 0.1f;
                    repaint();
                });
                glowTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 發光效果
                float glow = (float) (Math.sin(glowPhase) * 0.3 + 0.7);
                Color baseColor = spinning ? Color.GRAY : new Color(
                        (int) (255 * glow), (int) (50 * glow), 0);

                // 陰影
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fill(new RoundRectangle2D.Double(4, 4, w - 4, h - 4, 20, 20));

                // 按鈕本體
                GradientPaint gp = new GradientPaint(0, 0, baseColor.brighter(),
                        0, h, baseColor.darker());
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 4, h - 4, 20, 20));

                // 高光
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRect(10, 5, w - 24, h / 4);

                // 文字
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(getText())) / 2;
                int y = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        spinButton.setFont(new Font("Arial", Font.BOLD, 28));
        spinButton.setPreferredSize(new Dimension(180, 60));
        spinButton.setContentAreaFilled(false);
        spinButton.setBorderPainted(false);
        spinButton.setFocusPainted(false);
        spinButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        spinButton.addActionListener(e -> spin());

        controlRow.add(betLabel);
        controlRow.add(minBtn);
        controlRow.add(halfBtn);
        controlRow.add(betSpinner);
        controlRow.add(doubleBtn);
        controlRow.add(maxBtn);
        controlRow.add(spinButton);

        panel.add(msgPanel, BorderLayout.NORTH);
        panel.add(controlRow, BorderLayout.CENTER);

        return panel;
    }

    private JButton createQuickBetButton(String text, int value) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(50, 30));
        btn.setBackground(new Color(60, 60, 80));
        btn.setForeground(Constants.CASINO_GOLD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Constants.CASINO_GOLD, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            Player player = mainFrame.getCurrentPlayer();
            if (player == null) return;

            int newBet;
            if (value == -1) {
                // Half balance
                newBet = Math.max(100, (player.getBalance() / 2 / 100) * 100);
            } else if (value == -2) {
                // Double current bet
                newBet = Math.min(5000, (int) betSpinner.getValue() * 2);
            } else {
                newBet = Math.min(value, player.getBalance());
            }
            newBet = Math.min(newBet, 5000);
            newBet = Math.max(newBet, 100);
            betSpinner.setValue(newBet);
        });

        return btn;
    }

    // ========== 遊戲邏輯 ==========

    private void spin() {
        if (spinning) return;

        Player player = mainFrame.getCurrentPlayer();
        int betAmount = (int) betSpinner.getValue();

        if (player.getBalance() < betAmount) {
            messageLabel.setText("Insufficient balance!");
            messageLabel.setForeground(Color.RED);
            AnimationManager.shake(messageLabel, null);
            return;
        }

        player.placeBet(betAmount);
        updateBalanceDisplay();

        // 貢獻到獎池 (5% of bet)
        int contribution = betAmount * JACKPOT_CONTRIBUTION / 100;
        jackpot += contribution;
        updateJackpotDisplay();

        spinning = true;
        spinButton.setEnabled(false);
        betSpinner.setEnabled(false);
        messageLabel.setText("Spinning...");
        messageLabel.setForeground(Color.WHITE);
        lastWinLabel.setText(" ");

        // 決定最終結果 - 每個輪子(列)獨立隨機選擇停止位置
        // Each reel (column) independently picks a random stop position on its strip
        int[][] finalResults = new int[3][3];
        for (int col = 0; col < 3; col++) {
            int[] strip = REEL_STRIPS[col];
            int stopPosition = random.nextInt(strip.length);
            // 顯示連續3個符號 (上、中、下)
            for (int row = 0; row < 3; row++) {
                finalResults[row][col] = strip[(stopPosition + row) % strip.length];
            }
        }

        // 依序停止每列
        spinReels(finalResults, betAmount);
    }

    private void spinReels(int[][] finalResults, int betAmount) {
        // 每列開始轉動
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                reelPanels[row][col].startSpin();
            }
        }

        // 依序停止每列
        Timer stopTimer = new Timer(600, null);
        final int[] currentCol = {0};

        stopTimer.addActionListener(e -> {
            int col = currentCol[0];

            // 停止這一列的所有轉盤
            for (int row = 0; row < 3; row++) {
                int symbolIndex = finalResults[row][col];
                reelPanels[row][col].stopSpin(symbolIndex, () -> {});
            }

            currentCol[0]++;

            if (currentCol[0] >= 3) {
                stopTimer.stop();
                // 所有列都停止後檢查結果
                AnimationManager.delay(300, () -> checkWinnings(finalResults, betAmount));
            }
        });

        stopTimer.setInitialDelay(800);
        stopTimer.start();
    }

    private void checkWinnings(int[][] results, int betAmount) {
        int totalWin = 0;
        java.util.List<int[]> winningRows = new ArrayList<>();
        boolean jackpotWon = false;

        // 檢查橫排
        for (int row = 0; row < 3; row++) {
            if (results[row][0] == results[row][1] && results[row][1] == results[row][2]) {
                int symbolIdx = results[row][0];
                totalWin += betAmount * PAYOUTS[symbolIdx];
                winningRows.add(new int[]{row, 0});
                winningRows.add(new int[]{row, 1});
                winningRows.add(new int[]{row, 2});

                // 檢查是否贏得獎池 (中間排出現 $$$ - 符號索引6)
                if (row == 1 && symbolIdx == 6) {
                    jackpotWon = true;
                }
            }
        }

        // 檢查對角線
        if (results[0][0] == results[1][1] && results[1][1] == results[2][2]) {
            int symbolIdx = results[1][1];
            totalWin += betAmount * PAYOUTS[symbolIdx];
            winningRows.add(new int[]{0, 0});
            winningRows.add(new int[]{1, 1});
            winningRows.add(new int[]{2, 2});
        }
        if (results[0][2] == results[1][1] && results[1][1] == results[2][0]) {
            int symbolIdx = results[1][1];
            totalWin += betAmount * PAYOUTS[symbolIdx];
            winningRows.add(new int[]{0, 2});
            winningRows.add(new int[]{1, 1});
            winningRows.add(new int[]{2, 0});
        }

        // 中間排有兩個相同
        if (totalWin == 0) {
            if (results[1][0] == results[1][1] || results[1][1] == results[1][2]) {
                totalWin = betAmount / 2;
            }
        }

        // 處理結果
        Player player = mainFrame.getCurrentPlayer();
        boolean won = totalWin > 0;

        // 獎池處理
        if (jackpotWon) {
            totalWin += jackpot;
            int wonJackpot = jackpot;
            jackpot = 50000;  // 重設獎池
            updateJackpotDisplay();

            player.addWinnings(totalWin);

            // 閃爍所有格子
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    reelPanels[row][col].flashWin();
                }
            }

            messageLabel.setText("!!! JACKPOT !!!");
            messageLabel.setForeground(new Color(255, 50, 50));
            AnimationManager.pulse(messageLabel, 5, null);

            lastWinLabel.setText("JACKPOT: $" + String.format("%,d", wonJackpot) + " + $" + String.format("%,d", totalWin - wonJackpot));
            lastWinLabel.setForeground(new Color(255, 215, 0));

            // 餘額滾動動畫
            AnimationManager.countUp(balanceLabel, player.getBalance() - totalWin,
                    player.getBalance(), 1500, "$", "");
        } else if (won) {
            player.addWinnings(totalWin);

            // 閃爍贏錢的格子
            for (int[] pos : winningRows) {
                reelPanels[pos[0]][pos[1]].flashWin();
            }

            messageLabel.setText("*** WINNER! ***");
            messageLabel.setForeground(Constants.CASINO_GOLD);
            AnimationManager.pulse(messageLabel, 3, null);

            lastWinLabel.setText("Won: $" + String.format("%,d", totalWin));
            lastWinLabel.setForeground(Constants.CASINO_GOLD);

            // 餘額滾動動畫
            AnimationManager.countUp(balanceLabel, player.getBalance() - totalWin,
                    player.getBalance(), 800, "$", "");
        } else {
            messageLabel.setText("No luck this time...");
            messageLabel.setForeground(Color.GRAY);
            lastWinLabel.setText("Try again!");
            lastWinLabel.setForeground(Color.GRAY);
            updateBalanceDisplay();
        }

        player.recordGame("Slot Machine", betAmount, totalWin, won);
        PlayerService.getInstance().savePlayers();

        spinning = false;
        spinButton.setEnabled(true);
        betSpinner.setEnabled(true);
    }

    private void updateJackpotDisplay() {
        if (jackpotLabel != null) {
            jackpotLabel.setText("$" + String.format("%,d", jackpot));
        }
    }

    private void updateBalanceDisplay() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null) {
            balanceLabel.setText("$" + String.format("%,d", player.getBalance()));
        }
    }

    // ========== 內部類：轉盤格子 ==========

    private class ReelPanel extends JPanel {
        private int symbolIndex;
        private boolean isSpinning = false;
        private Timer spinTimer;
        private Timer flashTimer;
        private boolean flashing = false;

        public ReelPanel() {
            setPreferredSize(new Dimension(100, 100));
            setOpaque(false);
            // 隨機初始符號，避免一開始全部都是 7
            symbolIndex = random.nextInt(SYMBOLS.length);
        }

        public void startSpin() {
            isSpinning = true;
            spinTimer = new Timer(50, e -> {
                symbolIndex = random.nextInt(SYMBOLS.length);
                repaint();
            });
            spinTimer.start();
        }

        public void stopSpin(int finalSymbol, Runnable onComplete) {
            if (spinTimer != null) {
                spinTimer.stop();
            }
            symbolIndex = finalSymbol;
            isSpinning = false;

            // 停止時的彈跳效果
            AnimationManager.bounce(this, onComplete);
            repaint();
        }

        public void flashWin() {
            flashing = true;
            final int[] count = {0};
            flashTimer = new Timer(150, e -> {
                count[0]++;
                repaint();
                if (count[0] > 6) {
                    flashTimer.stop();
                    flashing = false;
                    repaint();
                }
            });
            flashTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 背景
            if (flashing) {
                g2.setColor(new Color(255, 255, 0, 150));
            } else {
                g2.setColor(new Color(10, 10, 20));
            }
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 15, 15));

            // 邊框
            g2.setColor(flashing ? Constants.CASINO_GOLD : new Color(60, 60, 80));
            g2.setStroke(new BasicStroke(3));
            g2.draw(new RoundRectangle2D.Double(2, 2, w - 4, h - 4, 12, 12));

            // 內部光暈
            GradientPaint innerGlow = new GradientPaint(0, 0, new Color(255, 255, 255, 20),
                    0, h, new Color(0, 0, 0, 0));
            g2.setPaint(innerGlow);
            g2.fillRect(5, 5, w - 10, h / 3);

            // 符號
            g2.setColor(SYMBOL_COLORS[symbolIndex]);
            g2.setFont(new Font("Arial", Font.BOLD, 42));
            FontMetrics fm = g2.getFontMetrics();
            String symbol = SYMBOLS[symbolIndex];
            int x = (w - fm.stringWidth(symbol)) / 2;
            int y = (h + fm.getAscent() - fm.getDescent()) / 2;

            // 文字陰影
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawString(symbol, x + 2, y + 2);

            // 文字本體
            g2.setColor(SYMBOL_COLORS[symbolIndex]);
            g2.drawString(symbol, x, y);

            g2.dispose();
        }
    }
}
