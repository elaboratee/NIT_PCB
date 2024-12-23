package util;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

/**
 * Утилитный класс, предоставляющий статические методы для фильтрации изображений
 */
public class Filters {

    /**
     * Метод для выполнения размытия по Гауссу
     *
     * @param src исходное изображение, которое необходимо размыть
     * @return размытое изображение
     */
    public static Mat applyGaussianBlur(Mat src) {
        Mat result = new Mat();
        Imgproc.GaussianBlur(src, result, new Size(3, 3), 1.5);
        return result;
    }

    /**
     * Метод для выравнивания гистограммы с помощью CLAHE
     *
     * @param src исходное изображение, гистограмму которого необходимо выровнять
     * @return изображение с выровненной гистограммой
     */
    public static Mat applyCLAHE(Mat src) {
        Mat result = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(6, new Size(2, 2));
        clahe.apply(src, result);
        return result;
    }
}
