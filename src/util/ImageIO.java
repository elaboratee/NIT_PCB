package util;

import exception.ImageReadException;
import exception.ImageWriteException;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Утилитный класс, предоставляющий статические методы
 * для загрузки и сохранения изображений
 */
public class ImageIO {

    /**
     * Метод для загрузки изображения по указанному пути
     *
     * @param path путь к изображению
     * @return загруженное изображение
     * @throws ImageReadException если возникла ошибка при загрузке изображения
     */
    public static Mat loadImage(String path) throws ImageReadException {
        Mat image = Imgcodecs.imread(path);
        if (image.empty()) {
            throw new ImageReadException("Загружено пустое изображение!");
        }
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);
        return image;
    }

    /**
     * Метод для сохранения изображения по указанному пути
     *
     * @param path путь для сохранения изображения
     * @param img  изображение, которое необходимо сохранить
     * @throws ImageWriteException если возникла ошибка при сохранении изображения
     */
    public static void saveImage(String path, Mat img) throws ImageWriteException {
        boolean saved = Imgcodecs.imwrite(path, img);
        if (!saved) {
            throw new ImageWriteException("Не удалось сохранить изображение!");
        }
    }
}
