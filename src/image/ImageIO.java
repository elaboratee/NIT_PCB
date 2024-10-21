package image;

import exception.ImageReadException;
import exception.ImageWriteException;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageIO {

    public static Mat loadImage(String path) throws ImageReadException {
        Mat image = Imgcodecs.imread(path);
        if (image.empty()) {
            throw new ImageReadException("Загружено пустое изображение!");
        }
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2BGR);
        return image;
    }

    public static void saveImage(String path, Mat img) throws ImageWriteException {
        boolean saved = Imgcodecs.imwrite(path, img);
        if (!saved) {
            throw new ImageWriteException("Не удалось сохранить изображение!");
        }
    }
}
