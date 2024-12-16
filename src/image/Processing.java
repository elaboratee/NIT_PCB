package image;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Processing {

    private static final int KERNEL_SIZE = 5;

    public static Mat matchTemplateOptimized(Mat target,
                                             Mat template) {
        // Результирующая матрица
        Mat result = new Mat(target.size(), CvType.CV_32F);

        // Структуры для хранения шаблонов и промежуточных результатов
        Rect roi = new Rect(0, 0, KERNEL_SIZE, KERNEL_SIZE);
        Mat targetRegion, templateRegion;
        Mat matchResult = new Mat(1, 1, CvType.CV_32F);
        double[] matchValue;

        // Сравнение по парам ROI
        int step = KERNEL_SIZE / 2;
        for (int y = 0; y < target.rows() - KERNEL_SIZE + 1; y += step) {
            for (int x = 0; x < target.cols() - KERNEL_SIZE + 1; x += step) {
                // Изменяем координаты ROI
                roi.x = x;
                roi.y = y;

                // Определяем области шаблонов
                targetRegion = target.submat(roi);
                templateRegion = template.submat(roi);

                // Сравниваем шаблоны
                Imgproc.matchTemplate(targetRegion, templateRegion, matchResult, Imgproc.TM_SQDIFF_NORMED);

                // Получаем значения сравнения и записываем результат,
                // корректируем координаты результата для соответствия центру ядра
                matchValue = matchResult.get(0, 0);
                result.put(y + step, x + step, matchValue[0]);
            }
        }

        // Нормализуем полученную матрицу для визуализации изображения
        Core.normalize(result, result, 0, 255, Core.NORM_MINMAX, -1);
        result.convertTo(result, CvType.CV_8UC1);

        return result;
    }

    public static List<MatOfPoint> findContours(Mat img) {
        // Пороговая обработка для получения бинарного изображения
        Mat thresholdImg = new Mat();
        Imgproc.threshold(img, thresholdImg, 127, 1, Imgproc.THRESH_BINARY);

        // Поиск контуров
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
                thresholdImg,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

        return contours;
    }

    public static List<Rect> getBoundingRects(List<MatOfPoint> contours) {
        List<Rect> rects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            rects.add(Imgproc.boundingRect(contour));
        }
        return rects;
    }

    public static Mat dilateImage(Mat src) {
        // Создание примитива
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(KERNEL_SIZE, KERNEL_SIZE));

        // Дилатацию изображения
        Mat result = new Mat();
        Imgproc.dilate(src, result, kernel);

        return result;
    }

    public static Mat highlightBoundaries(Mat img) {
        Mat result = new Mat();
        Imgproc.Canny(
                img,
                result,
                80,
                200,
                3,
                true
        );
        return result;
    }
}
