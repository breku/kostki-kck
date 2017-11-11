package sample;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by breku on 11.11.17.
 */
public class ImageReader {

	public Mat readImage() {
		final Mat imread = Imgcodecs.imread(getClass().getResource("img/1.png").getPath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		return imread;

	}
}
