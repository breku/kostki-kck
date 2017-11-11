package sample;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by breku on 11.11.17.
 */
public class CameraService {

	// the id of the camera to be used
	private static int cameraId = 0;

	private final Button button;

	private final ImageView currentFrame;

	private final Label histogramCompareResult;

	private final ImageView histImage1;

	private final ImageView histImage2;

	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;

	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();

	private ImageReader imageReader = new ImageReader();

	// a flag to change the button behavior
	private boolean cameraActive = false;

	private FrameService frameService = new FrameService(capture);

	public CameraService(Button button, ImageView currentFrame, Label histogramCompareResult, ImageView histImage1, ImageView histImage2) {
		this.button = button;
		this.currentFrame = currentFrame;
		this.histogramCompareResult = histogramCompareResult;
		this.histImage1 = histImage1;
		this.histImage2 = histImage2;
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

					final Mat readImage = imageReader.readImage();
					if (!readImage.empty() && !frame.empty()) {
						Mat readImageResized = new Mat();
						Imgproc.resize(readImage, readImageResized, frame.size());


						Mat image1 = new Mat();
						Mat image2 = new Mat();


						readImageResized.convertTo(image1, CvType.CV_32F);
						frame.convertTo(image2, CvType.CV_32F);

						double res = Imgproc.compareHist(image1, image2, Imgproc.CV_COMP_CORREL);

						Image imageToShow1= Utils.mat2Image(readImageResized);
						Image imageToShow2= Utils.mat2Image(frame);
						updateImageView(histImage1, imageToShow1);
						updateImageView(histImage2, imageToShow2);

						final String format = String.format("Histogram compare result: %s", res);
						Platform.runLater(() -> {
							histogramCompareResult.textProperty().set(format);
						});
					}

					// convert and show the frame
					Image imageToShow = Utils.mat2Image(frame);
					updateImageView(currentFrame, imageToShow);
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

}
