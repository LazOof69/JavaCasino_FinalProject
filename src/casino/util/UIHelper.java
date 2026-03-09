package casino.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UI 輔助工具類 - 提供統一的元件樣式
 * UI Helper - Provides unified component styles
 */
public class UIHelper {

    /**
     * 創建主要按鈕 (金色主題)
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, Constants.CASINO_GOLD, Constants.CASINO_BLACK,
                Constants.CASINO_GOLD.brighter(), Constants.CASINO_BLACK);
    }

    /**
     * 創建成功按鈕 (綠色主題)
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, new Color(34, 139, 34), Constants.CASINO_WHITE,
                new Color(50, 205, 50), Constants.CASINO_WHITE);
    }

    /**
     * 創建危險按鈕 (紅色主題)
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, new Color(178, 34, 34), Constants.CASINO_WHITE,
                new Color(220, 20, 60), Constants.CASINO_WHITE);
    }

    /**
     * 創建次要按鈕 (灰色主題)
     */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, new Color(70, 70, 70), Constants.CASINO_WHITE,
                new Color(100, 100, 100), Constants.CASINO_WHITE);
    }

    /**
     * 創建遊戲按鈕 (帶陰影效果)
     */
    public static JButton createGameButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 繪製陰影
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fill(new RoundRectangle2D.Double(3, 3, getWidth() - 4, getHeight() - 4, 15, 15));

                // 繪製按鈕背景
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 4, getHeight() - 4, 15, 15));

                // 繪製邊框光澤
                g2.setColor(new Color(255, 255, 255, 80));
                g2.drawLine(5, 2, getWidth() - 10, 2);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(Constants.BUTTON_FONT);
        button.setForeground(Constants.CASINO_BLACK);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 50));

        return button;
    }

    /**
     * 創建統一樣式按鈕
     */
    private static JButton createStyledButton(String text, Color bgColor, Color fgColor,
                                               Color hoverBg, Color hoverFg) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            private float hoverProgress = 0f;
            private Timer hoverTimer;

            {
                // 初始化 hover 動畫
                hoverTimer = new Timer(16, e -> {
                    if (isHovered && hoverProgress < 1f) {
                        hoverProgress = Math.min(1f, hoverProgress + 0.15f);
                        repaint();
                    } else if (!isHovered && hoverProgress > 0f) {
                        hoverProgress = Math.max(0f, hoverProgress - 0.15f);
                        repaint();
                    } else {
                        hoverTimer.stop();
                    }
                });

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHovered = true;
                        hoverTimer.start();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHovered = false;
                        hoverTimer.start();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 計算過渡顏色
                Color currentBg = interpolateColor(bgColor, hoverBg, hoverProgress);
                Color currentFg = interpolateColor(fgColor, hoverFg, hoverProgress);

                // 繪製圓角背景
                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));

                // 繪製邊框
                g2.setColor(currentBg.brighter());
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 12, 12));

                // 繪製文字
                g2.setColor(currentFg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        button.setFont(Constants.BUTTON_FONT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 45));

        return button;
    }

    /**
     * 顏色插值
     */
    private static Color interpolateColor(Color c1, Color c2, float t) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        return new Color(r, g, b);
    }

    /**
     * 創建卡片面板 (帶陰影)
     */
    public static JPanel createCardPanel(Color bgColor) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 繪製陰影
                for (int i = 0; i < 5; i++) {
                    g2.setColor(new Color(0, 0, 0, 20 - i * 4));
                    g2.fill(new RoundRectangle2D.Double(i, i, getWidth() - i * 2, getHeight() - i * 2, 20, 20));
                }

                // 繪製背景
                g2.setColor(bgColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 5, getHeight() - 5, 20, 20));

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    /**
     * 創建漸層背景面板
     */
    public static JPanel createGradientPanel(Color startColor, Color endColor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                GradientPaint gradient = new GradientPaint(
                        0, 0, startColor,
                        0, getHeight(), endColor
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.dispose();
            }
        };
    }

    /**
     * 創建撲克牌元件
     */
    public static JPanel createPlayingCard(String rank, String suit, boolean isRed, boolean faceUp) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 卡片陰影
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Double(4, 4, getWidth() - 4, getHeight() - 4, 10, 10));

                if (faceUp) {
                    // 正面 - 白色背景
                    g2.setColor(Color.WHITE);
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 4, getHeight() - 4, 10, 10));

                    // 邊框
                    g2.setColor(Color.DARK_GRAY);
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 6, getHeight() - 6, 10, 10));

                    // 文字
                    g2.setColor(isRed ? Color.RED : Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 24));
                    FontMetrics fm = g2.getFontMetrics();

                    String text = rank + suit;
                    int x = (getWidth() - 4 - fm.stringWidth(text)) / 2;
                    int y = (getHeight() - 4 + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(text, x, y);
                } else {
                    // 背面 - 藍色花紋
                    g2.setColor(new Color(0, 0, 139));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 4, getHeight() - 4, 10, 10));

                    // 花紋
                    g2.setColor(new Color(0, 0, 100));
                    for (int i = 0; i < getWidth(); i += 8) {
                        g2.drawLine(i, 0, i, getHeight());
                    }
                    for (int i = 0; i < getHeight(); i += 8) {
                        g2.drawLine(0, i, getWidth(), i);
                    }

                    // 邊框
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.draw(new RoundRectangle2D.Double(3, 3, getWidth() - 10, getHeight() - 10, 8, 8));
                }

                g2.dispose();
            }
        };

        card.setPreferredSize(new Dimension(80, 110));
        card.setOpaque(false);
        return card;
    }

    /**
     * 添加懸停縮放效果
     */
    public static void addHoverScale(JComponent component, double scale) {
        Dimension original = component.getPreferredSize();

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                int newW = (int) (original.width * scale);
                int newH = (int) (original.height * scale);
                component.setPreferredSize(new Dimension(newW, newH));
                component.revalidate();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                component.setPreferredSize(original);
                component.revalidate();
            }
        });
    }
}
