package gui;

import exception.ImageReadException;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import util.DataConversions;
import util.Filters;
import util.ImageIO;
import util.Processing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Класс, определяющий панель для обработки множества изображений
 */
public class ProcessManyPanel extends JPanel {

    private static ProcessManyPanel instance;

    private JButton selectImageDirButton, selectTemplateDirButton, selectLogsButton, processImagesButton;
    private JTextField imagePathField, templatePathField, logsPathField;
    private final JLabel imageLabel;
    private final JProgressBar progressBar;
    private final JFileChooser fileChooser;
    private final Toolkit tk = Toolkit.getDefaultToolkit();

    private ProcessManyPanel() {
        // Создание панели параметров
        JPanel paramPanel = createParamPanel();

        // Создание лейбла изображения
        imageLabel = createImageLabel();

        // Создание панели изображения
        JPanel imagePanel = createImagePanel(imageLabel);

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

    /**
     * Метод для получения экземпляра панели-синглтона
     *
     * @return экземпляр панели обработки множества изображений
     */
    public static ProcessManyPanel getInstance() {
        if (instance == null) {
            instance = new ProcessManyPanel();
        }
        return instance;
    }

    /**
     * Метод для создания панели параметров
     *
     * @return панель параметров с кнопками и текстовыми полями
     */
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

    /**
     * Метод для создания лейбла изображения
     *
     * @return универсальный лейбл изображения
     */
    private JLabel createImageLabel() {
        JLabel label = new JLabel();
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * Метод для создания панели изображения
     *
     * @param label лейбл изображения, который необходимо привязать к панели
     * @return панель изображения с привязанным лейблом {@code label}
     */
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

    /**
     * Метод для создания кнопки с заданными текстом и действием
     *
     * @param text   текст кнопки
     * @param action обработчик события кнопки (по сути выполняемое при нажатии действие)
     * @return кнопка с заданными текстом и действием
     */
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    /**
     * Обработчик событий кнопки выбора директории с изображениями.
     * При вызове выполняется выбор директории и отображение ее пути в текстовом поле
     */
    private void selectImageDirectory() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            imagePathField.setText(selectedDirectory.getAbsolutePath());
            selectTemplateDirButton.setEnabled(true);
        }
    }

    /**
     * Обработчик событий кнопки выбора директории с шаблонами.
     * При вызове выполняется выбор директории и отображение ее пути в текстовом поле
     */
    private void selectTemplateDirectory() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            templatePathField.setText(selectedDirectory.getAbsolutePath());
            selectLogsButton.setEnabled(true);
        }
    }

    /**
     * Обработчик событий кнопки выбора директории для сохранения файла логов.
     * При вызове выполняется выбор директории и отображение ее пути в текстовом поле
     */
    private void selectLogsDirectory() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            logsPathField.setText(selectedDirectory.getAbsolutePath());
            processImagesButton.setEnabled(true);
        }
    }

    /**
     * Обработчик событий кнопки обработки директории изображений.
     * При вызове выполняется парсинг изображений и шаблонов, а также создание
     * отдельного потока обработки изображений, в течение которой обработанные
     * изображения отображаются в интерфейсе. Также найденные дефекты и их координаты
     * логируются в файле, создаваемому по указанному пути
     */
    private void processImages() {
        // Получение изображений для обработки
        File imageDirectory = new File(imagePathField.getText());
        File[] imageFiles = imageDirectory
                .listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png"));
        if (imageFiles == null || imageFiles.length == 0) {
            showErrorDialog("Директория с изображениями пуста!");
            return;
        }

        // Получение шаблонных изображений
        File templateDirectory = new File(templatePathField.getText());
        File[] templateFiles = templateDirectory
                .listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                        name.toLowerCase().endsWith(".png"));
        if (templateFiles == null || templateFiles.length == 0) {
            showErrorDialog("Директория с шаблонами пуста!");
            return;
        }

        // Выключение кнопок
        selectImageDirButton.setEnabled(false);
        selectTemplateDirButton.setEnabled(false);
        selectLogsButton.setEnabled(false);
        processImagesButton.setEnabled(false);

        // Установка максимального и начального значений прогресс-бара
        progressBar.setMaximum(imageFiles.length);
        progressBar.setValue(0);

        new Thread(() -> {
            int time = Math.abs(Long.hashCode(System.currentTimeMillis()));
            try (PrintWriter writer = new PrintWriter(
                    new FileWriter(logsPathField.getText() + "\\logs_" + time + ".log"))) {
                // Объявление матриц, связанных с шаблоном для экономии памяти
                Mat templateSrc, templateGray = null;

                // Обработка изображений
                String lastLoadedTemplateCode = "";
                int counter = 0;
                for (File imageFile : imageFiles) {
                    // Загрузка шаблонного изображения
                    String templateCode = imageFile.getName().substring(0, 2);
                    if (!templateCode.equals(lastLoadedTemplateCode)) {
                        try {
                            templateSrc = ImageIO.loadImage(
                                    Arrays
                                            .stream(templateFiles)
                                            .filter(file -> file.getName().startsWith(templateCode))
                                            .findFirst()
                                            .orElseThrow(() ->
                                                    new ImageReadException("Шаблон не найден для кода: " +
                                                            templateCode))
                                            .toString()
                            );
                        } catch (ImageReadException ire) {
                            showErrorDialog("Ошибка при загрузке шаблонного изображения: " + ire.getMessage());
                            selectImageDirButton.setEnabled(true);
                            selectTemplateDirButton.setEnabled(true);
                            selectLogsButton.setEnabled(true);
                            processImagesButton.setEnabled(false);
                            return;
                        }
                        lastLoadedTemplateCode = templateCode;

                        // Уменьшение размеров нового шаблона
                        Imgproc.resize(templateSrc, templateSrc,
                                new Size((double) templateSrc.cols() / 2, (double) templateSrc.rows() / 2));

                        // Преобразование нового шаблона к оттенкам серого
                        templateGray = new Mat();
                        Imgproc.cvtColor(templateSrc, templateGray, Imgproc.COLOR_BGR2GRAY);
                    }

                    // Загрузка исходного изображения
                    Mat targetSrc;
                    try {
                        targetSrc = ImageIO.loadImage(imageFile.getAbsolutePath());
                    } catch (ImageReadException ire) {
                        showErrorDialog("Ошибка при загрузке исходного изображения: " + ire.getMessage());
                        selectImageDirButton.setEnabled(true);
                        selectTemplateDirButton.setEnabled(true);
                        selectLogsButton.setEnabled(true);
                        processImagesButton.setEnabled(false);
                        return;
                    }

                    // Уменьшение размеров изображений
                    Imgproc.resize(targetSrc, targetSrc,
                            new Size((double) targetSrc.cols() / 2, (double) targetSrc.rows() / 2));

                    // Преобразование исходного изображения к оттенкам серого
                    Mat targetGray = new Mat();
                    Imgproc.cvtColor(targetSrc, targetGray, Imgproc.COLOR_BGR2GRAY);

                    // Применение размытия по Гауссу
                    Mat templateBlur = Filters.applyGaussianBlur(templateGray);
                    Mat targetBlur = Filters.applyGaussianBlur(targetGray);

                    // Выравнивание гистограмм
                    Mat templateCLAHE = Filters.applyCLAHE(templateBlur);
                    Mat targetCLAHE = Filters.applyCLAHE(targetBlur);

                    // Поиск дефектов методом сравнения с шаблоном
                    Mat matchedImg = Processing.matchTemplate(templateCLAHE, targetCLAHE);

                    // Постобработка изображения
                    Mat dilatedImg = Processing.dilateImage(matchedImg);

                    // Поиск контуров
                    List<MatOfPoint> contours = Processing.findContours(dilatedImg);

                    // Создание изображения с выделенными дефектами
                    Mat boundedImg = new Mat();
                    targetSrc.copyTo(boundedImg);

                    // Отрисовка выделений дефектов
                    List<Rect> boundingRects = Processing.getBoundingRects(contours);
                    for (Rect rect : boundingRects) {
                        if (rect.size().width >= 2 || rect.size().height >= 2) {
                            Imgproc.rectangle(boundedImg, rect, new Scalar(255, 0, 255), 2);

                            String logMsg = String.format(
                                    "На плате %s обнаружен дефект. Координаты дефекта: (%d, %d; %d, %d)",
                                    imageFile.getName(),
                                    rect.x, rect.y,
                                    rect.x + rect.width, rect.y + rect.height
                            );
                            writer.println(logMsg);
                        }
                    }
                    writer.flush();

                    // Вывод изображения на панель
                    displayImage(boundedImg, imageLabel);

                    // Увеличение счетчика прогресс-бара
                    progressBar.setValue(++counter);
                }
            } catch (IOException e) {
                showErrorDialog("Ошибка создания файла логов: " + e.getMessage());
            }

            // Включение кнопок
            selectImageDirButton.setEnabled(true);
            selectTemplateDirButton.setEnabled(true);
            selectLogsButton.setEnabled(true);
            processImagesButton.setEnabled(true);
        }).start();
    }

    /**
     * Метод для создания текстового поля
     *
     * @return универсальное нередактируемое текстовое поле
     */
    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        return textField;
    }

    /**
     * Метод для создания прогресс-бара
     *
     * @return экземпляр прогресс-бара
     */
    private JProgressBar createProgressBar() {
        return new JProgressBar();
    }

    /**
     * Метод для создания настроенного JFileChooser
     *
     * @return сконфигурированный экземпляр JFileChooser для выбора директорий
     */
    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите директорию");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return fileChooser;
    }

    /**
     * Метод для отображения сообщения об ошибке в диалоговом окне
     *
     * @param message сообщение, которое необходимо отобразить в диалоговом окне
     */
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Метод для отображения изображения в интерфейсе
     *
     * @param image изображение, которое необходимо отобразить в {@code label}
     * @param label лейбл, в котором должно отобразиться изображение {@code image}
     */
    private void displayImage(Mat image, JLabel label) {
        // Получение оригинальных размеров изображения
        BufferedImage bufferedImage = DataConversions.matToBufferedImage(image);
        int originalWidth = bufferedImage.getWidth();
        int originalHeight = bufferedImage.getHeight();

        // Получение доступного размера панели
        int maxWidth = (int) (tk.getScreenSize().width / 1.5);
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
    }
}