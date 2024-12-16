package gui;

import exception.ImageReadException;
import exception.ImageWriteException;
import image.ImageIO;
import org.opencv.core.Mat;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
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
        panel = new JPanel(new GridLayout(1, 2, 10, 10));
        panel.setBackground(Color.GRAY);

        // Панели для шаблонного и целевого изображения
        templatePanel = createImagePanel();
        targetPanel = createImagePanel();

        panel.add(templatePanel, BorderLayout.WEST);
        panel.add(targetPanel, BorderLayout.EAST);

        return panel;
    }

    // Метод для создания панели изображения
    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        imagePanel.add(buttonPanel, BorderLayout.SOUTH);

        if (templateLabel == null) {
            templateLabel = imageLabel;
        } else {
            targetLabel = imageLabel;
        }

        return imagePanel;
    }

    // Метод для создания панели кнопок
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.setOpaque(false);

        if (templatePanel == null) {
            loadTemplateButton = createButton("Загрузить шаблон", e -> loadTemplateImage());
            buttonPanel.add(loadTemplateButton);
        } else {
            loadTargetButton = createButton("Загрузить изображение", e -> loadTargetImage());
            processTargetButton = createButton("Обработать", e -> processTargetImage());
            saveTargetButton = createButton("Сохранить", e -> saveTargetImage());

            buttonPanel.add(loadTargetButton);
            buttonPanel.add(processTargetButton);
            buttonPanel.add(saveTargetButton);

            loadTargetButton.setEnabled(false);
            processTargetButton.setEnabled(false);
            saveTargetButton.setEnabled(false);
        }

        return buttonPanel;
    }

    // Метод для создания кнопки с заданными текстом и действием
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    // Загрузка шаблонного изображения
    private void loadTemplateImage() {
        loadTargetButton.setEnabled(false);
        processTargetButton.setEnabled(false);
        saveTargetButton.setEnabled(false);

        JFileChooser fileChooser = createImageFileChooser();
        if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                templateImage = ImageIO.loadImage(imagePath);
                displayImage(templateImage, templateLabel);
                loadTargetButton.setEnabled(true);
            } catch (ImageReadException ire) {
                showErrorDialog("Ошибка при загрузке шаблонного изображения: " + ire.getMessage());
            }
        }
    }

    // Загрузка целевого изображения
    private void loadTargetImage() {
        JFileChooser fileChooser = createImageFileChooser();
        if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
            String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                targetImage = ImageIO.loadImage(imagePath);
                displayImage(targetImage, targetLabel);
                processTargetButton.setEnabled(true);
            } catch (ImageReadException ire) {
                showErrorDialog("Ошибка при загрузке целевого изображения: " + ire.getMessage());
            }
        }
    }

    // Обработка целевого изображения
    private void processTargetImage() {
        // TODO: Логика обработки целевого изображения
        saveTargetButton.setEnabled(true);
    }

    // Сохранение целевого изображения
    private void saveTargetImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохраните изображение");
        if (fileChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
            String savePath = fileChooser.getSelectedFile().getAbsolutePath() + ".png";
            try {
                ImageIO.saveImage(savePath, targetImage);
            } catch (ImageWriteException iwe) {
                showErrorDialog("Ошибка при сохранении изображения: " + iwe.getMessage());
            }
        }
    }

    // Метод для создания настроенного JFileChooser
    private JFileChooser createImageFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите изображение");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Изображения (JPG, PNG, BMP, GIF)", "jpg", "jpeg", "png", "bmp", "gif")
        );
        return fileChooser;
    }

    // Метод для отображения сообщения об ошибке
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(panel, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    // Метод для отображения изображения на фрейм
    private void displayImage(Mat image, JLabel label) {
        ImageIcon imageIcon = new ImageIcon(matToBufferedImage(image).getScaledInstance(
                tk.getScreenSize().width / 4,
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
