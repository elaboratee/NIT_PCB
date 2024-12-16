package gui;

import exception.ImageReadException;
import exception.ImageWriteException;
import image.Filters;
import image.ImageIO;
import image.Processing;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.util.List;

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
        imagePanel.setBackground(new Color(0x181818));
        imagePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(0x181818));
        imagePanel.add(scrollPane, BorderLayout.CENTER);

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
        // Клонирование исходных изображений
        Mat templateCopy = templateImage.clone();
        Mat targetCopy = targetImage.clone();

        // Изменение размеров изображений (до 25%)
        Imgproc.resize(templateCopy, templateCopy,
                new Size((double) templateCopy.cols() / 2, (double) templateCopy.rows() / 2));
        Imgproc.resize(targetCopy, targetCopy,
                new Size((double) targetCopy.cols() / 2, (double) targetCopy.rows() / 2));

        // Преобразование исходных изображений к оттенкам серого
        Mat templateGray = new Mat(templateCopy.rows(), templateCopy.cols(), CvType.CV_8UC1);
        Mat targetGray = new Mat(targetCopy.rows(), targetCopy.cols(), CvType.CV_8UC1);

        Imgproc.cvtColor(templateCopy, templateGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(targetCopy, targetGray, Imgproc.COLOR_BGR2GRAY);

        // Применение размытия по Гауссу
        Mat templateBlur = Filters.applyGaussianBlur(templateGray);
        Mat targetBlur = Filters.applyGaussianBlur(targetGray);

        // Выравнивание гистограммы
        Mat templateCLAHE = Filters.applyCLAHE(templateBlur);
        Mat targetCLAHE = Filters.applyCLAHE(targetBlur);

        // Поиск дефектов методом сравнения с шаблоном
        Mat matchedImg = Processing.matchTemplateOptimized(templateCLAHE, targetCLAHE);

        // Постобработка изображения
        Mat dilatedImg = Processing.dilateImage(matchedImg);

        // Поиск контуров
        List<MatOfPoint> contours = Processing.findContours(dilatedImg);

        // Создание изображения с выделенными дефектами
        Mat boundedImg = new Mat();
        targetCopy.copyTo(boundedImg);

        // Отрисовка выделений дефектов
        List<Rect> boundingRects = Processing.getBoundingRects(contours);
        for (Rect rect : boundingRects) {
            Imgproc.rectangle(boundedImg, rect, new Scalar(255, 0, 255), 2);
        }

        // Вывод изображения на панель
        displayImage(boundedImg, targetLabel);
        targetImage = boundedImg;
        panel.repaint();

        processTargetButton.setEnabled(false);
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
        // Получение оригинальных размеров изображения
        BufferedImage bufferedImage = matToBufferedImage(image);
        int originalWidth = bufferedImage.getWidth();
        int originalHeight = bufferedImage.getHeight();

        // Получение доступного размера панели
        int maxWidth = (int) (tk.getScreenSize().width / 2.5);
        int maxHeight = (int) (tk.getScreenSize().height / 2.0);

        // Расчет новых размеров с сохранением пропорций
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Масштабирование изображения с сохранением пропорций
        Image scaledImage = bufferedImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // Установка изображения на JLabel
        ImageIcon imageIcon = new ImageIcon(scaledImage);
        label.setIcon(imageIcon);

        panel.repaint();
    }

    // Метод для преобразования Mat из OpenCV в BufferedImage из AWT
    private BufferedImage matToBufferedImage(Mat mat) {
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else {
            throw new IllegalArgumentException("Не поддерживаемое количество каналов матрицы: " + mat.channels());
        }

        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];

        mat.get(0, 0, data);
        BufferedImage bufferedImage = new BufferedImage(width, height, type);
        bufferedImage.getRaster().setDataElements(0, 0, width, height, data);
        return bufferedImage;
    }
}
