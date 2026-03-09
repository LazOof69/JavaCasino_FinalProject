package casino.gui;

import casino.game.card.*;
import casino.model.Player;
import casino.service.PlayerService;
import casino.util.AnimationManager;
import casino.util.Constants;
import casino.util.UIHelper;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 21點遊戲面板 (帶動畫效果)
 * Blackjack Game Panel (with animations)
 */
public class BlackjackPanel extends JPanel {

    private MainFrame mainFrame;

    // 遊戲元件
    private Deck deck;
    private Hand playerHand;
    private Hand dealerHand;
    private int currentBet;
    private boolean gameInProgress;
    private boolean animating;

    // UI 元件
    private JLabel balanceLabel;
    private JLabel betLabel;
    private JLabel messageLabel;
    private JLabel playerScoreLabel;
    private JLabel dealerScoreLabel;
    private JPanel playerCardsPanel;
    private JPanel dealerCardsPanel;
    private JButton hitButton;
    private JButton standButton;
    private JButton doubleButton;
    private JButton dealButton;
    private JSpinner betSpinner;

    // 動畫相關
    private List<CardPanel> playerCardPanels;
    private List<CardPanel> dealerCardPanels;
    private JPanel deckPanel;

    public BlackjackPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.playerCardPanels = new ArrayList<>();
        this.dealerCardPanels = new ArrayList<>();
        initializeGame();
        initializeUI();
    }

    private void initializeGame() {
        deck = new Deck();
        playerHand = new Hand();
        dealerHand = new Hand();
        currentBet = 0;
        gameInProgress = false;
        animating = false;
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Constants.CASINO_GREEN);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createGameArea(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 60, 0),
                        0, getHeight(), new Color(0, 40, 0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = UIHelper.createSecondaryButton("<< Back");
        backButton.setPreferredSize(new Dimension(100, 35));
        backButton.addActionListener(e -> {
            if (gameInProgress) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Game in progress! Leave and forfeit bet?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;
            }
            resetGame();
            mainFrame.showPanel(MainFrame.LOBBY_PANEL);
        });

        JLabel titleLabel = new JLabel("BLACKJACK", SwingConstants.CENTER);
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
        JPanel panel = new JPanel(new BorderLayout(0, 30));
        panel.setOpaque(false);

        // 牌組顯示區 (右上角)
        deckPanel = createDeckPanel();

        // 莊家區域
        JPanel dealerArea = createPlayerArea("DEALER", true);
        dealerCardsPanel = (JPanel) ((JPanel) dealerArea.getComponent(1)).getComponent(0);
        dealerScoreLabel = (JLabel) dealerArea.getComponent(0);

        // 玩家區域
        JPanel playerArea = createPlayerArea("YOUR HAND", false);
        playerCardsPanel = (JPanel) ((JPanel) playerArea.getComponent(1)).getComponent(0);
        playerScoreLabel = (JLabel) playerArea.getComponent(0);

        // 組合面板
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(dealerArea, BorderLayout.CENTER);
        topSection.add(deckPanel, BorderLayout.EAST);

        panel.add(topSection, BorderLayout.NORTH);
        panel.add(playerArea, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPlayerArea(String title, boolean isDealer) {
        JPanel area = new JPanel(new BorderLayout(0, 10));
        area.setOpaque(false);

        // 分數標籤
        JLabel scoreLabel = new JLabel(title + ": --", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Constants.CASINO_WHITE);

        // 牌區容器
        JPanel cardsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, -20, 0));
        cardsContainer.setOpaque(false);
        cardsContainer.setPreferredSize(new Dimension(600, 130));

        // 外框
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0, 100), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        wrapper.add(cardsContainer, BorderLayout.CENTER);

        area.add(scoreLabel, BorderLayout.NORTH);
        area.add(wrapper, BorderLayout.CENTER);

        return area;
    }

    private JPanel createDeckPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 繪製多層牌背 (模擬牌組)
                for (int i = 4; i >= 0; i--) {
                    int offset = i * 2;
                    g2.setColor(new Color(0, 0, 139 - i * 10));
                    g2.fill(new RoundRectangle2D.Double(10 + offset, 10 + offset, 60, 85, 8, 8));
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1));
                    g2.draw(new RoundRectangle2D.Double(10 + offset, 10 + offset, 60, 85, 8, 8));
                }

                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(90, 120));
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 50, 0));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 訊息標籤
        messageLabel = new JLabel("Place your bet and click DEAL!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 22));
        messageLabel.setForeground(Constants.CASINO_GOLD);

        // 下注區
        JPanel betPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        betPanel.setOpaque(false);

        JLabel betTextLabel = new JLabel("BET:");
        betTextLabel.setFont(new Font("Arial", Font.BOLD, 18));
        betTextLabel.setForeground(Constants.CASINO_WHITE);

        // 快速下注按鈕
        JButton minBtn = createQuickBetButton("MIN", 100);
        JButton halfBtn = createQuickBetButton("1/2", -1);

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 100, 10000, 100);
        betSpinner = new JSpinner(spinnerModel);
        betSpinner.setFont(new Font("Arial", Font.BOLD, 16));
        ((JSpinner.DefaultEditor) betSpinner.getEditor()).getTextField().setColumns(5);

        JButton doubleBtn = createQuickBetButton("x2", -2);
        JButton maxBtn = createQuickBetButton("MAX", 10000);

        betLabel = new JLabel("Current: $0");
        betLabel.setFont(new Font("Arial", Font.BOLD, 16));
        betLabel.setForeground(Constants.CASINO_GOLD);

        betPanel.add(betTextLabel);
        betPanel.add(minBtn);
        betPanel.add(halfBtn);
        betPanel.add(betSpinner);
        betPanel.add(doubleBtn);
        betPanel.add(maxBtn);
        betPanel.add(Box.createHorizontalStrut(20));
        betPanel.add(betLabel);

        // 按鈕區
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        dealButton = UIHelper.createPrimaryButton("DEAL");
        hitButton = UIHelper.createSuccessButton("HIT");
        standButton = UIHelper.createGameButton("STAND", new Color(255, 140, 0));
        doubleButton = UIHelper.createDangerButton("DOUBLE");

        dealButton.addActionListener(e -> startNewGame());
        hitButton.addActionListener(e -> hit());
        standButton.addActionListener(e -> stand());
        doubleButton.addActionListener(e -> doubleDown());

        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);

        buttonPanel.add(dealButton);
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(doubleButton);

        panel.add(messageLabel, BorderLayout.NORTH);
        panel.add(betPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ========== 遊戲邏輯 ==========

    private void startNewGame() {
        if (animating) return;

        Player player = mainFrame.getCurrentPlayer();
        int betAmount = (int) betSpinner.getValue();

        if (player.getBalance() < betAmount) {
            showMessage("Insufficient balance!", Color.RED);
            AnimationManager.shake(messageLabel, null);
            return;
        }

        player.placeBet(betAmount);
        currentBet = betAmount;
        updateBalanceDisplay();
        betLabel.setText("Current: $" + currentBet);

        // 清空
        deck.reset();
        playerHand.clear();
        dealerHand.clear();
        playerCardsPanel.removeAll();
        dealerCardsPanel.removeAll();
        playerCardPanels.clear();
        dealerCardPanels.clear();

        gameInProgress = true;
        animating = true;
        updateButtons(false);
        showMessage("Dealing cards...", Constants.CASINO_WHITE);

        // 發牌動畫序列
        dealInitialCards();
    }

    private void dealInitialCards() {
        // 發4張牌，依序動畫
        Timer dealTimer = new Timer(400, null);
        final int[] cardIndex = {0};

        dealTimer.addActionListener(e -> {
            switch (cardIndex[0]) {
                case 0:
                    dealCardWithAnimation(playerHand, playerCardsPanel, playerCardPanels, true);
                    break;
                case 1:
                    dealCardWithAnimation(dealerHand, dealerCardsPanel, dealerCardPanels, true);
                    break;
                case 2:
                    dealCardWithAnimation(playerHand, playerCardsPanel, playerCardPanels, true);
                    break;
                case 3:
                    dealCardWithAnimation(dealerHand, dealerCardsPanel, dealerCardPanels, false);
                    break;
                case 4:
                    dealTimer.stop();
                    animating = false;
                    updateScores();
                    checkInitialBlackjack();
                    break;
            }
            cardIndex[0]++;
        });
        dealTimer.setInitialDelay(200);
        dealTimer.start();
    }

    private void dealCardWithAnimation(Hand hand, JPanel cardsPanel,
                                         List<CardPanel> cardPanelList, boolean faceUp) {
        Card card = faceUp ? deck.deal() : deck.dealFaceDown();
        hand.addCard(card);

        CardPanel cardPanel = new CardPanel(card);
        cardPanelList.add(cardPanel);

        // 初始位置 (牌組位置)
        cardPanel.setBounds(getWidth() - 100, -120, 80, 110);
        cardsPanel.setLayout(null);
        cardsPanel.add(cardPanel);

        // 目標位置
        int targetX = cardPanelList.size() * 60 + 50;
        int targetY = 10;

        // 滑入動畫
        animateCardDeal(cardPanel, targetX, targetY, () -> {
            // 動畫結束後重新佈局
            cardsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, -15, 0));
            cardsPanel.revalidate();
            cardsPanel.repaint();
            updateScores();
        });
    }

    private void animateCardDeal(CardPanel card, int targetX, int targetY, Runnable onComplete) {
        Timer timer = new Timer(16, null);
        Point start = card.getLocation();
        final long startTime = System.currentTimeMillis();
        final int duration = 300;

        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);

            // 緩動效果
            progress = easeOutBack(progress);

            int x = (int) (start.x + (targetX - start.x) * progress);
            int y = (int) (start.y + (targetY - start.y) * progress);
            card.setLocation(x, y);

            if (progress >= 1f) {
                card.setLocation(targetX, targetY);
                timer.stop();
                if (onComplete != null) onComplete.run();
            }
        });
        timer.start();
    }

    private float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private void checkInitialBlackjack() {
        if (playerHand.isBlackjack()) {
            revealDealerCard(() -> {
                if (dealerHand.isBlackjack()) {
                    endGame("Push! Both Blackjack!", currentBet, false);
                } else {
                    endGame("BLACKJACK! You win!", (int) (currentBet * 2.5), true);
                }
            });
        } else {
            updateButtons(true);
            showMessage("Your turn: Hit or Stand?", Constants.CASINO_GOLD);
        }
    }

    private void hit() {
        if (animating) return;
        animating = true;

        dealCardWithAnimation(playerHand, playerCardsPanel, playerCardPanels, true);

        AnimationManager.delay(350, () -> {
            animating = false;
            updateScores();

            if (playerHand.isBusted()) {
                endGame("BUSTED! You lose!", 0, false);
            } else if (playerHand.getValue() == 21) {
                stand();
            } else {
                doubleButton.setEnabled(false);
            }
        });
    }

    private void stand() {
        if (animating) return;
        animating = true;
        updateButtons(false);

        revealDealerCard(() -> {
            dealerPlay();
        });
    }

    private void revealDealerCard(Runnable onComplete) {
        if (!dealerCardPanels.isEmpty()) {
            CardPanel hiddenCard = dealerCardPanels.get(1);
            Card card = dealerHand.getCards().get(1);
            card.setFaceUp(true);

            // 翻牌動畫
            hiddenCard.flipCard(() -> {
                updateScores();
                AnimationManager.delay(300, onComplete);
            });
        } else {
            onComplete.run();
        }
    }

    private void dealerPlay() {
        if (dealerHand.getValue() >= Constants.DEALER_STAND) {
            determineWinner();
            return;
        }

        dealCardWithAnimation(dealerHand, dealerCardsPanel, dealerCardPanels, true);

        AnimationManager.delay(500, () -> {
            updateScores();
            dealerPlay();
        });
    }

    private void determineWinner() {
        animating = false;
        int playerValue = playerHand.getValue();
        int dealerValue = dealerHand.getValue();

        if (dealerHand.isBusted()) {
            endGame("Dealer BUSTED! You win!", currentBet * 2, true);
        } else if (playerValue > dealerValue) {
            endGame("You WIN! " + playerValue + " vs " + dealerValue, currentBet * 2, true);
        } else if (playerValue < dealerValue) {
            endGame("Dealer wins! " + dealerValue + " vs " + playerValue, 0, false);
        } else {
            endGame("PUSH! " + playerValue + " vs " + dealerValue, currentBet, false);
        }
    }

    private void doubleDown() {
        if (animating) return;

        Player player = mainFrame.getCurrentPlayer();
        if (player.getBalance() < currentBet) {
            showMessage("Insufficient balance to double!", Color.RED);
            AnimationManager.shake(messageLabel, null);
            return;
        }

        player.placeBet(currentBet);
        currentBet *= 2;
        betLabel.setText("Current: $" + currentBet);
        updateBalanceDisplay();

        animating = true;
        updateButtons(false);

        dealCardWithAnimation(playerHand, playerCardsPanel, playerCardPanels, true);

        AnimationManager.delay(400, () -> {
            updateScores();
            animating = false;  // Reset animation flag before proceeding
            if (playerHand.isBusted()) {
                endGame("BUSTED! You lose!", 0, false);
            } else {
                stand();  // Automatically stand after double down
            }
        });
    }

    private void endGame(String message, int winnings, boolean won) {
        gameInProgress = false;
        animating = false;
        Player player = mainFrame.getCurrentPlayer();

        if (winnings > 0) {
            player.addWinnings(winnings);
        }

        player.recordGame("Blackjack", currentBet, winnings, won);
        PlayerService.getInstance().savePlayers();

        // 動畫餘額更新
        int oldBalance = player.getBalance() - (won ? (winnings - currentBet) : 0);
        AnimationManager.countUp(balanceLabel, oldBalance, player.getBalance(), 500, "$", "");

        updateButtons(false);
        dealButton.setEnabled(true);
        betSpinner.setEnabled(true);

        if (won && winnings > currentBet) {
            showMessage(message, Constants.CASINO_GOLD);
            AnimationManager.pulse(messageLabel, 3, null);
            // 贏牌彈跳效果
            for (CardPanel cp : playerCardPanels) {
                AnimationManager.bounce(cp, null);
            }
        } else if (winnings == currentBet) {
            showMessage(message, Color.YELLOW);
        } else {
            showMessage(message, Color.RED);
            AnimationManager.shake(messageLabel, null);
        }

        currentBet = 0;
        betLabel.setText("Current: $0");
    }

    private void resetGame() {
        gameInProgress = false;
        animating = false;
        currentBet = 0;
        playerHand.clear();
        dealerHand.clear();
        playerCardsPanel.removeAll();
        dealerCardsPanel.removeAll();
        playerCardPanels.clear();
        dealerCardPanels.clear();
        updateButtons(false);
        dealButton.setEnabled(true);
        betSpinner.setEnabled(true);
        showMessage("Place your bet and click DEAL!", Constants.CASINO_GOLD);
        betLabel.setText("Current: $0");
        playerCardsPanel.repaint();
        dealerCardsPanel.repaint();
    }

    // ========== UI 更新 ==========

    private void updateScores() {
        int playerValue = playerHand.getValue();
        dealerScoreLabel.setText("DEALER: " + (dealerHand.hasHiddenCard() ?
                dealerHand.getVisibleValue() + " + ?" : dealerHand.getValue()));
        playerScoreLabel.setText("YOUR HAND: " + playerValue);

        // 分數顏色
        if (playerValue == 21) {
            playerScoreLabel.setForeground(Constants.CASINO_GOLD);
        } else if (playerValue > 21) {
            playerScoreLabel.setForeground(Color.RED);
        } else {
            playerScoreLabel.setForeground(Constants.CASINO_WHITE);
        }
    }

    private void updateButtons(boolean gameActive) {
        dealButton.setEnabled(!gameActive && !animating);
        betSpinner.setEnabled(!gameActive && !animating);
        hitButton.setEnabled(gameActive && !animating);
        standButton.setEnabled(gameActive && !animating);
        doubleButton.setEnabled(gameActive && !animating && playerHand.size() == 2);
    }

    private void updateBalanceDisplay() {
        Player player = mainFrame.getCurrentPlayer();
        if (player != null) {
            balanceLabel.setText("$" + String.format("%,d", player.getBalance()));
        }
    }

    private void showMessage(String text, Color color) {
        messageLabel.setText(text);
        messageLabel.setForeground(color);
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
            if (gameInProgress) return;
            Player player = mainFrame.getCurrentPlayer();
            if (player == null) return;

            int newBet;
            if (value == -1) {
                newBet = Math.max(100, (player.getBalance() / 2 / 100) * 100);
            } else if (value == -2) {
                newBet = Math.min(10000, (int) betSpinner.getValue() * 2);
            } else {
                newBet = Math.min(value, player.getBalance());
            }
            newBet = Math.min(newBet, 10000);
            newBet = Math.max(newBet, 100);
            betSpinner.setValue(newBet);
        });

        return btn;
    }

    // ========== 內部類：卡片面板 ==========

    private class CardPanel extends JPanel {
        private Card card;
        private boolean flipping = false;
        private float flipProgress = 0f;

        public CardPanel(Card card) {
            this.card = card;
            setPreferredSize(new Dimension(80, 110));
            setOpaque(false);
        }

        public void flipCard(Runnable onComplete) {
            flipping = true;
            Timer timer = new Timer(16, null);
            final boolean[] halfway = {false};

            timer.addActionListener(e -> {
                if (!halfway[0]) {
                    flipProgress += 0.1f;
                    if (flipProgress >= 0.5f) {
                        halfway[0] = true;
                        card.setFaceUp(true);
                    }
                } else {
                    flipProgress += 0.1f;
                    if (flipProgress >= 1f) {
                        flipProgress = 0f;
                        flipping = false;
                        timer.stop();
                        if (onComplete != null) onComplete.run();
                    }
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 翻牌時的縮放效果
            if (flipping) {
                double scale = Math.abs(Math.cos(flipProgress * Math.PI));
                int newW = (int) (w * scale);
                int offsetX = (w - newW) / 2;
                g2.translate(offsetX, 0);
                w = newW;
            }

            // 陰影
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fill(new RoundRectangle2D.Double(4, 4, w - 4, h - 4, 10, 10));

            if (card.isFaceUp()) {
                // 正面
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 4, h - 4, 10, 10));

                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, w - 6, h - 6, 10, 10));

                // 牌面
                g2.setColor(card.isRed() ? Color.RED : Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String text = card.getRank().getSymbol() + card.getSuit().getSymbol();
                g2.drawString(text, 8, 22);

                // 中央大符號
                g2.setFont(new Font("Arial", Font.PLAIN, 36));
                String suit = card.getSuit().getSymbol();
                fm = g2.getFontMetrics();
                int x = (w - 4 - fm.stringWidth(suit)) / 2;
                int y = h / 2 + 10;
                g2.drawString(suit, x, y);
            } else {
                // 背面
                g2.setColor(new Color(0, 0, 139));
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 4, h - 4, 10, 10));

                // 圖案
                g2.setColor(new Color(0, 0, 100));
                for (int i = 5; i < w - 10; i += 6) {
                    for (int j = 5; j < h - 10; j += 6) {
                        g2.fillOval(i, j, 3, 3);
                    }
                }

                // 邊框
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(4, 4, w - 12, h - 12, 6, 6));
            }

            g2.dispose();
        }
    }
}
