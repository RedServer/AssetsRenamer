package ru.redserver.assetsrenamer;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;

public class RenamerThread extends Thread {

	private final File assetsIndex;
	private final Action action;
	private final File assetsDir;
	private final List<String> duplicateResources = new ArrayList<>();
	private Runnable progressHandler;
	private Runnable endHandler;
	public volatile int totalResources = 0;
	public volatile int completedResources = 0;

	public RenamerThread(File assetsIndex, Action action) {
		super("RenamerThread");
		duplicateResources.add("icons/icon_32x32.png");
		duplicateResources.add("icons/icon_16x16.png");
		duplicateResources.add("icons/minecraft.icns");
		this.assetsIndex = assetsIndex;
		this.assetsDir = assetsIndex.getParentFile().getParentFile();
		this.action = action;
	}

	public void setOnProgress(Runnable handler) {
		progressHandler = handler;
	}

	public void setOnEnd(Runnable handler) {
		endHandler = handler;
	}

	@Override
	public void run() {
		System.out.println("== RENAMER ==");
		System.out.println("Индекс: " + assetsIndex + ", действие: " + action.name());

		try {
			if(action == Action.DECODE) {
				cleanup();

				AssetsIndex index = new Gson().fromJson(new FileReader(assetsIndex), AssetsIndex.class);
				totalResources = index.objects.size();

				for(Map.Entry<String, AssetsIndex.Entry> entry : index.objects.entrySet()) {
					if(duplicateResources.contains(entry.getKey())) {
						continue;
					}
					try {
						File resource = new File(assetsDir, Main.OBJECTS_DIR + File.separator + entry.getValue().hash.substring(0, 2) + File.separator + entry.getValue().hash);
						File destination = new File(assetsDir, entry.getKey().replace("/", File.separator));
						System.out.println("Обработка: " + entry.getKey());
						destination.getParentFile().mkdirs();
						Files.copy(resource.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					completedResources++;

					if(progressHandler != null) {
						Platform.runLater(() -> progressHandler.run());
					}

					if(isInterrupted()) {
						System.out.println("Задание было прервано.");
						if(endHandler != null) {
							Platform.runLater(() -> endHandler.run());
						}
						return;
					}
				}

				removeObjects();

			} else if(action == Action.ENCODE) {

				AssetsIndex newIndex = new AssetsIndex();
				newIndex.objects = new HashMap<>();

				List<String> exclude = new ArrayList<>();
				exclude.add(Main.INDEXES_DIR);
				exclude.add(Main.OBJECTS_DIR);
				List<File> files = Utils.scanDir(assetsDir, exclude, 0);
				totalResources = files.size();
				if(!files.isEmpty()) {

					removeObjects();

					for(File resource : files) {
						String key = resource.getAbsolutePath().substring(assetsDir.getAbsolutePath().length() + 1).replace(File.separator, "/");
						String hash = Utils.sha1File(resource);
						File renamed = new File(assetsDir, Main.OBJECTS_DIR + File.separator + hash.substring(0, 2) + File.separator + hash);
						renamed.getParentFile().mkdirs();
						Files.copy(resource.toPath(), renamed.toPath(), StandardCopyOption.REPLACE_EXISTING);

						System.out.println("Обработка: " + key);

						newIndex.objects.put(key, new AssetsIndex.Entry(hash, resource.length()));

						String dupKey = key.substring("minecraft/".length());
						if(duplicateResources.contains(dupKey)) {
							newIndex.objects.put(dupKey, newIndex.objects.get(key));
						}

						completedResources++;

						if(progressHandler != null) {
							Platform.runLater(() -> progressHandler.run());
						}
						if(isInterrupted()) {
							System.out.println("Задание было прервано.");
							if(endHandler != null) {
								Platform.runLater(() -> endHandler.run());
							}
							return;
						}
					}

					// создание нового индекса
					JsonWriter writer = new JsonWriter(new FileWriter(assetsIndex));
					writer.setIndent("\t");
					new Gson().toJson(newIndex, AssetsIndex.class, writer);
					writer.close();
					cleanup();
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("Завершено!");

		if(endHandler != null) {
			Platform.runLater(() -> endHandler.run());
		}
	}

	private void cleanup() {
		for(File file : assetsDir.listFiles()) {
			if(!file.getName().equalsIgnoreCase(Main.INDEXES_DIR) && !file.getName().equalsIgnoreCase(Main.OBJECTS_DIR)) {
				Utils.deleteRecursive(file);
			}
		}
	}

	private void removeObjects() {
		Utils.deleteRecursive(new File(assetsDir, Main.OBJECTS_DIR));
	}

}
