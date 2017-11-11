package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

	// the FXML button
	@FXML
	private Button button;

	// the FXML image view
	@FXML
	private ImageView currentFrame;

	@FXML
	private ImageView readImage;

	@FXML
	private ImageView histImage1;

	@FXML
	private ImageView histImage2;

	@FXML
	private Label histogramCompareResult;


	private CameraService cameraService;

	private ImageReader imageReader;


	/**
	 * The action triggered by pushing the button on the GUI
	 *
	 * @param event the push button event
	 */
	@FXML
	protected void startCamera(ActionEvent event) {
		cameraService.startCamera(event);
	}


	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 *
	 * @param view  the {@link ImageView} to update
	 * @param image the {@link Image} to show
	 */

	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed() {
		cameraService.stopCamera();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cameraService = new CameraService(button, currentFrame, histogramCompareResult,histImage1,histImage2);
		imageReader = new ImageReader();
		Image imageToShow = Utils.mat2Image(imageReader.readImage());
		Utils.onFXThread(readImage.imageProperty(), imageToShow);
	}
}
