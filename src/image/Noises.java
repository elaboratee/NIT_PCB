package image;

import org.opencv.core.Mat;

import java.util.Random;

public class Noises {

    public static Mat gaussianNoise(Mat src) {
        // Создание матрицы шума
        Mat noise = Mat.zeros(src.size(), src.type());

        // Параметры распределения
        Random random = new Random();
        double mean = 0;
        double sigma = 7;

        // Заполнение шума значениями по нормальному распределению
        for (int i = 0; i < noise.rows(); i++) {
            for (int j = 0; j < noise.cols(); j++) {
                double noiseValue = random.nextGaussian() * sigma + mean;
                double pixelValue = src.get(i, j)[0] + noiseValue;
                noise.put(i, j, Math.min(255, Math.max(0, pixelValue)));
            }
        }

        return noise;
    }

    public static Mat saltAndPepperNoise(Mat src) {
        Mat noised = new Mat();
        src.copyTo(noised);

        // Вероятность соли и перца
        double prob = 0.03;
        Random random = new Random();

        // Добавление шума
        for (int i = 0; i < src.rows(); i++) {
            for (int j = 0; j < src.cols(); j++) {
                double randomValue = random.nextDouble();
                if (randomValue < prob / 2) {
                    // Перец (черный пиксель)
                    noised.put(i, j, 0);
                } else if (randomValue > 1 - prob / 2) {
                    // Соль (белый пиксель)
                    noised.put(i, j, 255);
                }
            }
        }

        return noised;
    }
}
