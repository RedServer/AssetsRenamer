package ru.redserver.assetsrenamer;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String OBJECTS_DIR = "objects";
	public static final String INDEXES_DIR = "indexes";

	private Stage window;
	private static Main instance;

	public Main() {
		instance = this;
	}

	public static Main getInstance() {
		return instance;
	}

	public Stage getWindow() {
		return window;
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		window = primaryStage;
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("/resources/main.fxml"));
		Parent root = loader.load();
		Scene scene = new Scene(root);

		primaryStage.setTitle("Assets Renamer");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/icon.png")));
		primaryStage.show();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
