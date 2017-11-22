package sample;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import lombok.extern.java.Log;
import org.opencv.videoio.VideoCapture;

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

	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();


	// a flag to change the button behavior
	private boolean cameraActive = false;

	private FrameService frameService = new FrameService(capture);

	private BackgroundService backgroundService = new BackgroundService(frameService);

	private ScheduledExecutorService timer;


	public CameraService(Button button, Button saveBackgroundImage, ImageView image1, ImageView image2, ImageView image3) {
		this.button = button;
		this.saveBackgroundImage = saveBackgroundImage;
		this.image1 = image1;
		this.image2 = image2;
		this.image3 = image3;
	}

	public void startCamera(ActionEvent event) {
		if (!this.cameraActive) {
			tryToStartCameraAndGrabFrames();
		} else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.button.setText("Start Camera");
			// stop the timer
			stopCamera();
		}
	}

	private void tryToStartCameraAndGrabFrames() {
		// start the video capture
		this.capture.open(cameraId);
		// is the video stream available?
		if (this.capture.isOpened()) {
			this.cameraActive = true;
			// grab a frame every 33 ms (30 frames/sec)
			Runnable frameGrabber = new FrameGrabber(backgroundService,frameService, image1, image2, image3);
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
			// update the button content
			this.button.setText("Stop Camera");
		} else {
			// log the error
			log.log(Level.SEVERE, "Impossible to open the camera connection...");
		}
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
				log.log(Level.SEVERE, "Exception in stopping the frame capture, trying to release the camera now... ", e);
			}
		}
		if (this.capture.isOpened()) {
			// release the camera
			this.capture.release();
		}
	}

	public void saveBackgroundImage() {
		if (this.cameraActive && this.capture.isOpened()) {
			backgroundService.saveBackground();
		}
	}
}
