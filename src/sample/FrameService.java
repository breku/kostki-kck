package sample;

import lombok.extern.java.Log;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.logging.Level;

/**
 * Created by breku on 11.11.17.
 */
@Log
public class FrameService {

	private final VideoCapture capture;

	public FrameService(VideoCapture capture) {
		this.capture = capture;
	}

	public Mat grabGrayFrame() {
		final Mat frame = grabFrame();
		// if the frame is not empty, process it
		if (!frame.empty()) {
			Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
		}
		return frame;
	}

	public Mat grabFrame() {
		// init everything
		Mat frame = new Mat();

		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception during the image elaboration", e);
			}
		}

		return frame;
	}
}
