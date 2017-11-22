package sample;

import lombok.extern.java.Log;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.logging.Level;

/**
 * Created by breku on 22.11.17.
 */
@Log
public class BackgroundService {


	private final FrameService frameService;

	private boolean backgroundExists;

	public BackgroundService(FrameService frameService) {
		this.frameService = frameService;
	}

	public boolean isBackgroundExists() {
		return backgroundExists;
	}

	public void saveBackground() {
		Mat frame = frameService.grabGrayFrame();
		final String format = String.format("%s%s.jpg", getClass().getResource("img/").getPath(), "background");
		log.log(Level.INFO, "> Saving image to " + format);
		Imgcodecs.imwrite(format, frame);
		log.log(Level.INFO, "< Finished");
		backgroundExists = true;
	}
}
