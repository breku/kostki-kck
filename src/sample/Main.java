package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import org.opencv.core.Core;

import java.util.logging.Level;

@Log
public class Main extends Application {

	public static void main(String[] args) {
		loadOpenCVLib();
		launch(args);
	}

	private static void loadOpenCVLib() {
		log.log(Level.INFO, "> Loading OpenCV lib");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		log.log(Level.INFO, "< Finished.");
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
		primaryStage.setTitle("Kostki KCK");
		primaryStage.setScene(new Scene(root, 1200, 700));
		primaryStage.show();
	}
}
