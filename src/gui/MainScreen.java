package gui;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainScreen {

    private static JFrame frame;
    private static JLabel patternLabel, targetLabel;
    private static JButton loadButton, processButton, saveButton;
    private static Mat loadedImage;
    private static final Toolkit tk = Toolkit.getDefaultToolkit();

    public static void showMainScreen() {
        // Настройка фрейма
        frame = new JFrame("SurfaceScout");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(getScreenCenter());
        frame.setSize(getScreenWidth() / 2, getScreenHeight() / 2);

        // Установка иконки
        ImageIcon icon = new ImageIcon("img" + File.separator + "icon.png");
        frame.setIconImage(icon.getImage());

        // Создание панели обработки одного изображения
        ProcessSinglePanel processSinglePanel = ProcessSinglePanel.createInstance();

        // Создание и заполнение панели вкладок
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.addTab("Обработка одного изображения", processSinglePanel.getProcessSinglePanel());

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Отображение фрейма
        frame.setVisible(true);
    }

    // Метод для получения центра экрана
    private static Point getScreenCenter() {
        return new Point(
                getScreenWidth() / 2 - getScreenWidth() / 4,
                getScreenHeight() / 2 - getScreenHeight() / 4
        );
    }

    // Метод для получения ширины экрана
    private static int getScreenWidth() {
        return tk.getScreenSize().width;
    }

    // Метод для получения высоты экрана
    private static int getScreenHeight() {
        return tk.getScreenSize().height;
    }
}
