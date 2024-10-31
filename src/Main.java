import dataset.DatasetProcessing;
import exception.HistCreationException;
import exception.ImageReadException;
import exception.ImageWriteException;
import image.Filters;
import image.Hists;
import image.ImageIO;
import image.Processing;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Main {

    private static final String DEFECT_TYPE = "Short";
    private static final String PATH = DatasetProcessing.IMG_DIR + "\\" + DEFECT_TYPE;
    private static final String IMG_LOAD_FORMAT = ".jpg";
    private static final String IMG_SAVE_FORMAT = ".png";

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        findDefects();
    }

    private static void findDefects() {
        try (Stream<Path> pathStream = Files.walk(Paths.get(PATH))) {
            List<Path> pathList = pathStream.toList();

            // Получаем пути ко всем файлам и директориям
            for (Path path : pathList) {
                String pathString = path.toString();
                if (pathString.endsWith(".jpg")) {
                    // Получение имени изображения с дефектом
                    String[] splittedPath = pathString.split("\\\\");
                    String imageName = splittedPath[splittedPath.length - 1];

                    // Загрузка изображений
                    Mat targetSrc = ImageIO.loadImage(DatasetProcessing.PCB_USED_DIR + "\\" +
                            imageName.substring(0, 2) + IMG_LOAD_FORMAT);
                    Mat templateSrc = ImageIO.loadImage(pathString);

                    // Предварительная обработка изображений
                    Mat preprocessedTarget = preprocessImage(targetSrc);
                    Mat preprocessedTemplate = preprocessImage(templateSrc);

                    // Поиск дефектов методом сравнения с шаблоном
                    Mat matchedImg = matchTemplate(preprocessedTarget, preprocessedTemplate);

                    // Выделение дефектов рамками
                    Mat boundedImg = boundDefects(matchedImg);

                    // Сохранение полученного изображения
                    ImageIO.saveImage(
                            "img\\processed\\" + DEFECT_TYPE + "\\" +
                                    imageName.substring(0, imageName.length() - 4) + IMG_SAVE_FORMAT,
                            boundedImg);
                }
            }

        } catch (IOException | ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }

    private static Mat preprocessImage(Mat sourceImg) {
        // Преобразование к grayscale
        Mat sourceGray = new Mat(sourceImg.rows(), sourceImg.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(sourceImg, sourceGray, Imgproc.COLOR_BGR2GRAY);

        // Применение Gaussian Blur
        Mat sourceBlur = Filters.applyGaussianBlur(sourceGray);

        // Выравнивание гистограммы
        return Filters.applyCLAHE(sourceBlur);
    }

    private static Mat matchTemplate(Mat targetImg,
                                     Mat templateImg) {
        return Processing.matchTemplate(targetImg, templateImg);
    }

    private static Mat boundDefects(Mat sourceImg) {
        List<MatOfPoint> contours = Processing.findContours(sourceImg);

        Mat contoursImg = new Mat();
        sourceImg.copyTo(contoursImg);
        Imgproc.cvtColor(contoursImg, contoursImg, Imgproc.COLOR_GRAY2BGR);
        Imgproc.drawContours(contoursImg, contours, -1, new Scalar(255, 0, 0), 1);

        Mat boundedImg = new Mat();
        contoursImg.copyTo(boundedImg);

        List<Rect> boundingRects = Processing.getBoundingRects(contours);
        for (Rect rect : boundingRects) {
            Imgproc.rectangle(boundedImg, rect, new Scalar(0, 0, 255), 1);
        }

        return boundedImg;
    }

    private static void annotateImages() {
        // Визуализируем аннотации
        try (Stream<Path> pathStream = Files.walk(Paths.get(DatasetProcessing.IMG_DIR))) {
            // Парсим аннотации для всех файлов
            List<List<Map<String, String>>> annotations = DatasetProcessing.parseAllAnnotations();

            List<Path> pathList = pathStream.toList();
            for (Path path : pathList) {
                String pathString = path.toString();
                if (pathString.toLowerCase().endsWith(".jpg")) {
                    // Получаем имя изображения
                    String[] splittedPath = pathString.split("\\\\");
                    String imageName = splittedPath[splittedPath.length - 1];

                    // Загружаем изображение
                    Mat loadedImage = ImageIO.loadImage(pathString);

                    // Визуализируем аннотации для изображения.
                    Mat visualisedAnnot = Processing.visualizeAnnotations(
                            loadedImage,
                            annotations,
                            imageName
                    );

                    // Собираем путь для сохранения файла
                    String pathToSave = "img\\annotated\\" + DatasetProcessing.getSubfolder(imageName) + "\\" +
                            imageName.substring(0, imageName.length() - 4) + "_annotated.png";

                    // Сохраняем аннотированный файл
                    ImageIO.saveImage(pathToSave, visualisedAnnot);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void createHists() {
        try {
            // Создание гистограмм блюра
            Mat targetBlurHist = Hists.createHist(PATH + "\\blur\\target_blur" + IMG_SAVE_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\target\\target_blur_hist" + IMG_SAVE_FORMAT, targetBlurHist);

            Mat templateBlurHist = Hists.createHist(PATH + "\\blur\\template_blur" + IMG_SAVE_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\template\\template_blur_hist" + IMG_SAVE_FORMAT, templateBlurHist);

            // Создание выравненных гистограмм
            Mat targetClaheHist = Hists.createHist(PATH + "\\clahe\\target_clahe" + IMG_SAVE_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\target\\target_clahe_hist" + IMG_SAVE_FORMAT, targetClaheHist);

            Mat templateClaheHist = Hists.createHist(PATH + "\\clahe\\template_clahe" + IMG_SAVE_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\template\\template_clahe_hist" + IMG_SAVE_FORMAT, templateClaheHist);

        } catch (HistCreationException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }
}
