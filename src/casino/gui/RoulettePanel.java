package casino.gui;

import casino.model.Player;
import casino.service.PlayerService;
import casino.util.AnimationManager;
import casino.util.Constants;
import casino.util.UIHelper;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * 輪盤遊戲面板 - 支援多重下注
 * Roulette Game Panel - Multiple Bets Support
 */
public class RoulettePanel extends JPanel {

    private MainFrame mainFrame;
    private Random random;

    // 輪盤數字顏色
    private static final int[] RED_NUMBERS = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};
    private static final Set<Integer> RED_SET = new HashSet<>();
    static {
        for (int n : RED_NUMBERS) RED_SET.add(n);
    }

    // 歐式輪盤數字順序 (順時針)
    private static final int[] WHEEL_NUMBERS = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    // 下注類型枚舉
    public enum BetType {
        STRAIGHT(35, "Single Number"),      // 單號 35:1
        SPLIT(17, "2 Numbers"),             // 分注 17:1
        STREET(11, "3 Numbers"),            // 街注 11:1
        CORNER(8, "4 Numbers"),             // 角注 8:1
        SIX_LINE(5, "6 Numbers"),           // 線注 5:1
        COLUMN(2, "Column"),                // 列注 2:1
        DOZEN(2, "Dozen"),                  // 打注 2:1
        RED(1, "Red"),                      // 紅 1:1
        BLACK(1, "Black"),                  // 黑 1:1
        ODD(1, "Odd"),                      // 奇 1:1
        EVEN(1, "Even"),                    // 偶 1:1
        LOW(1, "1-18"),                     // 小 1:1
        HIGH(1, "19-36");                   // 大 1:1

        final int payout;
        final String description;

        BetType(int payout, String description) {
            this.payout = payout;
            this.description = description;
        }
    }

    // 下注資料類
    private static class Bet {
        BetType type;
        int[] numbers;  // 涵蓋的數字
        int amount;
        Rectangle area; // 下注區域位置

        Bet(BetType type, int[] numbers, int amount, Rectangle area) {
            this.type = type;
            this.numbers = numbers;
            this.amount = amount;
            this.area = area;
        }

        boolean coversNumber(int num) {
            for (int n : numbers) {
                if (n == num) return true;
            }
            return false;
        }
    }

    // 當前所有下注
    private List<Bet> currentBets = new ArrayList<>();
    private int selectedChipValue = 100;

    // UI 元件
    private JLabel balanceLabel;
    private JLabel totalBetLabel;
    private JLabel messageLabel;
    private JButton spinButton;
    private JButton clearButton;
    private BettingBoard bettingBoard;
    private RouletteWheelPanel wheelPanel;
    private boolean spinning = false;

    public RoulettePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.random = new Random();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(0, 60, 0));
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
                g2.setColor(new Color(0, 40, 0));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = UIHelper.createSecondaryButton("<< Back");
        backButton.setPreferredSize(new Dimension(100, 35));
        backButton.addActionListener(e -> {
            if (!currentBets.isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "You have active bets. Leave anyway?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
                refundAllBets();
            }
            mainFrame.showPanel(MainFrame.LOBBY_PANEL);
        });

        JLabel titleLabel = new JLabel("ROULETTE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Constants.CASINO_GOLD);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        totalBetLabel = new JLabel("Bet: $0");
        totalBetLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalBetLabel.setForeground(Color.WHITE);

        balanceLabel = new JLabel("$10,000");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 22));
        balanceLabel.setForeground(Constants.CASINO_GOLD);

        rightPanel.add(totalBetLabel);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(balanceLabel);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createGameArea() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);

        // 左側：下注板
        bettingBoard = new BettingBoard();
        JScrollPane scrollPane = new JScrollPane(bettingBoard);
        scrollPane.setBorder(BorderFactory.createLineBorder(Constants.CASINO_GOLD, 2));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // 右側：輪盤與籌碼選擇
        JPanel rightSide = new JPanel(new BorderLayout(0, 15));
        rightSide.setOpaque(false);
        rightSide.setPreferredSize(new Dimension(280, 400));

        rightSide.add(createWheelDisplay(), BorderLayout.CENTER);
        rightSide.add(createChipSelector(), BorderLayout.SOUTH);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(rightSide, BorderLayout.EAST);

        return panel;
    }

    private JPanel createWheelDisplay() {
        JPanel panel = new JPanel(new BorderLayout(0, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 真實輪盤
        wheelPanel = new RouletteWheelPanel();

        messageLabel = new JLabel("Place your bets!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setForeground(Color.WHITE);

        panel.add(wheelPanel, BorderLayout.CENTER);
        panel.add(messageLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createChipSelector() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(30, 30, 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel label = new JLabel("Select Chip Value:", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);

        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        chipsPanel.setOpaque(false);

        int[] chipValues = {25, 50, 100, 500, 1000};
        Color[] chipColors = {
                new Color(50, 150, 50),   // 25 - 綠
                new Color(50, 50, 200),   // 50 - 藍
                new Color(200, 50, 50),   // 100 - 紅
                new Color(150, 50, 150),  // 500 - 紫
                new Color(200, 150, 50)   // 1000 - 金
        };

        ButtonGroup chipGroup = new ButtonGroup();
        for (int i = 0; i < chipValues.length; i++) {
            final int value = chipValues[i];
            final Color color = chipColors[i];

            JToggleButton chipBtn = new JToggleButton("$" + value) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    Color bgColor = isSelected() ? color.brighter() : color;
                    g2.setColor(bgColor);
                    g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);

                    g2.setColor(isSelected() ? Color.WHITE : color.darker());
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(4, 4, getWidth() - 8, getHeight() - 8);

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    String text = "$" + value;
                    int x = (getWidth() - fm.stringWidth(text)) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, x, y);

                    g2.dispose();
                }
            };
            chipBtn.setPreferredSize(new Dimension(45, 45));
            chipBtn.setContentAreaFilled(false);
            chipBtn.setBorderPainted(false);
            chipBtn.setFocusPainted(false);
            chipBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chipBtn.addActionListener(e -> selectedChipValue = value);

            if (value == 100) chipBtn.setSelected(true);
            chipGroup.add(chipBtn);
            chipsPanel.add(chipBtn);
        }

        panel.add(label);
        panel.add(chipsPanel);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 40, 0));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        clearButton = UIHelper.createDangerButton("Clear Bets");
        clearButton.addActionListener(e -> {
            refundAllBets();
            bettingBoard.repaint();
        });

        spinButton = UIHelper.createPrimaryButton("SPIN!");
        spinButton.setPreferredSize(new Dimension(150, 50));
        spinButton.addActionListener(e -> spin());

        // 賠率說明按鈕
        JButton oddsButton = UIHelper.createSecondaryButton("Odds Info");
        oddsButton.addActionListener(e -> showOddsInfo());

        panel.add(clearButton);
        panel.add(spinButton);
        panel.add(oddsButton);

        return panel;
    }

    // ========== 下注邏輯 ==========

    private void placeBet(BetType type, int[] numbers, Rectangle area) {
        if (spinning) return;

        Player player = mainFrame.getCurrentPlayer();
        if (player.getBalance() < selectedChipValue) {
            messageLabel.setText("Insufficient balance!");
            messageLabel.setForeground(Color.RED);
            AnimationManager.shake(messageLabel, null);
            return;
        }

        // 扣除籌碼
        player.placeBet(selectedChipValue);
        updateBalanceDisplay();

        // 檢查是否已在同位置下注
        Bet existingBet = null;
        for (Bet b : currentBets) {
            if (Arrays.equals(b.numbers, numbers) && b.type == type) {
                existingBet = b;
                break;
            }
        }

        if (existingBet != null) {
            existingBet.amount += selectedChipValue;
        } else {
            currentBets.add(new Bet(type, numbers, selectedChipValue, area));
        }

        updateTotalBet();
        messageLabel.setText("Bet placed! " + type.description + " (" + type.payout + ":1)");
        messageLabel.setForeground(Constants.CASINO_GOLD);
        bettingBoard.repaint();
    }

    private void refundAllBets() {
        Player player = mainFrame.getCurrentPlayer();
        int totalRefund = 0;
        for (Bet bet : currentBets) {
            totalRefund += bet.amount;
        }
        if (totalRefund > 0) {
            player.addWinnings(totalRefund);
            updateBalanceDisplay();
        }
        currentBets.clear();
        updateTotalBet();
        messageLabel.setText("Bets cleared!");
        messageLabel.setForeground(Color.WHITE);
    }

    private void updateTotalBet() {
        int total = 0;
        for (Bet bet : currentBets) {
            total += bet.amount;
        }
        totalBetLabel.setText("Bet: $" + String.format("%,d", total));
    }

    // ========== 遊戲邏輯 ==========

    private void spin() {
        if (spinning) return;

        if (currentBets.isEmpty()) {
            messageLabel.setText("Place at least one bet!");
            messageLabel.setForeground(Color.RED);
            AnimationManager.shake(messageLabel, null);
            return;
        }

        spinning = true;
        spinButton.setEnabled(false);
        clearButton.setEnabled(false);
        messageLabel.setText("Spinning...");
        messageLabel.setForeground(Color.WHITE);

        // 決定結果並啟動輪盤動畫
        int result = random.nextInt(37);
        wheelPanel.spin(result, () -> showResult(result));
    }

    private void showResult(int result) {

        // 計算所有贏得的金額
        int totalWin = 0;
        int totalBet = 0;
        StringBuilder winDetails = new StringBuilder();

        for (Bet bet : currentBets) {
            totalBet += bet.amount;
            if (bet.coversNumber(result)) {
                int win = bet.amount + (bet.amount * bet.type.payout);
                totalWin += win;
                winDetails.append(bet.type.description).append(": +$").append(win).append(" ");
            }
        }

        Player player = mainFrame.getCurrentPlayer();
        boolean won = totalWin > 0;

        if (won) {
            player.addWinnings(totalWin);
            messageLabel.setText("WIN! $" + String.format("%,d", totalWin));
            messageLabel.setForeground(Constants.CASINO_GOLD);
            AnimationManager.pulse(messageLabel, 3, null);

            // 餘額動畫
            AnimationManager.countUp(balanceLabel, player.getBalance() - totalWin,
                    player.getBalance(), 800, "$", "");
        } else {
            String colorName = (result == 0) ? "Green" : (RED_SET.contains(result) ? "Red" : "Black");
            messageLabel.setText("No win. " + result + " " + colorName);
            messageLabel.setForeground(Color.GRAY);
            updateBalanceDisplay();
        }

        // 記錄遊戲
        player.recordGame("Roulette", totalBet, totalWin, won);
        PlayerService.getInstance().savePlayers();

        // 清空下注
        currentBets.clear();
        updateTotalBet();
        bettingBoard.repaint();

        spinning = false;
        spinButton.setEnabled(true);
        clearButton.setEnabled(true);
    }

    private void showOddsInfo() {
        String info = """
            === ROULETTE ODDS ===

            INSIDE BETS:
            - Straight (1 number): 35 to 1
            - Split (2 numbers): 17 to 1
            - Street (3 numbers): 11 to 1
            - Corner (4 numbers): 8 to 1
            - Six Line (6 numbers): 5 to 1

            OUTSIDE BETS:
            - Column (12 numbers): 2 to 1
            - Dozen (12 numbers): 2 to 1
            - Red/Black: 1 to 1
            - Odd/Even: 1 to 1
            - Low/High (1-18/19-36): 1 to 1

            TIP: Click on borders between numbers
            for Split and Corner bets!
            """;

        JTextArea textArea = new JTextArea(info);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(new Color(30, 30, 40));
        textArea.setForeground(Color.WHITE);
        JOptionPane.showMessageDialog(this, textArea, "Betting Odds", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateBalanceDisplay() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null) {
            balanceLabel.setText("$" + String.format("%,d", player.getBalance()));
        }
    }

    // ========== 下注板面板 ==========

    private class BettingBoard extends JPanel {
        private static final int CELL_WIDTH = 50;
        private static final int CELL_HEIGHT = 60;
        private static final int MARGIN = 40;

        // 數字佈局 (3列 x 12行)
        private final int[][] numberGrid = {
                {3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36},
                {2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35},
                {1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34}
        };

        public BettingBoard() {
            setPreferredSize(new Dimension(MARGIN * 2 + CELL_WIDTH * 14, MARGIN * 2 + CELL_HEIGHT * 6));
            setBackground(new Color(0, 80, 0));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleClick(e.getX(), e.getY());
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            });
        }

        private void handleClick(int x, int y) {
            if (spinning) return;

            // 檢查 0
            if (x >= MARGIN && x < MARGIN + CELL_WIDTH && y >= MARGIN && y < MARGIN + CELL_HEIGHT * 3) {
                placeBet(BetType.STRAIGHT, new int[]{0},
                        new Rectangle(MARGIN, MARGIN, CELL_WIDTH, CELL_HEIGHT * 3));
                return;
            }

            // 數字區域
            int gridX = (x - MARGIN - CELL_WIDTH) / CELL_WIDTH;
            int gridY = (y - MARGIN) / CELL_HEIGHT;

            if (gridX >= 0 && gridX < 12 && gridY >= 0 && gridY < 3) {
                int number = numberGrid[gridY][gridX];

                // 檢查是否點擊在邊界上 (Split/Corner)
                int cellLeft = MARGIN + CELL_WIDTH + gridX * CELL_WIDTH;
                int cellTop = MARGIN + gridY * CELL_HEIGHT;
                int relX = x - cellLeft;
                int relY = y - cellTop;

                boolean nearLeft = relX < 10;
                boolean nearRight = relX > CELL_WIDTH - 10;
                boolean nearTop = relY < 10;
                boolean nearBottom = relY > CELL_HEIGHT - 10;

                // Corner bet (4個數字)
                if ((nearLeft || nearRight) && (nearTop || nearBottom)) {
                    List<Integer> nums = new ArrayList<>();
                    nums.add(number);

                    if (nearLeft && gridX > 0) {
                        nums.add(numberGrid[gridY][gridX - 1]);
                    }
                    if (nearRight && gridX < 11) {
                        nums.add(numberGrid[gridY][gridX + 1]);
                    }
                    if (nearTop && gridY > 0) {
                        nums.add(numberGrid[gridY - 1][gridX]);
                        if (nearLeft && gridX > 0) nums.add(numberGrid[gridY - 1][gridX - 1]);
                        if (nearRight && gridX < 11) nums.add(numberGrid[gridY - 1][gridX + 1]);
                    }
                    if (nearBottom && gridY < 2) {
                        nums.add(numberGrid[gridY + 1][gridX]);
                        if (nearLeft && gridX > 0) nums.add(numberGrid[gridY + 1][gridX - 1]);
                        if (nearRight && gridX < 11) nums.add(numberGrid[gridY + 1][gridX + 1]);
                    }

                    if (nums.size() == 4) {
                        int[] arr = nums.stream().mapToInt(i -> i).toArray();
                        placeBet(BetType.CORNER, arr, new Rectangle(x - 15, y - 15, 30, 30));
                        return;
                    }
                }

                // Split bet (2個數字) - 水平
                if (nearTop && gridY > 0) {
                    int otherNum = numberGrid[gridY - 1][gridX];
                    placeBet(BetType.SPLIT, new int[]{number, otherNum},
                            new Rectangle(cellLeft + 15, cellTop - 10, 20, 20));
                    return;
                }
                if (nearBottom && gridY < 2) {
                    int otherNum = numberGrid[gridY + 1][gridX];
                    placeBet(BetType.SPLIT, new int[]{number, otherNum},
                            new Rectangle(cellLeft + 15, cellTop + CELL_HEIGHT - 10, 20, 20));
                    return;
                }

                // Split bet (2個數字) - 垂直
                if (nearLeft && gridX > 0) {
                    int otherNum = numberGrid[gridY][gridX - 1];
                    placeBet(BetType.SPLIT, new int[]{number, otherNum},
                            new Rectangle(cellLeft - 10, cellTop + 20, 20, 20));
                    return;
                }
                if (nearRight && gridX < 11) {
                    int otherNum = numberGrid[gridY][gridX + 1];
                    placeBet(BetType.SPLIT, new int[]{number, otherNum},
                            new Rectangle(cellLeft + CELL_WIDTH - 10, cellTop + 20, 20, 20));
                    return;
                }

                // Straight bet (單號)
                placeBet(BetType.STRAIGHT, new int[]{number},
                        new Rectangle(cellLeft, cellTop, CELL_WIDTH, CELL_HEIGHT));
                return;
            }

            // Column bet (一列12個數字) - 最右邊的區域
            if (x > MARGIN + CELL_WIDTH * 13 && gridY >= 0 && gridY < 3) {
                placeBet(BetType.COLUMN, numberGrid[gridY],
                        new Rectangle(MARGIN + CELL_WIDTH * 13, MARGIN + gridY * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT));
                return;
            }

            // 外圍下注區域
            int outsideY = MARGIN + CELL_HEIGHT * 3;

            // Dozen bets (1-12, 13-24, 25-36)
            if (y >= outsideY && y < outsideY + CELL_HEIGHT) {
                int dozenIndex = (x - MARGIN - CELL_WIDTH) / (CELL_WIDTH * 4);
                if (dozenIndex >= 0 && dozenIndex < 3) {
                    int start = dozenIndex * 12 + 1;
                    int[] nums = new int[12];
                    for (int i = 0; i < 12; i++) nums[i] = start + i;
                    String[] dozenNames = {"1-12", "13-24", "25-36"};
                    placeBet(BetType.DOZEN, nums,
                            new Rectangle(MARGIN + CELL_WIDTH + dozenIndex * CELL_WIDTH * 4, outsideY, CELL_WIDTH * 4, CELL_HEIGHT));
                    return;
                }
            }

            // 1:1 bets (Low, Even, Red, Black, Odd, High)
            outsideY += CELL_HEIGHT;
            if (y >= outsideY && y < outsideY + CELL_HEIGHT) {
                int betIndex = (x - MARGIN - CELL_WIDTH) / (CELL_WIDTH * 2);
                if (betIndex >= 0 && betIndex < 6) {
                    int[] nums;
                    BetType type;
                    switch (betIndex) {
                        case 0: // 1-18
                            nums = new int[18];
                            for (int i = 0; i < 18; i++) nums[i] = i + 1;
                            type = BetType.LOW;
                            break;
                        case 1: // Even
                            nums = new int[18];
                            for (int i = 0; i < 18; i++) nums[i] = (i + 1) * 2;
                            type = BetType.EVEN;
                            break;
                        case 2: // Red
                            nums = RED_NUMBERS.clone();
                            type = BetType.RED;
                            break;
                        case 3: // Black
                            nums = new int[18];
                            int idx = 0;
                            for (int i = 1; i <= 36; i++) {
                                if (!RED_SET.contains(i)) nums[idx++] = i;
                            }
                            type = BetType.BLACK;
                            break;
                        case 4: // Odd
                            nums = new int[18];
                            for (int i = 0; i < 18; i++) nums[i] = i * 2 + 1;
                            type = BetType.ODD;
                            break;
                        default: // 19-36
                            nums = new int[18];
                            for (int i = 0; i < 18; i++) nums[i] = i + 19;
                            type = BetType.HIGH;
                            break;
                    }
                    placeBet(type, nums,
                            new Rectangle(MARGIN + CELL_WIDTH + betIndex * CELL_WIDTH * 2, outsideY, CELL_WIDTH * 2, CELL_HEIGHT));
                    return;
                }
            }

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 繪製 0
            g2.setColor(new Color(0, 128, 0));
            g2.fillRect(MARGIN, MARGIN, CELL_WIDTH, CELL_HEIGHT * 3);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(MARGIN, MARGIN, CELL_WIDTH, CELL_HEIGHT * 3);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            g2.drawString("0", MARGIN + 18, MARGIN + CELL_HEIGHT * 1.5f + 8);

            // 繪製數字 1-36
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 12; col++) {
                    int num = numberGrid[row][col];
                    int x = MARGIN + CELL_WIDTH + col * CELL_WIDTH;
                    int y = MARGIN + row * CELL_HEIGHT;

                    // 背景顏色
                    if (RED_SET.contains(num)) {
                        g2.setColor(new Color(180, 0, 0));
                    } else {
                        g2.setColor(Color.BLACK);
                    }
                    g2.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);

                    // 邊框
                    g2.setColor(Color.WHITE);
                    g2.drawRect(x, y, CELL_WIDTH, CELL_HEIGHT);

                    // 數字
                    g2.setFont(new Font("Arial", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    String text = String.valueOf(num);
                    int textX = x + (CELL_WIDTH - fm.stringWidth(text)) / 2;
                    int textY = y + (CELL_HEIGHT + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, textX, textY);
                }
            }

            // Column 標籤
            int colX = MARGIN + CELL_WIDTH * 13;
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            for (int i = 0; i < 3; i++) {
                g2.setColor(new Color(0, 100, 0));
                g2.fillRect(colX, MARGIN + i * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                g2.setColor(Color.WHITE);
                g2.drawRect(colX, MARGIN + i * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                g2.drawString("2:1", colX + 12, MARGIN + i * CELL_HEIGHT + 35);
            }

            // Dozen bets
            int dozenY = MARGIN + CELL_HEIGHT * 3;
            String[] dozenLabels = {"1st 12", "2nd 12", "3rd 12"};
            for (int i = 0; i < 3; i++) {
                int dx = MARGIN + CELL_WIDTH + i * CELL_WIDTH * 4;
                g2.setColor(new Color(0, 100, 0));
                g2.fillRect(dx, dozenY, CELL_WIDTH * 4, CELL_HEIGHT);
                g2.setColor(Color.WHITE);
                g2.drawRect(dx, dozenY, CELL_WIDTH * 4, CELL_HEIGHT);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(dozenLabels[i], dx + (CELL_WIDTH * 4 - fm.stringWidth(dozenLabels[i])) / 2, dozenY + 35);
            }

            // 1:1 bets
            int outsideY = dozenY + CELL_HEIGHT;
            String[] outsideLabels = {"1-18", "EVEN", "RED", "BLACK", "ODD", "19-36"};
            Color[] outsideColors = {
                    new Color(0, 100, 0), new Color(0, 100, 0),
                    new Color(180, 0, 0), Color.BLACK,
                    new Color(0, 100, 0), new Color(0, 100, 0)
            };

            for (int i = 0; i < 6; i++) {
                int ox = MARGIN + CELL_WIDTH + i * CELL_WIDTH * 2;
                g2.setColor(outsideColors[i]);
                g2.fillRect(ox, outsideY, CELL_WIDTH * 2, CELL_HEIGHT);
                g2.setColor(Color.WHITE);
                g2.drawRect(ox, outsideY, CELL_WIDTH * 2, CELL_HEIGHT);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(outsideLabels[i], ox + (CELL_WIDTH * 2 - fm.stringWidth(outsideLabels[i])) / 2, outsideY + 35);
            }

            // 繪製籌碼
            for (Bet bet : currentBets) {
                drawChip(g2, bet.area.x + bet.area.width / 2, bet.area.y + bet.area.height / 2, bet.amount);
            }

            g2.dispose();
        }

        private void drawChip(Graphics2D g2, int x, int y, int amount) {
            int size = 30;

            // 籌碼顏色根據金額
            Color chipColor;
            if (amount >= 1000) chipColor = new Color(200, 150, 50);
            else if (amount >= 500) chipColor = new Color(150, 50, 150);
            else if (amount >= 100) chipColor = new Color(200, 50, 50);
            else chipColor = new Color(50, 150, 50);

            // 陰影
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillOval(x - size / 2 + 2, y - size / 2 + 2, size, size);

            // 籌碼本體
            g2.setColor(chipColor);
            g2.fillOval(x - size / 2, y - size / 2, size, size);

            // 邊框
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x - size / 2 + 3, y - size / 2 + 3, size - 6, size - 6);

            // 金額文字
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            FontMetrics fm = g2.getFontMetrics();
            String text = "$" + amount;
            g2.drawString(text, x - fm.stringWidth(text) / 2, y + 4);
        }
    }

    // ========== 輪盤面板 ==========

    private class RouletteWheelPanel extends JPanel {
        private double ballAngle = 90;       // 球的角度 (從頂部開始)
        private double ballRadius;           // 球的軌道半徑
        private boolean isSpinning = false;
        private int targetNumber = -1;
        private Timer spinTimer;
        private Runnable onComplete;

        // 動畫參數 - 使用緩動函數
        private double startAngle = 0;
        private double targetAngle = 0;
        private double startRadius = 0;
        private double targetRadius = 0;
        private int animationFrame = 0;
        private int totalFrames = 200;  // 約 3.2 秒動畫

        public RouletteWheelPanel() {
            setPreferredSize(new Dimension(250, 250));
            setOpaque(false);
        }

        public void spin(int resultNumber, Runnable callback) {
            this.targetNumber = resultNumber;
            this.onComplete = callback;
            this.isSpinning = true;
            this.animationFrame = 0;

            // 計算目標角度
            int targetIndex = 0;
            for (int i = 0; i < WHEEL_NUMBERS.length; i++) {
                if (WHEEL_NUMBERS[i] == targetNumber) {
                    targetIndex = i;
                    break;
                }
            }
            double segmentAngle = 360.0 / WHEEL_NUMBERS.length;
            double finalAngle = 90 - (targetIndex * segmentAngle + segmentAngle / 2);

            // 從當前位置開始，逆時針轉至少 6 圈再停在目標
            this.startAngle = ballAngle;
            this.targetAngle = startAngle - 360 * 6 - ((startAngle - finalAngle) % 360 + 360) % 360;

            // 球的軌道從外到內
            this.startRadius = getWheelRadius() * 0.82;
            this.targetRadius = getWheelRadius() * 0.55;
            this.ballRadius = startRadius;

            spinTimer = new Timer(16, e -> updateAnimation());
            spinTimer.start();
        }

        private void updateAnimation() {
            animationFrame++;

            // 使用 easeOutQuart 緩動函數
            double progress = (double) animationFrame / totalFrames;
            double easedProgress = 1 - Math.pow(1 - progress, 4);

            // 計算當前角度和半徑
            ballAngle = startAngle + (targetAngle - startAngle) * easedProgress;

            // 球在後半段才開始往內縮
            if (progress > 0.5) {
                double radiusProgress = (progress - 0.5) * 2;  // 0 到 1
                double easedRadiusProgress = 1 - Math.pow(1 - radiusProgress, 2);
                ballRadius = startRadius + (targetRadius - startRadius) * easedRadiusProgress;
            }

            repaint();

            // 動畫結束
            if (animationFrame >= totalFrames) {
                spinTimer.stop();
                isSpinning = false;
                ballAngle = targetAngle;
                ballRadius = targetRadius;
                repaint();

                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }

        private int getWheelRadius() {
            return Math.min(getWidth(), getHeight()) / 2 - 15;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = getWheelRadius();

            // 外框 (木頭色)
            g2.setColor(new Color(101, 67, 33));
            g2.fillOval(centerX - radius - 10, centerY - radius - 10,
                    (radius + 10) * 2, (radius + 10) * 2);

            g2.setColor(Constants.CASINO_GOLD);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(centerX - radius - 10, centerY - radius - 10,
                    (radius + 10) * 2, (radius + 10) * 2);

            // 繪製輪盤扇區 (固定不轉)
            double segmentAngle = 360.0 / WHEEL_NUMBERS.length;
            for (int i = 0; i < WHEEL_NUMBERS.length; i++) {
                int number = WHEEL_NUMBERS[i];

                // 顏色
                Color segmentColor;
                if (number == 0) {
                    segmentColor = new Color(0, 128, 0);  // 綠色
                } else if (RED_SET.contains(number)) {
                    segmentColor = new Color(180, 0, 0);  // 紅色
                } else {
                    segmentColor = new Color(20, 20, 20); // 黑色
                }

                // 繪製扇區 (從頂部開始，順時針)
                int startAngle = (int) (90 - (i + 1) * segmentAngle);
                g2.setColor(segmentColor);
                g2.fillArc(centerX - radius, centerY - radius,
                        radius * 2, radius * 2,
                        startAngle, (int) segmentAngle + 1);

                // 繪製數字
                double angle = Math.toRadians(90 - i * segmentAngle - segmentAngle / 2);
                int textRadius = radius - 20;
                int textX = centerX + (int) (textRadius * Math.cos(angle));
                int textY = centerY - (int) (textRadius * Math.sin(angle));

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                String numStr = String.valueOf(number);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(numStr, textX - fm.stringWidth(numStr) / 2, textY + 4);
            }

            // 球軌道圈
            g2.setColor(new Color(80, 60, 40));
            g2.setStroke(new BasicStroke(2));
            int trackRadius = (int) (radius * 0.7);
            g2.drawOval(centerX - trackRadius, centerY - trackRadius,
                    trackRadius * 2, trackRadius * 2);

            // 中心圓
            int innerRadius = radius / 3;
            g2.setColor(new Color(60, 60, 60));
            g2.fillOval(centerX - innerRadius, centerY - innerRadius,
                    innerRadius * 2, innerRadius * 2);
            g2.setColor(Constants.CASINO_GOLD);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(centerX - innerRadius, centerY - innerRadius,
                    innerRadius * 2, innerRadius * 2);

            // 繪製球
            double actualBallAngle = Math.toRadians(ballAngle);
            int ballX = centerX + (int) (ballRadius * Math.cos(actualBallAngle));
            int ballY = centerY - (int) (ballRadius * Math.sin(actualBallAngle));

            // 球的陰影
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(ballX - 5, ballY - 3, 12, 12);

            // 球本體
            g2.setColor(Color.WHITE);
            g2.fillOval(ballX - 6, ballY - 6, 12, 12);

            // 球的高光
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillOval(ballX - 4, ballY - 4, 5, 5);

            // 指針 (頂部)
            g2.setColor(Constants.CASINO_GOLD);
            int[] triangleX = {centerX - 8, centerX + 8, centerX};
            int[] triangleY = {centerY - radius - 5, centerY - radius - 5, centerY - radius + 10};
            g2.fillPolygon(triangleX, triangleY, 3);

            // 顯示結果數字 (在中心)
            if (!isSpinning && targetNumber >= 0) {
                g2.setFont(new Font("Arial", Font.BOLD, 22));
                String resultStr = String.valueOf(targetNumber);
                FontMetrics fm = g2.getFontMetrics();

                // 背景框
                Color numColor = (targetNumber == 0) ? new Color(0, 128, 0) :
                        (RED_SET.contains(targetNumber) ? new Color(180, 0, 0) : Color.BLACK);
                g2.setColor(numColor);
                g2.fillRoundRect(centerX - 18, centerY - 12, 36, 24, 8, 8);
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(centerX - 18, centerY - 12, 36, 24, 8, 8);

                g2.setColor(Color.WHITE);
                g2.drawString(resultStr, centerX - fm.stringWidth(resultStr) / 2, centerY + 7);
            }

            g2.dispose();
        }
    }
}
