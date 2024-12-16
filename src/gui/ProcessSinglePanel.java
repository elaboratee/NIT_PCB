package gui;

import exception.ImageReadException;
import exception.ImageWriteException;
import image.ImageIO;
import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ProcessSinglePanel extends JPanel {

    private JPanel panel, templatePanel, targetPanel;
    private JLabel templateLabel, targetLabel;
    private JButton loadTemplateButton, loadTargetButton, processTargetButton, saveTargetButton;
    private Mat templateImage, targetImage;
    private final Toolkit tk = Toolkit.getDefaultToolkit();

    private ProcessSinglePanel() {
    }

    public static ProcessSinglePanel createInstance() {
        return new ProcessSinglePanel();
    }

    public JPanel getProcessSinglePanel() {
        // Основная панель
        panel = new JPanel(new BorderLayout());

        // Панели для шаблонного и целевого изображения
        templatePanel = new JPanel(new BorderLayout());
        targetPanel = new JPanel(new BorderLayout());

        // Создание элементов панели для отображения изображений
        templateLabel = new JLabel();
        templatePanel.add(templateLabel, BorderLayout.SOUTH);

        targetLabel = new JLabel();
        targetPanel.add(targetLabel, BorderLayout.SOUTH);

        panel.add(templatePanel, BorderLayout.WEST);
        panel.add(targetPanel, BorderLayout.EAST);

        // Создание панели кнопок целевого изображения
        JPanel targetButtonPanel = getTargetButtonPanel();
        targetPanel.add(targetButtonPanel, BorderLayout.NORTH);

        // Кнопки для панели шаблонного изображения
        JPanel templateButtonPanel = getTemplateButtonPanel();
        templatePanel.add(templateButtonPanel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel getTargetButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

        loadTargetButton = getLoadTargetButton();
        processTargetButton = getProcessTargetButton();
        saveTargetButton = getSaveTargetButton();

        buttonPanel.add(loadTargetButton);
        buttonPanel.add(processTargetButton);
        buttonPanel.add(saveTargetButton);

        return buttonPanel;
    }

    private JPanel getTemplateButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));

        loadTemplateButton = getLoadTemplateButton();
        buttonPanel.add(loadTemplateButton);

        return buttonPanel;
    }

    // Метод для получения кнопки загрузки шаблона
    private JButton getLoadTemplateButton() {
        JButton button = new JButton("Загрузить шаблон");

        button.addActionListener(e -> {
            // Отключение кнопок
            loadTargetButton.setEnabled(false);
            processTargetButton.setEnabled(false);
            saveTargetButton.setEnabled(false);

            // Создание и настройка JFileChooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Выберите изображение");

            // Установка фильтра для выбора только изображений
            FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                    "Изображения (JPG, PNG, BMP, GIF)", "jpg", "jpeg", "png", "bmp", "gif"
            );
            fileChooser.setFileFilter(imageFilter);

            // Показ диалога выбора файла
            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    templateImage = ImageIO.loadImage(imagePath);
                } catch (ImageReadException ire) {
                    throw new RuntimeException("Ошибка при загрузке изображения: " + ire.getMessage(), ire);
                }

                // Отображение загруженного изображения
                displayImage(templateImage, templateLabel);
                loadTargetButton.setEnabled(true);
            }
        });

        return button;
    }

    // Метод для получения кнопки загрузки целевого изображения
    private JButton getLoadTargetButton() {
        JButton button = new JButton("Загрузить изображение");

        button.addActionListener(e -> {
            // Создание и настройка JFileChooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Выберите изображение");

            // Установка фильтра для выбора только изображений
            FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                    "Изображения (JPG, PNG, BMP, GIF)", "jpg", "jpeg", "png", "bmp", "gif"
            );
            fileChooser.setFileFilter(imageFilter);

                    // Показ диалога выбора файла
                    int result = fileChooser.showOpenDialog(panel);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
                        try {
                            targetImage = ImageIO.loadImage(imagePath);
                        } catch (ImageReadException ire) {
                            throw new RuntimeException("Ошибка при загрузке изображения: " + ire.getMessage(), ire);
                        }

                        // Отображение загруженного изображения
                        displayImage(targetImage, targetLabel);
                        processTargetButton.setEnabled(true);
                    }
        });
        button.setEnabled(false);

        return button;
    }

    // Метод для получения кнопки обработки целевого изображения
    private JButton getProcessTargetButton() {
        JButton button = new JButton("Обработать");

        button.addActionListener(e -> {
            saveTargetButton.setEnabled(true);
        });
        button.setEnabled(false);

        return button;
    }

    // Метод для получения кнопки сохранения целевого изображения
    private JButton getSaveTargetButton() {
        JButton button = new JButton("Сохранить");

        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохраните изображение");

            int result = fileChooser.showSaveDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                String savePath = fileChooser.getSelectedFile().getAbsolutePath();

                try {
                    ImageIO.saveImage(savePath + ".png", targetImage);
                } catch (ImageWriteException iwe) {
                    throw new RuntimeException("Ошибка при сохранении изображения: " + iwe.getMessage(), iwe);
                }
            }
        });
        button.setEnabled(false);

        return button;
    }

    // Метод для отображения изображения на фрейм
    private void displayImage(Mat image, JLabel label) {
        ImageIcon imageIcon = new ImageIcon(matToBufferedImage(image).getScaledInstance(
                tk.getScreenSize().width / 2,
                tk.getScreenSize().height / 2,
                Image.SCALE_SMOOTH)
        );
        label.setIcon(imageIcon);
        panel.repaint();
    }

    // Метод для преобразования Mat из OpenCV в BufferedImage из AWT
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];

        mat.get(0, 0, data);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
        return bufferedImage;
    }
}
