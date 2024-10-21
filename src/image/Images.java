package image;

import dataset.DatasetProcessing;
import org.opencv.core.*;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.Map;

public class Images {

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

    public static Mat matchTemplate(Mat target,
                                    Mat template) {
        // Преобразуем исходные изображения в grayscale
        Mat targetGray = new Mat(target.rows(), target.cols(), CvType.CV_8UC1);
        Mat templateGray = new Mat(template.rows(), template.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(target, targetGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(template, templateGray, Imgproc.COLOR_BGR2GRAY);

        // Изменение размеров изображений (до 25%)
        Imgproc.resize(targetGray, targetGray,
                new Size(targetGray.cols() / 2.0, targetGray.rows() / 2.0));
        Imgproc.resize(templateGray, templateGray,
                new Size(templateGray.cols() / 2.0, templateGray.rows() / 2.0));

        // Размер ядра и результирующая матрица
        int kernelSize = 5;
        Mat result = new Mat(targetGray.size(), CvType.CV_32F);

        // Структуры для хранения шаблонов и промежуточных результатов
        Rect roi = new Rect(0, 0, kernelSize, kernelSize);
        Mat targetRegion, templateRegion;
        Mat matchResult = new Mat(1, 1, CvType.CV_32F);
        double[] matchValue;

        // Сравнение по всем пикселям
        for (int y = 0; y <= targetGray.rows() - kernelSize; y++) {
            for (int x = 0; x <= targetGray.cols() - kernelSize; x++) {
                // Изменяем координаты ROI
                roi.x = x;
                roi.y = y;

                // Определяем области шаблонов
                targetRegion = targetGray.submat(roi);
                templateRegion = templateGray.submat(roi);

                // Сравниваем шаблоны
                Imgproc.matchTemplate(targetRegion, templateRegion, matchResult, Imgproc.TM_SQDIFF_NORMED);

                // Получаем значения сравнения и записываем результат
                matchValue = matchResult.get(0, 0);
                result.put(y, x, matchValue[0]);
            }
        }

        // Нормализуем полученную матрицу для визуализации изображения
        Core.normalize(result, result, 0, 255, Core.NORM_MINMAX, -1);

        return result;
    }

    public static Mat applyGaussianBlur(Mat src) {
        Mat result = new Mat();
        Imgproc.GaussianBlur(src, result, new Size(3, 3), 12);
        return result;
    }

    public static Mat applyCLAHE(Mat src) {
        Mat result = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(6, new Size(2, 2));
        clahe.apply(src, result);
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
