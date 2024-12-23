package util;

import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

/**
 * Утилитный класс, содержащий статические методы для преобразования сложных типов данных
 */
public class DataConversions {

    /**
     * Метод для преобразования Mat из OpenCV в BufferedImage из AWT
     *
     * @param mat изображение в формате Mat, которое необходимо преобразовать в BufferedImage
     * @return изображение в формате BufferedImage
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
        // Получение типа выходного изображения в зависимости от количества каналов входного
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else {
            throw new IllegalArgumentException("Не поддерживаемое количество каналов матрицы: " + mat.channels());
        }

        // Получение параметров выходного изображения
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        byte[] data = new byte[width * height * channels];

        // Заполнение выходного изображения
        mat.get(0, 0, data);
        BufferedImage bufferedImage = new BufferedImage(width, height, type);
        bufferedImage.getRaster().setDataElements(0, 0, width, height, data);

        return bufferedImage;
    }
}
