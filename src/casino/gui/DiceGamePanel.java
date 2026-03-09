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
 * 骰子遊戲面板
 * Dice Game Panel
 */
public class DiceGamePanel extends JPanel {

    private MainFrame mainFrame;
    private Random random;

    // 骰子值
    private int dice1Value = 1;
    private int dice2Value = 1;
    private boolean rolling = false;

    // UI 元件
    private JLabel balanceLabel;
    private JLabel messageLabel;
    private DicePanel dice1Panel;
    private DicePanel dice2Panel;
    private JSpinner betSpinner;
    private JButton rollButton;
    private ButtonGroup betTypeGroup;
    private String selectedBetType = "OVER7";
    private JLabel selectedBetLabel;

    // 賠率
    private static final Map<String, Double> PAYOUTS = new HashMap<>();
    static {
        PAYOUTS.put("OVER7", 2.0);      // 總和 > 7
        PAYOUTS.put("UNDER7", 2.0);     // 總和 < 7
        PAYOUTS.put("EXACT7", 5.0);     // 總和 = 7
        PAYOUTS.put("DOUBLES", 6.0);    // 雙骰 (兩個一樣)
        PAYOUTS.put("ODD", 2.0);        // 奇數
        PAYOUTS.put("EVEN", 2.0);       // 偶數
        PAYOUTS.put("HIGH", 3.0);       // 高點 (10-12)
        PAYOUTS.put("LOW", 3.0);        // 低點 (2-4)
    }

    public DiceGamePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.random = new Random();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 60, 20));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createGameArea(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(10, 40, 10));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = UIHelper.createSecondaryButton("<< Back");
        backButton.setPreferredSize(new Dimension(100, 35));
        backButton.addActionListener(e -> mainFrame.showPanel(MainFrame.LOBBY_PANEL));

        JLabel titleLabel = new JLabel("DICE GAME", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Constants.CASINO_GOLD);

        balanceLabel = new JLabel("$10,000");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 22));
        balanceLabel.setForeground(Constants.CASINO_GOLD);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(balanceLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createGameArea() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);

        // 左側：下注選項
        JPanel betOptionsPanel = createBetOptionsPanel();

        // 中央：骰子顯示
        JPanel diceArea = createDiceArea();

        // 右側：賠率表
        JPanel payoutPanel = createPayoutPanel();

        panel.add(betOptionsPanel, BorderLayout.WEST);
        panel.add(diceArea, BorderLayout.CENTER);
        panel.add(payoutPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBetOptionsPanel() {
        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(180, 300));
        panel.setOpaque(false);

        JLabel title = new JLabel("BET TYPE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Constants.CASINO_GOLD);
        panel.add(title);

        betTypeGroup = new ButtonGroup();
        String[][] betOptions = {
                {"OVER7", "Over 7 (2x)"},
                {"UNDER7", "Under 7 (2x)"},
                {"EXACT7", "Exact 7 (5x)"},
                {"DOUBLES", "Doubles (6x)"},
                {"ODD", "Odd Total (2x)"},
                {"EVEN", "Even Total (2x)"},
                {"HIGH", "High 10-12 (3x)"}
        };

        for (String[] opt : betOptions) {
            JRadioButton btn = new JRadioButton(opt[1]);
            btn.setActionCommand(opt[0]);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setOpaque(false);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                selectedBetType = e.getActionCommand();
                updateSelectedBetLabel();
            });
            if (opt[0].equals("OVER7")) btn.setSelected(true);
            betTypeGroup.add(btn);
            panel.add(btn);
        }

        return panel;
    }

    private JPanel createDiceArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 80, 0));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(new Color(139, 69, 19));
                g2.setStroke(new BasicStroke(5));
                g2.draw(new RoundRectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4, 20, 20));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // 骰子面板
        JPanel diceContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        diceContainer.setOpaque(false);

        dice1Panel = new DicePanel();
        dice2Panel = new DicePanel();

        diceContainer.add(dice1Panel);
        diceContainer.add(dice2Panel);

        // 結果顯示
        JPanel resultPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        resultPanel.setOpaque(false);

        selectedBetLabel = new JLabel("Bet: Over 7", SwingConstants.CENTER);
        selectedBetLabel.setFont(new Font("Arial", Font.BOLD, 18));
        selectedBetLabel.setForeground(Color.WHITE);

        messageLabel = new JLabel("Roll the dice!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setForeground(Constants.CASINO_GOLD);

        resultPanel.add(selectedBetLabel);
        resultPanel.add(messageLabel);

        panel.add(diceContainer, BorderLayout.CENTER);
        panel.add(resultPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPayoutPanel() {
        JPanel panel = new JPanel(new GridLayout(9, 1, 5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 15, 15));
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(160, 300));
        panel.setOpaque(false);

        JLabel title = new JLabel("PAYOUTS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Constants.CASINO_GOLD);
        panel.add(title);

        String[][] payouts = {
                {"Over 7", "2x"},
                {"Under 7", "2x"},
                {"Exact 7", "5x"},
                {"Doubles", "6x"},
                {"Odd", "2x"},
                {"Even", "2x"},
                {"High 10-12", "3x"},
                {"Low 2-4", "3x"}
        };

        for (String[] p : payouts) {
            JLabel label = new JLabel(p[0] + ": " + p[1], SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 12));
            label.setForeground(new Color(200, 200, 200));
            panel.add(label);
        }

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(10, 40, 10));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel betLabel = new JLabel("BET:");
        betLabel.setFont(new Font("Arial", Font.BOLD, 18));
        betLabel.setForeground(Constants.CASINO_WHITE);

        // 快速下注按鈕
        JButton minBtn = createQuickBetButton("MIN", 50);
        JButton halfBtn = createQuickBetButton("1/2", -1);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 50, 5000, 50);
        betSpinner = new JSpinner(spinnerModel);
        betSpinner.setFont(new Font("Arial", Font.BOLD, 16));
        ((JSpinner.DefaultEditor) betSpinner.getEditor()).getTextField().setColumns(5);

        JButton doubleBtn = createQuickBetButton("x2", -2);
        JButton maxBtn = createQuickBetButton("MAX", 5000);

        rollButton = UIHelper.createPrimaryButton("ROLL DICE!");
        rollButton.setPreferredSize(new Dimension(150, 50));
        rollButton.addActionListener(e -> rollDice());

        panel.add(betLabel);
        panel.add(minBtn);
        panel.add(halfBtn);
        panel.add(betSpinner);
        panel.add(doubleBtn);
        panel.add(maxBtn);
        panel.add(rollButton);

        return panel;
    }

    private void updateSelectedBetLabel() {
        String[] labels = {
                "OVER7", "Over 7",
                "UNDER7", "Under 7",
                "EXACT7", "Exact 7",
                "DOUBLES", "Doubles",
                "ODD", "Odd Total",
                "EVEN", "Even Total",
                "HIGH", "High (10-12)",
                "LOW", "Low (2-4)"
        };
        for (int i = 0; i < labels.length; i += 2) {
            if (labels[i].equals(selectedBetType)) {
                selectedBetLabel.setText("Bet: " + labels[i + 1]);
                break;
            }
        }
    }

    private void rollDice() {
        if (rolling) return;

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

        rolling = true;
        rollButton.setEnabled(false);
        messageLabel.setText("Rolling...");
        messageLabel.setForeground(Color.WHITE);

        // 骰子動畫
        Timer rollTimer = new Timer(50, null);
        final int[] count = {0};
        final int maxCount = 20;

        rollTimer.addActionListener(e -> {
            count[0]++;
            dice1Value = random.nextInt(6) + 1;
            dice2Value = random.nextInt(6) + 1;
            dice1Panel.setValue(dice1Value);
            dice2Panel.setValue(dice2Value);

            if (count[0] >= maxCount) {
                rollTimer.stop();
                // 最終結果
                dice1Value = random.nextInt(6) + 1;
                dice2Value = random.nextInt(6) + 1;
                dice1Panel.setValue(dice1Value);
                dice2Panel.setValue(dice2Value);
                checkResult(betAmount);
            }
        });
        rollTimer.start();
    }

    private void checkResult(int betAmount) {
        int total = dice1Value + dice2Value;
        boolean won = false;

        switch (selectedBetType) {
            case "OVER7":
                won = total > 7;
                break;
            case "UNDER7":
                won = total < 7;
                break;
            case "EXACT7":
                won = total == 7;
                break;
            case "DOUBLES":
                won = dice1Value == dice2Value;
                break;
            case "ODD":
                won = total % 2 == 1;
                break;
            case "EVEN":
                won = total % 2 == 0;
                break;
            case "HIGH":
                won = total >= 10;
                break;
            case "LOW":
                won = total <= 4;
                break;
        }

        Player player = mainFrame.getCurrentPlayer();
        int winAmount = 0;

        if (won) {
            double payout = PAYOUTS.get(selectedBetType);
            winAmount = (int) (betAmount * payout);
            player.addWinnings(winAmount);

            messageLabel.setText("WIN! Total: " + total + " (+$" + winAmount + ")");
            messageLabel.setForeground(Constants.CASINO_GOLD);
            AnimationManager.pulse(messageLabel, 3, null);
            AnimationManager.bounce(dice1Panel, null);
            AnimationManager.bounce(dice2Panel, null);

            AnimationManager.countUp(balanceLabel, player.getBalance() - winAmount,
                    player.getBalance(), 600, "$", "");
        } else {
            messageLabel.setText("No win. Total: " + total);
            messageLabel.setForeground(Color.GRAY);
            updateBalanceDisplay();
        }

        player.recordGame("Dice", betAmount, winAmount, won);
        PlayerService.getInstance().savePlayers();

        rolling = false;
        rollButton.setEnabled(true);
    }

    private void updateBalanceDisplay() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null) {
            balanceLabel.setText("$" + String.format("%,d", player.getBalance()));
        }
    }

    private JButton createQuickBetButton(String text, int value) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(45, 28));
        btn.setBackground(new Color(60, 60, 80));
        btn.setForeground(Constants.CASINO_GOLD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Constants.CASINO_GOLD, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            if (rolling) return;
            Player player = mainFrame.getCurrentPlayer();
            if (player == null) return;

            int newBet;
            if (value == -1) {
                newBet = Math.max(50, (player.getBalance() / 2 / 50) * 50);
            } else if (value == -2) {
                newBet = Math.min(5000, (int) betSpinner.getValue() * 2);
            } else {
                newBet = Math.min(value, player.getBalance());
            }
            newBet = Math.min(newBet, 5000);
            newBet = Math.max(newBet, 50);
            betSpinner.setValue(newBet);
        });

        return btn;
    }

    // ========== 骰子面板 ==========

    private class DicePanel extends JPanel {
        private int value = 1;

        public DicePanel() {
            setPreferredSize(new Dimension(100, 100));
            setOpaque(false);
        }

        public void setValue(int value) {
            this.value = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int size = Math.min(w, h) - 10;
            int x = (w - size) / 2;
            int y = (h - size) / 2;

            // 陰影
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fill(new RoundRectangle2D.Double(x + 4, y + 4, size, size, 15, 15));

            // 骰子本體
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(x, y, size, size, 15, 15));

            // 邊框
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(2));
            g2.draw(new RoundRectangle2D.Double(x, y, size, size, 15, 15));

            // 繪製點數
            g2.setColor(new Color(30, 30, 30));
            int dotSize = 14;
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            int offset = size / 4;

            switch (value) {
                case 1:
                    drawDot(g2, centerX, centerY, dotSize);
                    break;
                case 2:
                    drawDot(g2, centerX - offset, centerY - offset, dotSize);
                    drawDot(g2, centerX + offset, centerY + offset, dotSize);
                    break;
                case 3:
                    drawDot(g2, centerX - offset, centerY - offset, dotSize);
                    drawDot(g2, centerX, centerY, dotSize);
                    drawDot(g2, centerX + offset, centerY + offset, dotSize);
                    break;
                case 4:
                    drawDot(g2, centerX - offset, centerY - offset, dotSize);
                    drawDot(g2, centerX + offset, centerY - offset, dotSize);
                    drawDot(g2, centerX - offset, centerY + offset, dotSize);
                    drawDot(g2, centerX + offset, centerY + offset, dotSize);
                    break;
                case 5:
                    drawDot(g2, centerX - offset, centerY - offset, dotSize);
                    drawDot(g2, centerX + offset, centerY - offset, dotSize);
                    drawDot(g2, centerX, centerY, dotSize);
                    drawDot(g2, centerX - offset, centerY + offset, dotSize);
                    drawDot(g2, centerX + offset, centerY + offset, dotSize);
                    break;
                case 6:
                    drawDot(g2, centerX - offset, centerY - offset, dotSize);
                    drawDot(g2, centerX + offset, centerY - offset, dotSize);
                    drawDot(g2, centerX - offset, centerY, dotSize);
                    drawDot(g2, centerX + offset, centerY, dotSize);
                    drawDot(g2, centerX - offset, centerY + offset, dotSize);
                    drawDot(g2, centerX + offset, centerY + offset, dotSize);
                    break;
            }

            g2.dispose();
        }

        private void drawDot(Graphics2D g2, int x, int y, int size) {
            g2.fillOval(x - size / 2, y - size / 2, size, size);
        }
    }
}
