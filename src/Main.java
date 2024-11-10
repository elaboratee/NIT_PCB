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

    private static final String DEFECT_TYPE = "Missing_hole";
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

        // Поиск дефектов на изображениях
        findDefects();
    }

    private static void preProcessTemplates() {
        try (Stream<Path> pathStream = Files.walk(Paths.get(DatasetProcessing.PCB_USED_DIR))) {
            // Поток путей к шаблонам
            List<Path> pathList = pathStream.toList();

            // Обработка шаблонов
            for (Path path : pathList) {
                String pathString = path.toString().toLowerCase();
                if (pathString.endsWith(".jpg")) {
                    // Получение имени шаблона
                    String[] splittedPath = pathString.split("\\\\");
                    String imageName = splittedPath[splittedPath.length - 1];

                    // Загрузка изображения
                    Mat src = ImageIO.loadImage(pathString);

                    // Изменение размеров шаблона (до 25%)
                    Imgproc.resize(src, src,
                            new Size(src.cols() / 2.0, src.rows() / 2.0));

                    // Предварительная обработка шаблона
                    Mat preprocessed = preProcessImage(src);

                    // Сохранение шаблона
                    ImageIO.saveImage("img\\templates\\" + imageName, preprocessed);
                }
            }
        } catch (IOException | ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void findDefects() {
        // Путь к CSV-файлу
        String csvFile = "info\\" + DEFECT_TYPE + "_" + OPTIMIZATION_TYPE + ".csv";

        try (Stream<Path> pathStream = Files.walk(Paths.get(PATH));
             PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {

            // Поток путей к файлам из директории
            List<Path> pathList = pathStream.toList();

            // Обработка файлов из директории
            Mat targetSrc = null;
            String lastLoadedTemplate = "";
            for (Path path : pathList) {
                String pathString = path.toString();
                if (pathString.endsWith(".jpg")) {
                    // Получение имени изображения с дефектом
                    String[] splittedPath = pathString.split("\\\\");
                    String imageName = splittedPath[splittedPath.length - 1];

                    // Загрузка изображений
                    String templateCode = imageName.substring(0, 2);
                    if (!lastLoadedTemplate.equals(templateCode)) {
                        targetSrc = ImageIO.loadImage("img\\templates\\" + templateCode + IMG_LOAD_FORMAT);
                        lastLoadedTemplate = templateCode;
                    }
                    Mat templateSrc = ImageIO.loadImage(pathString);

                    // Преобразование загруженного шаблона в оттенки серого
                    Mat targetGray = new Mat(targetSrc.rows(), targetSrc.cols(), CvType.CV_8UC1);
                    Imgproc.cvtColor(targetSrc, targetGray, Imgproc.COLOR_BGR2GRAY);

                    // Изменение размеров изображения (до 25%)
                    Imgproc.resize(templateSrc, templateSrc,
                            new Size(templateSrc.cols() / 2.0, templateSrc.rows() / 2.0));


//                    Mat sourceGray = new Mat(templateSrc.rows(), templateSrc.cols(), CvType.CV_8UC1);
//                    Imgproc.cvtColor(templateSrc, sourceGray, Imgproc.COLOR_BGR2GRAY);
//                    Mat sourceHist = Hists.createHist(sourceGray);
//                    ImageIO.saveImage(
//                            "img\\processed\\" + DEFECT_TYPE + "\\" +
//                                    imageName.substring(0, imageName.length() - 4) + "_source_hist" + IMG_LOAD_FORMAT,
//                            sourceHist
//                    );
//
//                    Mat filteredImg = Filters.applyGaussianBlur(sourceGray);
//                    ImageIO.saveImage(
//                            "img\\processed\\" + DEFECT_TYPE + "\\" +
//                                    imageName.substring(0, imageName.length() - 4) + "_filtered" + IMG_SAVE_FORMAT,
//                            filteredImg
//                    );
//                    Mat filteredHist = Hists.createHist(filteredImg);
//                    ImageIO.saveImage(
//                            "img\\processed\\" + DEFECT_TYPE + "\\" +
//                                    imageName.substring(0, imageName.length() - 4) + "_filtered_hist" + IMG_LOAD_FORMAT,
//                            filteredHist
//                    );
//
//                    Mat claheImg = Filters.applyCLAHE(filteredImg);
//                    ImageIO.saveImage(
//                            "img\\processed\\" + DEFECT_TYPE + "\\" +
//                                    imageName.substring(0, imageName.length() - 4) + "_clahe" + IMG_SAVE_FORMAT,
//                            claheImg
//                    );
//                    Mat claheHist = Hists.createHist(claheImg);
//                    ImageIO.saveImage(
//                            "img\\processed\\" + DEFECT_TYPE + "\\" +
//                                    imageName.substring(0, imageName.length() - 4) + "_clahe_hist" + IMG_LOAD_FORMAT,
//                            claheHist
//                    );


                    // Начало замера времени работы
                    long startTime = System.currentTimeMillis();

                    // Предварительная обработка изображений
                    Mat preprocessedTemplate = preProcessImage(templateSrc);

                    // Поиск дефектов методом сравнения с шаблоном
                    Mat matchedImg = matchTemplate(targetGray, preprocessedTemplate);

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

        // Создание изображения с выделенными дефектами
        Mat boundedImg = new Mat();
        targetImg.copyTo(boundedImg);

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
        try (Stream<Path> pathStream = Files.walk(Paths.get(DatasetProcessing.IMG_DIR))) {
            // Парсинг аннотаций для всех файлов
            List<List<Map<String, String>>> annotations = DatasetProcessing.parseAllAnnotations();

            // Визуализация аннотаций
            List<Path> pathList = pathStream.toList();
            for (Path path : pathList) {
                String pathString = path.toString();
                if (pathString.toLowerCase().endsWith(".jpg")) {
                    // Получение имя изображения
                    String[] splittedPath = pathString.split("\\\\");
                    String imageName = splittedPath[splittedPath.length - 1];

                    // Загрузка изображение
                    Mat loadedImage = ImageIO.loadImage(pathString);

                    // Визуализация аннотаций для изображения
                    Mat visualisedAnnot = Processing.visualizeAnnotations(
                            loadedImage,
                            annotations,
                            imageName
                    );

                    // Сборка пути для сохранения файла
                    String pathToSave = "img\\annotated\\" + DatasetProcessing.getSubfolder(imageName) + "\\" +
                            imageName.substring(0, imageName.length() - 4) + "_annotated.png";

                    // Сохранение аннотированного файла
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
