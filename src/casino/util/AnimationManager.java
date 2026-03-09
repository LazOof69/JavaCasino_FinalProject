package casino.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * 動畫管理器 - 提供各種動畫效果
 * Animation Manager - Provides various animation effects
 */
public class AnimationManager {

    /**
     * 淡入效果
     */
    public static void fadeIn(JComponent component, int duration, Runnable onComplete) {
        component.setVisible(true);
        Timer timer = new Timer(16, null);
        final float[] opacity = {0f};

        timer.addActionListener(e -> {
            opacity[0] += 0.05f;
            if (opacity[0] >= 1f) {
                opacity[0] = 1f;
                timer.stop();
                if (onComplete != null) onComplete.run();
            }
            component.repaint();
        });
        timer.start();
    }

    /**
     * 滑入效果 (從上方)
     */
    public static void slideInFromTop(JComponent component, JPanel parent,
                                       Point targetPos, int duration, Runnable onComplete) {
        Point startPos = new Point(targetPos.x, -component.getHeight());
        component.setLocation(startPos);
        component.setVisible(true);

        animate(startPos, targetPos, duration, pos -> {
            component.setLocation(pos);
            parent.repaint();
        }, onComplete);
    }

    /**
     * 滑入效果 (從左側)
     */
    public static void slideInFromLeft(JComponent component, JPanel parent,
                                        Point targetPos, int duration, Runnable onComplete) {
        Point startPos = new Point(-component.getWidth(), targetPos.y);
        component.setLocation(startPos);
        component.setVisible(true);

        animate(startPos, targetPos, duration, pos -> {
            component.setLocation(pos);
            parent.repaint();
        }, onComplete);
    }

    /**
     * 彈跳效果
     */
    public static void bounce(JComponent component, Runnable onComplete) {
        Point original = component.getLocation();
        int bounceHeight = 20;

        Timer timer = new Timer(16, null);
        final int[] step = {0};
        final int totalSteps = 20;

        timer.addActionListener(e -> {
            step[0]++;
            double progress = (double) step[0] / totalSteps;
            // 使用正弦波產生彈跳效果
            double bounce = Math.sin(progress * Math.PI * 3) * (1 - progress) * bounceHeight;
            component.setLocation(original.x, (int)(original.y - bounce));

            if (step[0] >= totalSteps) {
                component.setLocation(original);
                timer.stop();
                if (onComplete != null) onComplete.run();
            }
        });
        timer.start();
    }

    /**
     * 搖晃效果 (輸錢時)
     */
    public static void shake(JComponent component, Runnable onComplete) {
        Point original = component.getLocation();

        Timer timer = new Timer(30, null);
        final int[] step = {0};
        final int[] offsets = {-10, 10, -8, 8, -5, 5, -2, 2, 0};

        timer.addActionListener(e -> {
            if (step[0] < offsets.length) {
                component.setLocation(original.x + offsets[step[0]], original.y);
                step[0]++;
            } else {
                component.setLocation(original);
                timer.stop();
                if (onComplete != null) onComplete.run();
            }
        });
        timer.start();
    }

    /**
     * 脈動效果 (贏錢時)
     */
    public static void pulse(JComponent component, int times, Runnable onComplete) {
        Timer timer = new Timer(100, null);
        final int[] count = {0};
        final Color originalBg = component.getBackground();

        timer.addActionListener(e -> {
            if (count[0] < times * 2) {
                if (count[0] % 2 == 0) {
                    component.setBackground(Constants.CASINO_GOLD);
                } else {
                    component.setBackground(originalBg);
                }
                count[0]++;
            } else {
                component.setBackground(originalBg);
                timer.stop();
                if (onComplete != null) onComplete.run();
            }
        });
        timer.start();
    }

    /**
     * 翻牌效果
     */
    public static void flipCard(JLabel cardLabel, String newText, Color newColor,
                                 Runnable onComplete) {
        Timer timer = new Timer(20, null);
        final int[] width = {cardLabel.getWidth()};
        final int originalWidth = width[0];
        final boolean[] flipped = {false};

        timer.addActionListener(e -> {
            if (!flipped[0]) {
                // 縮小寬度 (翻轉中)
                width[0] -= 8;
                if (width[0] <= 0) {
                    width[0] = 0;
                    flipped[0] = true;
                    // 更換內容
                    cardLabel.setText(newText);
                    cardLabel.setForeground(newColor);
                }
            } else {
                // 恢復寬度 (翻轉完成)
                width[0] += 8;
                if (width[0] >= originalWidth) {
                    width[0] = originalWidth;
                    timer.stop();
                    if (onComplete != null) onComplete.run();
                }
            }

            // 調整大小以產生翻轉視覺效果
            Dimension size = cardLabel.getPreferredSize();
            cardLabel.setBounds(
                cardLabel.getX() + (originalWidth - width[0]) / 2,
                cardLabel.getY(),
                width[0],
                size.height
            );
            cardLabel.getParent().repaint();
        });
        timer.start();
    }

    /**
     * 數字滾動效果
     */
    public static void countUp(JLabel label, int from, int to, int duration,
                                String prefix, String suffix) {
        Timer timer = new Timer(16, null);
        final long startTime = System.currentTimeMillis();

        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);

            // 緩動效果
            progress = easeOutQuad(progress);

            int current = (int) (from + (to - from) * progress);
            label.setText(prefix + String.format("%,d", current) + suffix);

            if (progress >= 1f) {
                label.setText(prefix + String.format("%,d", to) + suffix);
                timer.stop();
            }
        });
        timer.start();
    }

    /**
     * 通用動畫方法
     */
    private static void animate(Point from, Point to, int duration,
                                 Consumer<Point> onUpdate, Runnable onComplete) {
        Timer timer = new Timer(16, null);
        final long startTime = System.currentTimeMillis();

        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(1f, (float) elapsed / duration);

            // 緩動效果
            progress = easeOutBack(progress);

            int x = (int) (from.x + (to.x - from.x) * progress);
            int y = (int) (from.y + (to.y - from.y) * progress);

            onUpdate.accept(new Point(x, y));

            if (progress >= 1f) {
                onUpdate.accept(to);
                timer.stop();
                if (onComplete != null) onComplete.run();
            }
        });
        timer.start();
    }

    /**
     * 緩動函數 - 減速
     */
    private static float easeOutQuad(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    /**
     * 緩動函數 - 彈性回彈
     */
    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float)Math.pow(t - 1, 3) + c1 * (float)Math.pow(t - 1, 2);
    }

    /**
     * 延遲執行
     */
    public static void delay(int milliseconds, Runnable action) {
        Timer timer = new Timer(milliseconds, e -> action.run());
        timer.setRepeats(false);
        timer.start();
    }
}
