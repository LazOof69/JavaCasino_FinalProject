package casino.gui;

import casino.service.PlayerService;
import casino.util.AnimationManager;
import casino.util.Constants;
import casino.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 登入/註冊面板 (優化版)
 * Login/Register Panel (Enhanced)
 */
public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 背景面板
        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // 漸層背景
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(10, 10, 30),
                        getWidth(), getHeight(), new Color(30, 0, 20)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 裝飾性圖案
                g2.setColor(new Color(255, 215, 0, 20));
                for (int i = 0; i < 20; i++) {
                    int x = (int) (Math.random() * getWidth());
                    int y = (int) (Math.random() * getHeight());
                    int size = (int) (Math.random() * 100 + 50);
                    g2.fillOval(x, y, size, size);
                }

                g2.dispose();
            }
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 登入卡片
        JPanel loginCard = createLoginCard();
        bgPanel.add(loginCard, gbc);

        add(bgPanel, BorderLayout.CENTER);

        // 底部版權
        JLabel copyrightLabel = new JLabel("Java Casino - Final Project 2025", SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        copyrightLabel.setForeground(new Color(150, 150, 150));
        copyrightLabel.setOpaque(true);
        copyrightLabel.setBackground(new Color(20, 20, 30));
        copyrightLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(copyrightLabel, BorderLayout.SOUTH);
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 陰影
                for (int i = 0; i < 10; i++) {
                    g2.setColor(new Color(0, 0, 0, 20 - i * 2));
                    g2.fill(new RoundRectangle2D.Double(i, i, getWidth() - i * 2, getHeight() - i * 2, 30, 30));
                }

                // 卡片背景
                GradientPaint cardBg = new GradientPaint(0, 0, new Color(50, 50, 60),
                        0, getHeight(), new Color(30, 30, 40));
                g2.setPaint(cardBg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 10, getHeight() - 10, 25, 25));

                // 邊框
                g2.setColor(Constants.CASINO_GOLD);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 12, getHeight() - 12, 25, 25));

                g2.dispose();
            }
        };
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(450, 480));
        card.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 標題
        JLabel titleLabel = new JLabel("JAVA CASINO", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Constants.CASINO_GOLD);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 15, 5, 15);
        card.add(titleLabel, gbc);

        // 副標題
        JLabel subtitleLabel = new JLabel("Blackjack | Slots | Roulette", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(180, 180, 180));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 15, 25, 15);
        card.add(subtitleLabel, gbc);

        // 用戶名
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(Constants.CASINO_WHITE);
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 20, 2, 20);
        card.add(userLabel, gbc);

        usernameField = createStyledTextField();
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 20, 10, 20);
        card.add(usernameField, gbc);

        // 密碼
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passLabel.setForeground(Constants.CASINO_WHITE);
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 20, 2, 20);
        card.add(passLabel, gbc);

        passwordField = createStyledPasswordField();
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 20, 15, 20);
        card.add(passwordField, gbc);

        // 訊息標籤
        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 13));
        messageLabel.setForeground(Color.RED);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 20, 10, 20);
        card.add(messageLabel, gbc);

        // 按鈕區
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        JButton loginButton = UIHelper.createPrimaryButton("LOGIN");
        JButton registerButton = UIHelper.createSuccessButton("REGISTER");

        loginButton.setPreferredSize(new Dimension(150, 50));
        registerButton.setPreferredSize(new Dimension(150, 50));

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> handleRegister());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridy = 7;
        gbc.insets = new Insets(10, 20, 20, 20);
        card.add(buttonPanel, gbc);

        // 提示
        JLabel hintLabel = new JLabel("New player? Click REGISTER to create account", SwingConstants.CENTER);
        hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(120, 120, 120));
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 20, 15, 20);
        card.add(hintLabel, gbc);

        // Enter 鍵登入
        passwordField.addActionListener(e -> handleLogin());

        return card;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 背景
                g2.setColor(new Color(40, 40, 50));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setForeground(Constants.CASINO_WHITE);
        field.setCaretColor(Constants.CASINO_GOLD);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(40, 40, 50));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));

                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setFont(new Font("Arial", Font.PLAIN, 16));
        field.setForeground(Constants.CASINO_WHITE);
        field.setCaretColor(Constants.CASINO_GOLD);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return field;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password", true);
            AnimationManager.shake(messageLabel, null);
            return;
        }

        if (PlayerService.getInstance().login(username, password)) {
            clearFields();
            mainFrame.onLoginSuccess();
        } else {
            showMessage("Invalid username or password", true);
            AnimationManager.shake(messageLabel, null);
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty()) {
            showMessage("Please enter username", true);
            AnimationManager.shake(messageLabel, null);
            return;
        }
        if (username.length() < 3) {
            showMessage("Username must be at least 3 characters", true);
            AnimationManager.shake(messageLabel, null);
            return;
        }
        if (password.length() < 4) {
            showMessage("Password must be at least 4 characters", true);
            AnimationManager.shake(messageLabel, null);
            return;
        }
        if (PlayerService.getInstance().isUsernameTaken(username)) {
            showMessage("Username already exists", true);
            AnimationManager.shake(messageLabel, null);
            return;
        }

        if (PlayerService.getInstance().register(username, password)) {
            showMessage("Registration successful! Please login.", false);
            passwordField.setText("");
        } else {
            showMessage("Registration failed", true);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setForeground(isError ? new Color(255, 100, 100) : Constants.CASINO_GOLD);
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
    }
}
