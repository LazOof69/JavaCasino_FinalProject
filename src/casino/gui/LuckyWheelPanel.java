package casino.gui;

import casino.model.Player;
import casino.service.PlayerService;
import casino.util.AnimationManager;
import casino.util.Constants;
import casino.util.UIHelper;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

/**
 * 幸運轉盤面板
 * Lucky Wheel Panel
 */
public class LuckyWheelPanel extends JPanel {

    private MainFrame mainFrame;
    private Random random;

    // 轉盤獎品
    private static final String[] PRIZES = {
            "$500", "$1,000", "$200", "$2,000", "$100",
            "$5,000", "$300", "$1,500", "JACKPOT", "$800",
            "$400", "$3,000"
    };
    private static final int[] PRIZE_VALUES = {
            500, 1000, 200, 2000, 100,
            5000, 300, 1500, 10000, 800,
            400, 3000
    };
    private static final Color[] SEGMENT_COLORS = {
            new Color(220, 50, 50),   // Red
            new Color(50, 150, 50),   // Green
            new Color(50, 50, 200),   // Blue
            new Color(220, 180, 50),  // Gold
            new Color(150, 50, 150),  // Purple
            new Color(255, 100, 0),   // Orange
            new Color(50, 150, 150),  // Teal
            new Color(200, 50, 100),  // Pink
            new Color(255, 215, 0),   // Jackpot Gold
            new Color(100, 100, 200), // Light Blue
            new Color(150, 100, 50),  // Brown
            new Color(80, 180, 80)    // Light Green
    };

    // UI 元件
    private JLabel balanceLabel;
    private JLabel messageLabel;
    private JButton freeSpinButton;
    private JButton paidSpinButton;
    private WheelPanel wheelPanel;

    // 遊戲狀態
    private boolean spinning = false;
    private int spinCost = 500;

    public LuckyWheelPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.random = new Random();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(25, 25, 50));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, new Color(100, 50, 150),
                        getWidth(), 0, new Color(50, 25, 100));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = UIHelper.createSecondaryButton("<< Back");
        backButton.setPreferredSize(new Dimension(100, 35));
        backButton.addActionListener(e -> mainFrame.showPanel(MainFrame.LOBBY_PANEL));

        JLabel titleLabel = new JLabel("LUCKY WHEEL", SwingConstants.CENTER);
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

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);

        // 左側：獎品列表
        JPanel prizeListPanel = createPrizeListPanel();

        // 中央：轉盤
        wheelPanel = new WheelPanel();

        // 右側：說明
        JPanel infoPanel = createInfoPanel();

        panel.add(prizeListPanel, BorderLayout.WEST);
        panel.add(wheelPanel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createPrizeListPanel() {
        JPanel panel = new JPanel(new GridLayout(PRIZES.length + 1, 1, 2, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 60));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(140, 300));
        panel.setOpaque(false);

        JLabel title = new JLabel("PRIZES", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(Constants.CASINO_GOLD);
        panel.add(title);

        for (int i = 0; i < PRIZES.length; i++) {
            JLabel label = new JLabel(PRIZES[i], SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            label.setForeground(SEGMENT_COLORS[i].brighter());
            panel.add(label);
        }

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 5, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 40, 60));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(160, 300));
        panel.setOpaque(false);

        JLabel title = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(Constants.CASINO_GOLD);

        String[] info = {
                "1. Spin the wheel",
                "2. Win the prize!",
                "Free spin: Daily",
                "Paid spin: $" + spinCost,
                "JACKPOT: $10,000!"
        };

        panel.add(title);
        for (String s : info) {
            JLabel label = new JLabel(s, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 11));
            label.setForeground(Color.WHITE);
            panel.add(label);
        }

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(30, 30, 50));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));

        messageLabel = new JLabel("Spin the wheel to win!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        messageLabel.setForeground(Constants.CASINO_GOLD);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        buttonPanel.setOpaque(false);

        freeSpinButton = UIHelper.createSuccessButton("FREE SPIN");
        freeSpinButton.setPreferredSize(new Dimension(140, 50));
        freeSpinButton.addActionListener(e -> spinWheel(true));

        paidSpinButton = UIHelper.createPrimaryButton("SPIN ($" + spinCost + ")");
        paidSpinButton.setPreferredSize(new Dimension(140, 50));
        paidSpinButton.addActionListener(e -> spinWheel(false));

        buttonPanel.add(freeSpinButton);
        buttonPanel.add(paidSpinButton);

        panel.add(messageLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }

    private void spinWheel(boolean isFree) {
        if (spinning) return;

        Player player = mainFrame.getCurrentPlayer();

        if (isFree) {
            if (!player.canClaimDailyBonus()) {
                messageLabel.setText("Free spin used! Pay $" + spinCost + " to spin.");
                messageLabel.setForeground(Color.ORANGE);
                return;
            }
        } else {
            if (player.getBalance() < spinCost) {
                messageLabel.setText("Not enough chips!");
                messageLabel.setForeground(Color.RED);
                AnimationManager.shake(messageLabel, null);
                return;
            }
            player.placeBet(spinCost);
            updateBalanceDisplay();
        }

        spinning = true;
        freeSpinButton.setEnabled(false);
        paidSpinButton.setEnabled(false);
        messageLabel.setText("Spinning...");
        messageLabel.setForeground(Color.WHITE);

        // 決定結果 (加權隨機)
        int result = getWeightedResult();
        wheelPanel.spin(result, () -> {
            int prize = PRIZE_VALUES[result];
            player.addWinnings(prize);

            if (isFree) {
                // 標記已使用每日獎勵
                player.claimDailyBonus();
            }

            PlayerService.getInstance().savePlayers();

            String prizeText = PRIZES[result];
            if (result == 8) { // JACKPOT
                messageLabel.setText("JACKPOT!!! You won $10,000!");
                messageLabel.setForeground(new Color(255, 215, 0));
                AnimationManager.pulse(messageLabel, 5, null);
            } else {
                messageLabel.setText("You won " + prizeText + "!");
                messageLabel.setForeground(Constants.CASINO_GOLD);
                AnimationManager.pulse(messageLabel, 3, null);
            }

            AnimationManager.countUp(balanceLabel, player.getBalance() - prize,
                    player.getBalance(), 800, "$", "");

            spinning = false;
            freeSpinButton.setEnabled(player.canClaimDailyBonus());
            paidSpinButton.setEnabled(true);
        });
    }

    private int getWeightedResult() {
        // 加權隨機 - 高獎品機率較低
        int[] weights = {15, 12, 18, 8, 20, 3, 16, 10, 1, 14, 17, 6};
        int totalWeight = 0;
        for (int w : weights) totalWeight += w;

        int rand = random.nextInt(totalWeight);
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += weights[i];
            if (rand < sum) return i;
        }
        return 0;
    }

    private void updateBalanceDisplay() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null) {
            balanceLabel.setText("$" + String.format("%,d", player.getBalance()));
        }
    }

    public void onShow() {
        updateBalanceDisplay();
        Player player = mainFrame.getCurrentPlayer();
        freeSpinButton.setEnabled(player != null && player.canClaimDailyBonus());
    }

    // ========== 轉盤面板 ==========

    private class WheelPanel extends JPanel {
        private double rotation = 0;
        private boolean isSpinning = false;
        private Timer spinTimer;
        private Runnable onComplete;
        private int targetSegment = 0;

        // 動畫參數
        private double startRotation = 0;
        private double targetRotation = 0;
        private int animationFrame = 0;
        private int totalFrames = 180;  // 約 3 秒動畫

        public WheelPanel() {
            setPreferredSize(new Dimension(350, 350));
            setOpaque(false);
        }

        public void spin(int resultIndex, Runnable callback) {
            this.targetSegment = resultIndex;
            this.onComplete = callback;
            this.isSpinning = true;
            this.animationFrame = 0;

            // 計算目標角度 (指針在右邊 = 0度)
            // 要讓 segment i 的中心對準指針，需要 rotation = -(i * segmentAngle + segmentAngle/2)
            double segmentAngle = 360.0 / PRIZES.length;
            double targetAngle = -(targetSegment * segmentAngle + segmentAngle / 2);

            // 正規化到 0-360
            targetAngle = ((targetAngle % 360) + 360) % 360;

            // 從當前位置開始，轉至少 5 圈再停在目標
            this.startRotation = rotation;
            double currentNormalized = ((startRotation % 360) + 360) % 360;
            double diff = targetAngle - currentNormalized;
            if (diff <= 0) diff += 360;  // 確保往前轉
            this.targetRotation = startRotation + 360 * 5 + diff;

            spinTimer = new Timer(16, e -> updateSpin());
            spinTimer.start();
        }

        private void updateSpin() {
            animationFrame++;

            // 使用 easeOutCubic 緩動函數，讓減速更自然
            double progress = (double) animationFrame / totalFrames;
            double easedProgress = 1 - Math.pow(1 - progress, 3);

            // 計算當前角度
            rotation = startRotation + (targetRotation - startRotation) * easedProgress;

            repaint();

            // 動畫結束
            if (animationFrame >= totalFrames) {
                spinTimer.stop();
                isSpinning = false;
                rotation = targetRotation % 360;
                repaint();

                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radius = Math.min(getWidth(), getHeight()) / 2 - 30;

            // 外框光暈
            for (int i = 5; i > 0; i--) {
                g2.setColor(new Color(255, 215, 0, 30 * i));
                g2.fillOval(centerX - radius - 15 - i * 3, centerY - radius - 15 - i * 3,
                        (radius + 15 + i * 3) * 2, (radius + 15 + i * 3) * 2);
            }

            // 外框
            g2.setColor(new Color(139, 90, 43));
            g2.fillOval(centerX - radius - 15, centerY - radius - 15,
                    (radius + 15) * 2, (radius + 15) * 2);

            g2.setColor(Constants.CASINO_GOLD);
            g2.setStroke(new BasicStroke(4));
            g2.drawOval(centerX - radius - 15, centerY - radius - 15,
                    (radius + 15) * 2, (radius + 15) * 2);

            // 繪製扇區
            double segmentAngle = 360.0 / PRIZES.length;
            for (int i = 0; i < PRIZES.length; i++) {
                double startAngle = i * segmentAngle + rotation;

                g2.setColor(SEGMENT_COLORS[i]);
                g2.fill(new Arc2D.Double(centerX - radius, centerY - radius,
                        radius * 2, radius * 2,
                        startAngle, segmentAngle, Arc2D.PIE));

                // 繪製獎品文字
                double textAngle = Math.toRadians(startAngle + segmentAngle / 2);
                int textRadius = radius - 40;
                int textX = centerX + (int) (textRadius * Math.cos(textAngle));
                int textY = centerY - (int) (textRadius * Math.sin(textAngle));

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                FontMetrics fm = g2.getFontMetrics();
                String prize = PRIZES[i];
                g2.drawString(prize, textX - fm.stringWidth(prize) / 2, textY + 4);
            }

            // 中心圓
            g2.setColor(new Color(50, 50, 70));
            g2.fillOval(centerX - 35, centerY - 35, 70, 70);
            g2.setColor(Constants.CASINO_GOLD);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(centerX - 35, centerY - 35, 70, 70);

            // SPIN 文字
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("SPIN", centerX - fm.stringWidth("SPIN") / 2, centerY + 5);

            // 指針 (右側)
            g2.setColor(Constants.CASINO_GOLD);
            int[] arrowX = {centerX + radius + 10, centerX + radius + 25, centerX + radius + 25};
            int[] arrowY = {centerY, centerY - 12, centerY + 12};
            g2.fillPolygon(arrowX, arrowY, 3);

            // 指針邊框
            g2.setColor(new Color(180, 140, 0));
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(arrowX, arrowY, 3);

            g2.dispose();
        }
    }
}
