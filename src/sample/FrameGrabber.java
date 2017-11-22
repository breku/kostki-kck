package sample;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.java.Log;
import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by breku on 22.11.17.
 */
@Log
public class FrameGrabber implements Runnable {

	private final BackgroundService backgroundService;

	private final FrameService frameService;

	private final ImageView image1;

	private final ImageView image2;

	private final ImageView image3;

	private ImageReader imageReader = new ImageReader();


	public FrameGrabber(BackgroundService backgroundService, FrameService frameService, ImageView image1, ImageView image2, ImageView image3) {
		this.backgroundService = backgroundService;
		this.frameService = frameService;
		this.image1 = image1;
		this.image2 = image2;
		this.image3 = image3;
	}

	@Override
	public void run() {
		final Mat frame = frameService.grabFrame();
		if (!frame.empty()) {
			updateImageView(image1, Utils.mat2Image(frame));
		}
		if (backgroundService.isBackgroundExists() && !frame.empty()) {
			final Mat backgroundMat = imageReader.readImage("background.jpg");
			updateImageView(image2, Utils.mat2Image(backgroundMat));


			// Difference between current gray frame and gray background
			final Mat diffMat = new Mat();
			final Mat currentGrayFrame = frameService.grabGrayFrame();
			Core.absdiff(backgroundMat, currentGrayFrame, diffMat);



			final Mat diffWithThreshold0Mat = new Mat();
			Imgproc.threshold(diffMat, diffWithThreshold0Mat, 100, 255, Imgproc.THRESH_BINARY);


			Imgproc.Canny(diffWithThreshold0Mat, diffWithThreshold0Mat, 2, 2 * 2, 3, false);


			List<MatOfPoint> diceContours = new ArrayList<>();
			Mat diceHierarchy = new Mat();
			Imgproc.findContours(diffWithThreshold0Mat, diceContours, diceHierarchy, 0, Imgproc.CHAIN_APPROX_SIMPLE);

			for (MatOfPoint diceContour : diceContours) {
				final double diceContourArea = Imgproc.contourArea(diceContour);

				if (diceContourArea > 2000 && diceContourArea < 10000) {
					// get bounding rectangle
					final Rect rect = Imgproc.boundingRect(diceContour);
					Mat dice = diffMat.submat(rect);


					Imgproc.resize(dice, dice,new Size(150,150));
					Imgproc.threshold(dice, dice, 150, 255, Imgproc.THRESH_BINARY);


					Imgproc.floodFill(dice,new Mat(),new Point(0,0),new Scalar(255));
					Imgproc.floodFill(dice,new Mat(),new Point(0,149),new Scalar(255));
					Imgproc.floodFill(dice,new Mat(),new Point(149,0),new Scalar(255));
					Imgproc.floodFill(dice,new Mat(),new Point(149,149),new Scalar(255));


					FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
					MatOfKeyPoint keypoints = new MatOfKeyPoint();

					blobDetector.read(getClass().getResource("blobReaderParameters.xml").getPath());

					// Detect dots on dice
					blobDetector.detect(dice, keypoints);
					log.log(Level.INFO, "" + keypoints.size().height);
					updateImageView(image3, Utils.mat2Image(dice));


					Imgproc.putText(frame,"Val:"+(int)keypoints.size().height,new Point(rect.x,rect.y+rect.height+20),Core.FONT_HERSHEY_COMPLEX_SMALL,0.8,new Scalar(255),1,8,false);
					Imgproc.rectangle(frame,rect.tl(),rect.br(),new Scalar(0,153,255),5,8,0);
					updateImageView(image1, Utils.mat2Image(frame));

				}
			}


		}
	}

	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}
}
