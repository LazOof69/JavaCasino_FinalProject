package casino;

import casino.gui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 賭場遊戲主程式入口
 * Casino Game Main Entry Point
 */
public class Main {

    public static void main(String[] args) {
        // 設定系統外觀
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 在 Event Dispatch Thread 中啟動 GUI
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
