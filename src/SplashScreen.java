import javax.swing.*;
import java.awt.*;

public class SplashScreen {

    private static JFrame splashFrame;

    public static void showSplashScreen() {
        // Создание фрейма сплэш-экрана
        splashFrame = new JFrame("Экран загрузки");

        // Конфигурация фрейма сплэш-экрана
        splashFrame.setUndecorated(true);
        splashFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        splashFrame.setResizable(false);
        splashFrame.setAlwaysOnTop(true);
        splashFrame.setSize(700, 400);
        splashFrame.setLocationRelativeTo(null);

        // Создание JLayeredPane для управления слоями
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        // Добавление иконки
        ImageIcon icon = new ImageIcon("img/icon.png");
        splashFrame.setIconImage(icon.getImage());

        // Добавление изображения
        ImageIcon imageIcon = new ImageIcon("img/splash_screen.png");
        imageIcon.setImage(imageIcon.getImage().getScaledInstance(680, 380, Image.SCALE_SMOOTH));
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setBounds(10, 10, imageIcon.getIconWidth(), imageIcon.getIconHeight());
        layeredPane.add(imageLabel, JLayeredPane.DEFAULT_LAYER);

        // Добавление рамки с анимацией
        BorderProgressBar borderProgressBar = new BorderProgressBar(imageLabel);
        borderProgressBar.setBounds(0, 0, splashFrame.getWidth(), splashFrame.getHeight());
        layeredPane.add(borderProgressBar, JLayeredPane.PALETTE_LAYER);

        splashFrame.add(layeredPane);
        splashFrame.setVisible(true);

        borderProgressBar.startAnimation();
    }

    public static void closeSplashScreen() {
        if (splashFrame != null) {
            splashFrame.dispose();
        }
    }
}
