package image;

import dataset.DatasetProcessing;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Processing {

    private static final int KERNEL_SIZE = 5;

    public static Mat visualizeAnnotations(Mat img,
                                           List<List<Map<String, String>>> allAnnotations,
                                           String imageName) {
        // Фильтруем аннотации для текущего изображения
        List<Map<String, String>> filteredAnnotations =
                DatasetProcessing.filterAnnotationsByFilename(allAnnotations, imageName);

        // Визуализируем аннотации
        for (Map<String, String> annotation : filteredAnnotations) {
            String classLabel = annotation.get("name");
            int xmin = Integer.parseInt(annotation.get("xmin"));
            int ymin = Integer.parseInt(annotation.get("ymin"));
            int xmax = Integer.parseInt(annotation.get("xmax"));
            int ymax = Integer.parseInt(annotation.get("ymax"));

            // Рисуем рамку, выделяющую дефект
            Imgproc.rectangle(
                    img,
                    new Point(xmin, ymin),
                    new Point(xmax, ymax),
                    new Scalar(255, 255, 255),
                    3
            );

            // Получаем размер подписи
            int[] baseLine = {0};
            Size textSize = Imgproc.getTextSize(
                    classLabel,
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.5,
                    2,
                    baseLine
            );

            // Добавляем background для подписи
            Imgproc.rectangle(
                    img,
                    new Point(xmin, ymin - textSize.height - 5),
                    new Point(xmin + textSize.width, ymin - 1),
                    new Scalar(255, 255, 255),
                    -1
            );

            // Добавляем подпись к рамке дефекта
            Imgproc.putText(
                    img,
                    classLabel,
                    new Point(xmin, ymin - 5),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.5,
                    new Scalar(0, 0, 0),
                    2
            );
        }

        return img;
    }

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
        for (int y = 0; y <= target.rows() - KERNEL_SIZE; y += step) {
            for (int x = 0; x <= target.cols() - KERNEL_SIZE; x += step) {
                // Изменяем координаты ROI
                roi.x = x;
                roi.y = y;

                // Определяем области шаблонов
                targetRegion = target.submat(roi);
                templateRegion = template.submat(roi);

                // Сравниваем шаблоны
                Imgproc.matchTemplate(targetRegion, templateRegion, matchResult, Imgproc.TM_SQDIFF_NORMED);

                // Получаем значения сравнения и записываем результат
                matchValue = matchResult.get(0, 0);
                result.put(y, x, matchValue[0]);
            }
        }

        // Нормализуем полученную матрицу для визуализации изображения
        Core.normalize(result, result, 0, 255, Core.NORM_MINMAX, -1);
        result.convertTo(result, CvType.CV_8UC1);

        return result;
    }

    public static List<MatOfPoint> findContours(Mat img) {
        Mat imgClone = img.clone();

        Mat thresholdImg = new Mat();
        Imgproc.threshold(imgClone, thresholdImg, 127, 1, Imgproc.THRESH_BINARY);

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
            Rect rect = Imgproc.boundingRect(contour);

            // Корректировка координат
            rect.width += KERNEL_SIZE;
            rect.height += KERNEL_SIZE;

            rects.add(rect);
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
