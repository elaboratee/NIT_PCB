package gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainScreen {

    private static final Toolkit tk = Toolkit.getDefaultToolkit();

    public static void showMainScreen() {
        // Настройка фрейма
        JFrame frame = new JFrame("SurfaceScout");
        frame.setSize(
                tk.getScreenSize().width / 2,
                tk.getScreenSize().height / 2
        );
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Установка иконки
        ImageIcon icon = new ImageIcon("img" + File.separator + "icon.png");
        frame.setIconImage(icon.getImage());

        // Создание и заполнение панели вкладок
        JTabbedPane tabbedPane = getTabbedPane();

        // Добавление панели вкладок на фрейм
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Отображение фрейма
        frame.setVisible(true);
    }

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
}
