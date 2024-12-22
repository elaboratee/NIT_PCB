package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class ProcessManyPanel extends JPanel {

    private static ProcessManyPanel instance;

    private final JPanel paramPanel, imagePanel;
    private JButton selectImageDirButton, selectTemplateDirButton, selectLogsButton, processImagesButton;
    private JTextField imagePathField, templatePathField, logsPathField;
    private JLabel imageLabel;
    private final JProgressBar progressBar;
    private final JFileChooser fileChooser;

    private ProcessManyPanel() {
        // Создание верхней панели
        paramPanel = createParamPanel();

        // Создание лейбла изображения
        imageLabel = createImageLabel();

        // Создание панели изображения
        imagePanel = createImagePanel(imageLabel);

        // Создание панели прогресс-бара
        progressBar = createProgressBar();

        // Создание JFileChooser
        fileChooser = createFileChooser();

        // Заполнение родительской панели
        setLayout(new BorderLayout());
        add(paramPanel, BorderLayout.NORTH);
        add(imagePanel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }

    public static ProcessManyPanel getInstance() {
        if (instance == null) {
            instance = new ProcessManyPanel();
        }
        return instance;
    }

    // Метод для создания панели параметров
    private JPanel createParamPanel() {
        // Создание и настройка верхней панели (с кнопками)
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.setBackground(new Color(0x181818));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // Создание кнопок
        selectImageDirButton = createButton(
                "Выбрать директорию с изображениями",
                e -> selectImageDirectory()
        );

        selectTemplateDirButton = createButton(
                "Выбрать директорию с шаблонами",
                e -> selectTemplateDirectory()
        );

        selectLogsButton = createButton(
                "Выбрать директорию для сохранения логов",
                e -> selectLogsDirectory()
        );

        processImagesButton = createButton(
                "Обработать изображения",
                e -> processImages()
        );

        // Создание текстовых полей
        imagePathField = createTextField();
        templatePathField = createTextField();
        logsPathField = createTextField();

        // Добавление кнопок и текстовых полей
        panel.add(selectImageDirButton);
        panel.add(imagePathField);

        panel.add(selectTemplateDirButton);
        panel.add(templatePathField);

        panel.add(selectLogsButton);
        panel.add(logsPathField);

        panel.add(processImagesButton);

        // Отключение кнопок
        selectTemplateDirButton.setEnabled(false);
        selectLogsButton.setEnabled(false);
        processImagesButton.setEnabled(false);

        return panel;
    }

    // Метод для создания лейбла изображения
    private JLabel createImageLabel() {
        JLabel label = new JLabel();
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    // Метод для создания панели изображения
    private JPanel createImagePanel(JLabel label) {
        // Создание и настройка панели изображения
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0x181818));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        // Создание скролл-панели
        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(0x181818));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Метод для создания кнопки с заданными текстом и действием
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    // Метод для создания текстового поля
    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        return textField;
    }

    // Метод для создания прогресс-бара
    private JProgressBar createProgressBar() {
        return new JProgressBar();
    }

    private void selectImageDirectory() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            imagePathField.setText(selectedDirectory.getAbsolutePath());
            selectTemplateDirButton.setEnabled(true);
        }
    }

    private void selectTemplateDirectory() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            templatePathField.setText(selectedDirectory.getAbsolutePath());
            selectLogsButton.setEnabled(true);
        }
    }

    private void selectLogsDirectory() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            logsPathField.setText(selectedDirectory.getAbsolutePath());
            processImagesButton.setEnabled(true);
        }
    }

    private void processImages() {
        // Получение изображений для обработки
        File imageDirectory = new File(imagePathField.getText());
        File[] imageFiles = imageDirectory
                .listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            showErrorDialog("Директория с изображениями пуста!");
            return;
        }

        // Получение шаблонных изображений
        File templateDirectory = new File(templatePathField.getText());
        File[] templateFiles = templateDirectory
                .listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        if (templateFiles == null || templateFiles.length == 0) {
            showErrorDialog("Директория с шаблонами пуста!");
            return;
        }

        // Установка максимального и начального значений прогресс-бара
        progressBar.setMaximum(imageFiles.length);
        progressBar.setValue(0);
    }

    // Метод для создания настроенного JFileChooser
    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите директорию");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return fileChooser;
    }

    // Метод для отображения сообщения об ошибке
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}
