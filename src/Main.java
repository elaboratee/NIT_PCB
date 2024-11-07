import dataset.DatasetProcessing;
import exception.HistCreationException;
import exception.ImageReadException;
import exception.ImageWriteException;
import image.Filters;
import image.Hists;
import image.ImageIO;
import image.Processing;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class Main {

    private static final String DEFECT_TYPE = "Open_circuit";
    private static final String PATH = DatasetProcessing.IMG_DIR + "\\" + DEFECT_TYPE;
    private static final String IMG_LOAD_FORMAT = ".jpg";
    private static final String IMG_SAVE_FORMAT = ".png";
    private static final String OPTIMIZATION_TYPE = "OPTIMIZED";
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Настройка логгера
        LOGGER.setLevel(Level.INFO);
        try {
            FileHandler fileHandler = new FileHandler("info\\logs.log");
            LOGGER.addHandler(fileHandler);
        } catch (IOException | SecurityException e) {
            throw new RuntimeException(e);
        }

        // Обработка изображений
        findDefects();
    }

    private static void findDefects() {
        // Путь к CSV-файлу
        String csvFile = "info\\" + DEFECT_TYPE + "_" + OPTIMIZATION_TYPE + ".csv";

        try (Stream<Path> pathStream = Files.walk(Paths.get(PATH));
             PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {

            // Поток путей к файлам из директории
            List<Path> pathList = pathStream.toList();

            // Обработка файлов из директории
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

                    // Изменение размеров изображений (до 25%)
                    Imgproc.resize(targetSrc, targetSrc,
                            new Size(targetSrc.cols() / 2.0, targetSrc.rows() / 2.0));
                    Imgproc.resize(templateSrc, templateSrc,
                            new Size(templateSrc.cols() / 2.0, templateSrc.rows() / 2.0));

                    // Начало замера времени работы
                    long startTime = System.currentTimeMillis();

                    // Предварительная обработка изображений
                    Mat preprocessedTarget = preProcessImage(targetSrc);
                    Mat preprocessedTemplate = preProcessImage(templateSrc);

                    // Поиск дефектов методом сравнения с шаблоном
                    Mat matchedImg = matchTemplate(preprocessedTarget, preprocessedTemplate);

                    // Постобработка изображения
                    Mat dilatedImg = postProcessImage(matchedImg);

                    // Выделение дефектов рамками
                    Mat boundedImg = boundDefectsTarget(dilatedImg, templateSrc, imageName);

                    // Окончание замера времени работы
                    long processingTime = System.currentTimeMillis() - startTime;
                    System.out.println("Время обработки изображения " + imageName + " = " +
                            (processingTime / 1000.0) + " sec\n");

                    // Сохранение полученного изображения
                    ImageIO.saveImage(
                            "img\\processed\\" + DEFECT_TYPE + "\\" +
                                    imageName.substring(0, imageName.length() - 4) + IMG_SAVE_FORMAT,
                            boundedImg
                    );

                    // Запись данных в CSV
                    writer.println(imageName + "," + processingTime + "," + templateSrc.size());
                    writer.flush();
                }
            }
        } catch (IOException | ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }

    private static Mat preProcessImage(Mat sourceImg) {
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
        return Processing.matchTemplateOptimized(targetImg, templateImg);
    }

    private static Mat postProcessImage(Mat sourceImg) {
        return Processing.dilateImage(sourceImg);
    }

    private static Mat boundDefects(Mat sourceImg,
                                    String imgName) {
        // Поиск контуров
        List<MatOfPoint> contours = Processing.findContours(sourceImg);

        // Создание изображения контуров
        Mat contoursImg = new Mat();
        sourceImg.copyTo(contoursImg);
        Imgproc.cvtColor(contoursImg, contoursImg, Imgproc.COLOR_GRAY2BGR);
        Imgproc.drawContours(contoursImg, contours, -1, new Scalar(255, 0, 0), 1);

        // Создание изображения с выделенными дефектами
        Mat boundedImg = new Mat();
        contoursImg.copyTo(boundedImg);

        // Отрисовка выделений дефектов
        List<Rect> boundingRects = Processing.getBoundingRects(contours);
        for (Rect rect : boundingRects) {
            Imgproc.rectangle(boundedImg, rect, new Scalar(0, 0, 255), 1);

            if (rect.size().width >= 2 || rect.size().height >= 2) {
                String logMsg = String.format("На плате %s обнаружен дефект. Координаты дефекта: (%d, %d; %d, %d)",
                        imgName.substring(0, imgName.length() - 4),
                        rect.x, rect.y,
                        rect.x + rect.height, rect.y + rect.width);
                LOGGER.log(Level.WARNING, logMsg);
            }
        }

        return boundedImg;
    }

    private static Mat boundDefectsTarget(Mat sourceImg,
                                          Mat targetImg,
                                          String imgName) {
        // Поиск контуров
        List<MatOfPoint> contours = Processing.findContours(sourceImg);

        // Создание изображения контуров
        Mat contoursImg = new Mat();
        targetImg.copyTo(contoursImg);
        Imgproc.drawContours(contoursImg, contours, -1, new Scalar(255, 0, 0), 1);

        // Создание изображения с выделенными дефектами
        Mat boundedImg = new Mat();
        contoursImg.copyTo(boundedImg);

        // Отрисовка выделений дефектов
        List<Rect> boundingRects = Processing.getBoundingRects(contours);
        for (Rect rect : boundingRects) {
            Imgproc.rectangle(boundedImg, rect, new Scalar(0, 0, 255), 1);

            if (rect.size().width >= 2 || rect.size().height >= 2) {
                String logMsg = String.format("На плате %s обнаружен дефект. Координаты дефекта: (%d, %d; %d, %d)",
                        imgName.substring(0, imgName.length() - 4),
                        rect.x, rect.y,
                        rect.x + rect.height, rect.y + rect.width);
                LOGGER.log(Level.WARNING, logMsg);
            }
        }

        return boundedImg;
    }

    private static void showImage(Mat src) {
        HighGui.imshow("Processed Image", src);
        HighGui.waitKey(1);
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
