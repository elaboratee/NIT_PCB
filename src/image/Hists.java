package image;

import exception.HistCreationException;
import exception.ImageReadException;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Hists {

    private static final Scalar COLOR_BLACK = new Scalar(0, 0, 0);
    private static final Scalar COLOR_WHITE = new Scalar(255, 255, 255);

    public static Mat createHist(Mat src) {
        List<Mat> images = new ArrayList<>();
        images.add(src);

        Mat hist = new Mat();
        Imgproc.calcHist(
                images,
                new MatOfInt(0),
                new Mat(),
                hist,
                new MatOfInt(256),
                new MatOfFloat(0, 256)
        );
        Core.normalize(hist, hist, 0, 128, Core.NORM_MINMAX, -1);

        double v;
        int h = 150;

        Mat imgHist = new Mat(h, 256, CvType.CV_8UC3, COLOR_WHITE);
        for (int i = 0; i < hist.rows(); i++) {
            v = Math.round(hist.get(i, 0)[0]);
            if (v != 0) {
                Imgproc.line(
                        imgHist,
                        new Point(i, h - 1),
                        new Point(i, h - 1 - v),
                        COLOR_BLACK
                );
            }
        }

        return imgHist;
    }

    public static Mat createHist(String path) throws HistCreationException {
        Mat img;
        try {
            img = ImageIO.loadImage(path);
        } catch (ImageReadException e) {
            throw new HistCreationException(e);
        }
        return createHist(img);
    }
}
