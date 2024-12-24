package gui;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Класс, определяющий главное окно приложения
 */
public class MainScreen extends JFrame {

    private static MainScreen instance;

    private static final Toolkit tk = Toolkit.getDefaultToolkit();
    private final URL iconURL = getClass().getClassLoader().getResource("icon.png");

    private MainScreen() {
        super("SurfaceScout");
        configureMainScreen();
    }

    /**
     * Метод для получения экземпляра синглтона основного окна приложения
     *
     * @return экземпляр основного окна
     */
    public static synchronized MainScreen getInstance() {
        if (instance == null) {
            instance = new MainScreen();
        }
        return instance;
    }

    /**
     * Метод для конфигурации основного окна приложения
     */
    private void configureMainScreen() {
        Toolkit tk = Toolkit.getDefaultToolkit();

        // Настройка окна
        setSize(tk.getScreenSize().width / 2, tk.getScreenSize().height / 2);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Установка иконки
        ImageIcon icon = new ImageIcon(iconURL);
        setIconImage(icon.getImage());

        // Создание и добавление панели вкладок
        JTabbedPane tabbedPane = getTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Метод для получения панели вкладок
     *
     * @return панель вкладок основного окна приложения
     */
    private static JTabbedPane getTabbedPane() {
        // Создание панели обработки одного изображения
        ProcessSinglePanel processSinglePanel = ProcessSinglePanel.getInstance();

        // Создание панели обработки множества изображений
        ProcessManyPanel processManyPanel = ProcessManyPanel.getInstance();

        // Создание и заполнение панели вкладок
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.addTab("Обработка одного изображения", processSinglePanel);
        tabbedPane.addTab("Обработка множества изображений", processManyPanel);

        return tabbedPane;
    }

    /**
     * Метод для отображения основного окна приложения
     */
    public void showMainScreen() {
        setVisible(true);
    }
}
