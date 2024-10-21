import exception.HistCreationException;
import exception.ImageReadException;
import exception.ImageWriteException;
import image.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import dataset.DatasetProcessing;
import image.Hists;
import image.Images;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Main {

    private static final String PATH = "img_test\\Missing_hole";
    private static final String IMG_FORMAT = ".png";

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        preprocessImages();
        matchTemplate();
        createHists();
    }

    private static void preprocessImages() {
        try {
            // Загрузка исходных изображений
            Mat targetSrc = ImageIO.loadImage(PATH + "\\src\\target.jpg");
            Mat templateSrc = ImageIO.loadImage(PATH + "\\src\\template.jpg");

            // Преобразование к grayscale
            Mat targetGray = new Mat(targetSrc.rows(), targetSrc.cols(), CvType.CV_8UC1);
            Imgproc.cvtColor(targetSrc, targetGray, Imgproc.COLOR_BGR2GRAY);

            Mat templateGray = new Mat(templateSrc.rows(), templateSrc.cols(), CvType.CV_8UC1);
            Imgproc.cvtColor(templateSrc, templateGray, Imgproc.COLOR_BGR2GRAY);

            // Применение Gaussian Blur
            Mat targetBlur = Images.applyGaussianBlur(targetGray);
            ImageIO.saveImage(PATH + "\\blur\\target_blur" + IMG_FORMAT, targetBlur);

            Mat templateBlur = Images.applyGaussianBlur(templateGray);
            ImageIO.saveImage(PATH + "\\blur\\template_blur" + IMG_FORMAT, templateBlur);

            // Выравнивание гистограммы
            Mat targetClahe = Images.applyCLAHE(targetBlur);
            ImageIO.saveImage(PATH + "\\clahe\\target_clahe" + IMG_FORMAT, targetClahe);

            Mat templateClahe = Images.applyCLAHE(templateBlur);
            ImageIO.saveImage(PATH + "\\clahe\\template_clahe" + IMG_FORMAT, templateClahe);

        } catch (ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void matchTemplate() {
        try {
            // Загрузка предварительно обработанных изображений
            Mat targetImage = ImageIO.loadImage(
                    PATH + "\\clahe\\target_clahe" + IMG_FORMAT);
            Mat templateImage = ImageIO.loadImage(
                    PATH + "\\clahe\\template_clahe" + IMG_FORMAT);

            // Выполнение сравнения
            Mat result = Images.matchTemplate(targetImage, templateImage);

            // Сохранение результата сравнения
            ImageIO.saveImage(PATH + "\\match_result" + IMG_FORMAT, result);

        } catch (ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createHists() {
        try {
            // Создание гистограмм блюра
            Mat targetBlurHist = Hists.createHist(PATH + "\\blur\\target_blur" + IMG_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\target\\target_blur_hist" + IMG_FORMAT, targetBlurHist);

            Mat templateBlurHist = Hists.createHist(PATH + "\\blur\\template_blur" + IMG_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\template\\template_blur_hist" + IMG_FORMAT, templateBlurHist);

            // Создание выравненных гистограмм
            Mat targetClaheHist = Hists.createHist(PATH + "\\clahe\\target_clahe" + IMG_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\target\\target_clahe_hist" + IMG_FORMAT, targetClaheHist);

            Mat templateClaheHist = Hists.createHist(PATH + "\\clahe\\template_clahe" + IMG_FORMAT);
            ImageIO.saveImage(PATH + "\\hist\\template\\template_clahe_hist" + IMG_FORMAT, templateClaheHist);

        } catch (HistCreationException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void detectBoundaries() {
        try {
            // Загрузка исходных изображений
            Mat targetSrc = ImageIO.loadImage(PATH + "\\src\\target.jpg");
            Mat templateSrc = ImageIO.loadImage(PATH + "\\src\\template.jpg");

            // Преобразование к grayscale
            Mat targetGray = new Mat(targetSrc.rows(), targetSrc.cols(), CvType.CV_8UC1);
            Imgproc.cvtColor(targetSrc, targetGray, Imgproc.COLOR_BGR2GRAY);

            Mat templateGray = new Mat(templateSrc.rows(), templateSrc.cols(), CvType.CV_8UC1);
            Imgproc.cvtColor(templateSrc, templateGray, Imgproc.COLOR_BGR2GRAY);

            // Применение Gaussian Blur
            Mat targetBlur = Images.applyGaussianBlur(targetGray);
            ImageIO.saveImage(PATH + "\\blur\\target_blur_test" + IMG_FORMAT, targetBlur);

            Mat templateBlur = Images.applyGaussianBlur(templateGray);
            ImageIO.saveImage(PATH + "\\blur\\template_blur_test" + IMG_FORMAT, templateBlur);

            // Выравнивание гистограммы
            Mat targetClahe = Images.applyCLAHE(targetBlur);
            ImageIO.saveImage(PATH + "\\clahe\\target_clahe_test" + IMG_FORMAT, targetClahe);

            Mat templateClahe = Images.applyCLAHE(templateBlur);
            ImageIO.saveImage(PATH + "\\clahe\\template_clahe_test" + IMG_FORMAT, templateClahe);

            // Выделение границ
            Mat targetBoundaries = Images.highlightBoundaries(targetClahe);
            ImageIO.saveImage(PATH + "\\boundaries\\target_boundaries_test" + IMG_FORMAT, targetBoundaries);

            Mat templateBoundaries = Images.highlightBoundaries(templateClahe);
            ImageIO.saveImage(PATH + "\\boundaries\\template_boundaries_test" + IMG_FORMAT, templateBoundaries);

        } catch (ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void combineWithBoundaries() {
        try {
            Mat targetImage = ImageIO.loadImage(PATH + "\\src\\target.jpg");
            Mat targetBoundaries = ImageIO.loadImage(
                    PATH + "\\boundaries\\target_boundaries_test" + IMG_FORMAT);

            Mat templateImage = ImageIO.loadImage(PATH + "\\src\\template.jpg");
            Mat templateBoundaries = ImageIO.loadImage(
                    PATH + "\\boundaries\\template_boundaries_test" + IMG_FORMAT);

            Mat targetCombined = new Mat();
            Core.addWeighted(targetImage, 1, targetBoundaries, 0.75, 0, targetCombined);

            Mat templateCombined = new Mat();
            Core.addWeighted(templateImage, 1, templateBoundaries, 0.75, 0, templateCombined);

            ImageIO.saveImage(
                    PATH + "\\combination\\target_combination_test" + IMG_FORMAT,
                    targetCombined
            );
            ImageIO.saveImage(
                    PATH + "\\combination\\template_combination_test" + IMG_FORMAT,
                    templateCombined
            );
        } catch (ImageReadException | ImageWriteException e) {
            throw new RuntimeException(e);
        }
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
                    Mat visualisedAnnot = Images.visualizeAnnotations(
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
}
