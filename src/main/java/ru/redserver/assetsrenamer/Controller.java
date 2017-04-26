package ru.redserver.assetsrenamer;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class Controller implements Initializable {

	private final FileChooser fileChooser;
	private boolean running = false;
	private RenamerThread task;
	private File indexFile;

	@FXML
	private Button startButton;
	@FXML
	private TextField indexFileInput;
	@FXML
	private ChoiceBox<Action> choiceAction;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Button buttonBrowse;

	public Controller() {
		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файл JSON", "*.json"));
		fileChooser.setTitle("Выберите файл индекса");

		File mcDir = Utils.getMinecraftDir();
		if(mcDir != null && mcDir.exists()) {
			fileChooser.setInitialDirectory(mcDir);
		}
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		choiceAction.getItems().addAll(Arrays.asList(Action.values()));
		choiceAction.getSelectionModel().select(0);

		indexFileInput.setEditable(false);

		buttonBrowse.setOnAction((ActionEvent event) -> {
			indexFile = fileChooser.showOpenDialog(Main.getInstance().getWindow());
			indexFileInput.setText((indexFile == null) ? "" : indexFile.getAbsolutePath());
		});

		startButton.setOnAction((ActionEvent event) -> {
			if(running) {
				if(task.isAlive()) {
					task.interrupt();
				}
			} else {
				Action selectedAction = choiceAction.getSelectionModel().getSelectedItem();

				if(indexFile == null) {
					Utils.showAlert("Не выбран файл индекса.");
					return;
				} else if(!indexFile.exists()) {
					Utils.showAlert("Файл индекса не существует.");
					return;
				} else if(selectedAction == Action.DECODE && !new File(indexFile.getParentFile().getParentFile(), Main.OBJECTS_DIR).exists()) {
					Utils.showAlert("Отсутствует папка " + Main.OBJECTS_DIR + ".");
					return;
				}
				task = new RenamerThread(indexFile, selectedAction);
				task.setOnProgress(() -> {
					progressBar.setProgress((double)task.completedResources / (double)task.totalResources);
				});
				task.setOnEnd(() -> {
					progressBar.setProgress(0);
					lockUIElements(false);
					running = false;
				});
				lockUIElements(true);
				task.start();
			}
			running = !running;
		});

	}

	private void lockUIElements(boolean lock) {
		choiceAction.setDisable(lock);
		buttonBrowse.setDisable(lock);
		indexFileInput.setDisable(lock);
		startButton.setText(lock ? "Отмена" : "Пуск");
	}

}
