package sample;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Created by breku on 11.11.17.
 */
public class ImageReader {

	public Mat readImage(String imageName) {
		final String imagePath = String.format("%s%s", getClass().getResource("img/").getPath(), imageName);
		final Mat imread = Imgcodecs.imread(imagePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		return imread;

	}
}
