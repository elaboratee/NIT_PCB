package image;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class Filters {

    public static Mat applyGaussianBlur(Mat src) {
        Mat result = new Mat();
        Imgproc.GaussianBlur(src, result, new Size(3, 3), 1.5);
        return result;
    }

    public static Mat applyCLAHE(Mat src) {
        Mat result = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(6, new Size(2, 2));
        clahe.apply(src, result);
        return result;
    }
}
