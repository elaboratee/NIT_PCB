package dataset;

import exception.AnnotationParseException;

import javax.management.modelmbean.XMLParseException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatasetProcessing {

    public static final String DATASET_DIR = "D:\\DATASETS\\PCB_DATASET";
    public static final String IMG_DIR = DATASET_DIR + "\\images";
    public static final String ANNOT_DIR = DATASET_DIR + "\\Annotations";
    public static final String PCB_USED_DIR = DATASET_DIR + "\\PCB_USED";

    public static List<List<Map<String, String>>> parseAllAnnotations() throws IOException {
        List<List<Map<String, String>>> parsedData = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(Paths.get(ANNOT_DIR))) {
            List<Path> paths = pathStream.toList();
            for (Path path : paths) {
                if (path.toString().toLowerCase().endsWith(".xml")) {
                    parsedData.add(XMLParsing.parseXML(path.toString()));
                }
            }
        } catch (XMLParseException e) {
            return new ArrayList<>();
        }
        return parsedData;
    }

    public static String getSubfolder(String imageName) {
        String splittedName = Arrays.toString(imageName.split("_"));
        String subfolder;
        if (splittedName.contains("missing")) {
            subfolder = "Missing_hole";
        } else if (splittedName.contains("mouse")) {
            subfolder = "Mouse_bite";
        } else if (splittedName.contains("open")) {
            subfolder = "Open_circuit";
        } else if (splittedName.contains("short")) {
            subfolder = "Short";
        } else if (splittedName.contains("spurious")) {
            subfolder = "Spurious_copper";
        } else {
            subfolder = "Spur";
        }
        return subfolder;
    }

    public static List<Map<String, String>> filterAnnotationsByFilename(
            List<List<Map<String, String>>> allAnnotations,
            String filename) {
        return allAnnotations.stream()
                .flatMap(List::stream)
                .filter(annotation -> filename.equals(annotation.get("filename")))
                .collect(Collectors.toList());
    }

    /**
     * Count files (or directories) on the given {@code directoryPath}
     * @param directoryPath path to the directory.
     * @return number of files/directories in the given {@code directoryPath}
     * @throws IOException if an I/ O error occurs when opening the directory
     */
    public static long countFiles(String directoryPath) throws IOException {
        File dir = new File(directoryPath);
        long count = 0;
        if (dir.exists() && dir.isDirectory()) {
            try (Stream<Path> files = Files.list(Paths.get(directoryPath))) {
                count = files.count();
            }
        }
        return count;
    }

    public static void generateDefectCountCSV() {
        // Имя CSV-файла
        String csvFile = "info\\DEFECT_COUNT.csv";

        List<Map<String, Integer>> defectData;
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
            // Получение данных о дефектах
            defectData = parseAllAnnotationsWithDefectCount();

            for (Map<String, Integer> data : defectData) {
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    writer.println(entry.getKey() + "," + entry.getValue());
                }
            }
            writer.flush();

        } catch (IOException | AnnotationParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Map<String, Integer>> parseAllAnnotationsWithDefectCount()
            throws AnnotationParseException {

        List<Map<String, Integer>> defectCountData = new ArrayList<>();
        try (Stream<Path> pathStream = Files.walk(Paths.get(ANNOT_DIR))) {
            List<Path> paths = pathStream.toList();
            for (Path path : paths) {
                if (path.toString().toLowerCase().endsWith(".xml")) {
                    // Получение имени файла
                    String fileName = path.getFileName().toString().replace(".xml", ".jpg");

                    // Подсчет количества дефектов
                    int defectCount = XMLParsing.countDefects(path.toString());

                    // Сохранение информации
                    Map<String, Integer> defectInfo = new HashMap<>();
                    defectInfo.put(fileName, defectCount);
                    defectCountData.add(defectInfo);
                }
            }
        } catch (IOException | XMLParseException e) {
            throw new AnnotationParseException(e);
        }
        return defectCountData;
    }
}
