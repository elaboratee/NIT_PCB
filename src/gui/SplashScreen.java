package gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;

/**
 * Класс, определяющий сплэш-скрин
 */
public class SplashScreen extends JFrame {

    private static SplashScreen instance;

    private final URL iconURL = getClass().getClassLoader().getResource("icon.png");
    private final URL screenURL = getClass().getClassLoader().getResource("splash_screen.png");

    private SplashScreen() {
        super("SurfaceScout");
        configureSplashScreen();
    }

    /**
     * Метод для получения экземпляра синглтона сплэш-скрина
     *
     * @return экземпляр сплэш-скрина
     */
    public static synchronized SplashScreen getInstance() {
        if (instance == null) {
            instance = new SplashScreen();
        }
        return instance;
    }

    /**
     * Метод для конфигурации сплэш-скрина
     */
    private void configureSplashScreen() {
        // Конфигурация фрейма сплэш-экрана
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setAlwaysOnTop(true);
        setSize(700, 400);
        setLocationRelativeTo(null);

        // Создание JLayeredPane для управления слоями
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        add(layeredPane);

        // Добавление иконки
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());

        // Добавление изображения
        ImageIcon imageIcon = new ImageIcon(screenURL);


        imageIcon.setImage(imageIcon.getImage().getScaledInstance(680, 380, Image.SCALE_SMOOTH));
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setBounds(10, 10, imageIcon.getIconWidth(), imageIcon.getIconHeight());
        layeredPane.add(imageLabel, JLayeredPane.DEFAULT_LAYER);

        // Добавление рамки с анимацией
        Rectangle targetBounds = imageLabel.getBounds();
        BorderProgressBar borderProgressBar = new BorderProgressBar(targetBounds);
        borderProgressBar.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(borderProgressBar, JLayeredPane.PALETTE_LAYER);

        // Запуск анимации прогресс-бара
        borderProgressBar.startAnimation();
    }

    /**
     * Метод для отображения сплэш-скрина
     */
    public void showSplashScreen() {
        setVisible(true);
    }

    /**
     * Метод для закрытия сплэш-скрина
     */
    public void closeSplashScreen() {
        dispose();
        instance = null;
    }
}
