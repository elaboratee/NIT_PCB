import com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme;
import exception.ImageReadException;
import exception.ImageWriteException;
import image.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

public class DefectDetectionApp {

    private JFrame frame;
    private JLabel patternLabel, targetLabel;
    private JButton loadButton, processButton, saveButton;
    private Mat loadedImage;
    private final Toolkit tk = Toolkit.getDefaultToolkit();

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FlatGradiantoNatureGreenIJTheme.setup();
    }

    public static void main(String[] args) {
        display();
    }

    private static void display() {
        SplashScreen.showSplashScreen();
        Random rand = new Random(System.currentTimeMillis());
        Timer timer = new Timer(rand.nextInt(3000, 7000), e -> {
            SplashScreen.closeSplashScreen();
            SwingUtilities.invokeLater(() -> {
                new DefectDetectionApp().initialize();
            });
        });
        timer.setRepeats(false);
        timer.start();
    }

    private int getScreenWidth() {
        return tk.getScreenSize().width;
    }

    private int getScreenHeight() {
        return tk.getScreenSize().height;
    }

    private Point getScreenCenter() {
        return new Point(
                getScreenWidth() / 2 - getScreenWidth() / 4,
                getScreenHeight() / 2 - getScreenHeight() / 4
        );
    }

    private void initialize() {
        // Настройка главного окна
        frame = new JFrame("SurfaceScout");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(getScreenCenter());
        frame.setSize(getScreenWidth() / 2, getScreenHeight() / 2);

        // Установка иконки
        ImageIcon icon = new ImageIcon("img" + File.separator + "icon.png");
        frame.setIconImage(icon.getImage());

        // Панель для отображения изображений
        patternLabel = new JLabel();
        targetLabel = new JLabel();

        JPanel imagePanel = new JPanel(new GridLayout(1, 2));
        imagePanel.add(patternLabel, targetLabel);

        frame.add(imagePanel, BorderLayout.CENTER);

        // Панель с кнопками и текстовой областью
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 3));

        loadButton = new JButton("Загрузить");
        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Выберите изображение");
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                String imagePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    loadedImage = ImageIO.loadImage(imagePath);
                } catch (ImageReadException ex) {
                    throw new RuntimeException(ex);
                }
                displayImage(loadedImage);
                processButton.setEnabled(true);
            }
        });

        processButton = new JButton("Обработать");
        processButton.addActionListener(e -> {
//            Mat processedImage = processImage(loadedImage);
//            displayImage(processedImage);
            saveButton.setEnabled(true);
        });
        processButton.setEnabled(false); // Пока изображение не загружено, кнопка отключена

        saveButton = new JButton("Сохранить");
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохраните изображение");
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                String savePath = fileChooser.getSelectedFile().getAbsolutePath();
                try {
                    ImageIO.saveImage(savePath + ".png", loadedImage);
                } catch (ImageWriteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        saveButton.setEnabled(false); // Пока обработка не выполнена, кнопка отключена

        // Добавление кнопок
        panel.add(loadButton);
        panel.add(processButton);
        panel.add(saveButton);
        frame.add(panel, BorderLayout.SOUTH);

        // Отображение окна
        frame.setVisible(true);
    }

    // Отображает изображение
    private void displayImage(Mat image) {
        ImageIcon imageIcon = new ImageIcon(matToBufferedImage(image).getScaledInstance(
                tk.getScreenSize().width / 2,
                tk.getScreenSize().height / 2,
                Image.SCALE_SMOOTH)
        );
        patternLabel.setIcon(imageIcon);
        frame.repaint();
    }

    // Преобразует Mat из OpenCV в BufferedImage из AWT
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
