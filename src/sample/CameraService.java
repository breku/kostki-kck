package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.java.Log;
import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by breku on 11.11.17.
 */
@Log
public class CameraService {

	// the id of the camera to be used
	private static int cameraId = 0;

	private final Button button;

	private final ImageView image1;

	private final ImageView image2;

	private final ImageView image3;

	private final Button saveBackgroundImage;


	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;

	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();

	private ImageReader imageReader = new ImageReader();

	// a flag to change the button behavior
	private boolean cameraActive = false;

	private FrameService frameService = new FrameService(capture);

	private boolean backgroundExists;


	public CameraService(Button button, Button saveBackgroundImage, ImageView image1, ImageView image2, ImageView image3) {
		this.button = button;
		this.saveBackgroundImage = saveBackgroundImage;
		this.image1 = image1;
		this.image2 = image2;
		this.image3 = image3;
	}

	public void startCamera(ActionEvent event) {
		if (!this.cameraActive) {
			// start the video capture
			this.capture.open(cameraId);

			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = () -> {
					// effectively grab and process a single frame
					Mat frame = frameService.grabFrame();


					updateImageView(image1, Utils.mat2Image(frame));
					if (backgroundExists && !frame.empty()) {
						final Mat backgroundMat = imageReader.readImage("background.jpg");
						updateImageView(image2, Utils.mat2Image(backgroundMat));

						final Mat diffMat = new Mat();
						Core.absdiff(backgroundMat, frame, diffMat);

						final Mat diffWithThreshold0Mat = new Mat();
						Imgproc.threshold(diffMat, diffWithThreshold0Mat, 100, 255, Imgproc.THRESH_BINARY);


						Imgproc.Canny(diffWithThreshold0Mat, diffWithThreshold0Mat, 2, 2 * 2, 3, false);

						List<MatOfPoint> diceContours = new ArrayList<>();
						Mat diceHierarchy = new Mat();
						Imgproc.findContours(diffWithThreshold0Mat, diceContours, diceHierarchy, 0, Imgproc.CHAIN_APPROX_SIMPLE);

//						Imgproc.drawContours(diffWithThreshold0Mat,diceContours,-1,new Scalar(255,100,100));
						for (MatOfPoint diceContour : diceContours) {
							final double diceContourArea = Imgproc.contourArea(diceContour);

							if (diceContourArea > 2000 && diceContourArea < 4000) {

								// get bounding rectangle
								final Rect rect = Imgproc.boundingRect(diceContour);

//								cv::Rect rect = cv::boundingRect( cv::Mat(diceContours[i]) );
								// set dice roi
//
//								Mat dice = diffWithThreshold0Mat.adjustROI(rect.height, rect.width, rect.x, rect.y);
								Mat dice = diffMat.submat(rect);

								// resize
								Imgproc.resize(dice, dice,new Size(150,150));
								Imgproc.threshold(dice, dice, 150, 255, Imgproc.THRESH_BINARY);

								// floodfill

								Imgproc.floodFill(dice,new Mat(),new Point(0,0),new Scalar(255));
								Imgproc.floodFill(dice,new Mat(),new Point(0,149),new Scalar(255));
								Imgproc.floodFill(dice,new Mat(),new Point(149,0),new Scalar(255));
								Imgproc.floodFill(dice,new Mat(),new Point(149,149),new Scalar(255));
								// convert to grayscale
//								if(!dice.empty()){
//									Imgproc.cvtColor(dice, dice, Imgproc.COLOR_BGR2GRAY);
//
//								}

								FeatureDetector blobDetector;
								blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
								MatOfKeyPoint keypoints = new MatOfKeyPoint();
								blobDetector.detect(dice, keypoints);
								blobDetector.read(getClass().getResource("/").getPath()+"blobReaderParameters.xml");
								log.log(Level.INFO, "" + keypoints.size());
								updateImageView(image3, Utils.mat2Image(dice));

								// threshold
//								cv::threshold(dice, dice, 150, 255, cv::THRESH_BINARY | CV_THRESH_OTSU );


								// floodfill
//								cv::floodFill(dice, cv::Point(0,0), cv::Scalar(255));
//								cv::floodFill(dice, cv::Point(0,149), cv::Scalar(255));
//								cv::floodFill(dice, cv::Point(149,0), cv::Scalar(255));
//								cv::floodFill(dice, cv::Point(149,149), cv::Scalar(255));
//								cv::Mat diceROI = frame(rect);

								// count number of pips
//								int numberOfPips = countPips(diceROI);

							}
						}


					}

				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				this.button.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.button.setText("Start Camera");

			// stop the timer
			stopCamera();
		}
	}

	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	public void stopCamera() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}

	public void saveBackgroundImage() {
		if (this.cameraActive && this.capture.isOpened()) {
			backgroundExists = true;
			Mat frame = frameService.grabFrame();
			final String format = String.format("%s%s.jpg", getClass().getResource("img/").getPath(), "background");
			log.log(Level.INFO, "> Saving image to " + format);
			Imgcodecs.imwrite(format, frame);
			log.log(Level.INFO, "< Finished");
		}
	}
}
