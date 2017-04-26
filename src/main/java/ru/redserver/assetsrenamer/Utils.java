package ru.redserver.assetsrenamer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.stage.StageStyle;

public class Utils {

	public static File getMinecraftDir() {
		String userHome = System.getProperty("user.home", ".");
		File workingDirectory;
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("linux") || osName.contains("unix")) {
			workingDirectory = new File(userHome, ".minecraft/");
		} else if(osName.contains("win")) {
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData : userHome;
			workingDirectory = new File(folder, ".minecraft/");
		} else if(osName.contains("mac")) {
			workingDirectory = new File(userHome, "Library/Application Support/minecraft");
		} else {
			workingDirectory = new File(userHome, "minecraft/");
		}
		return workingDirectory;
	}

	public static void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setHeaderText(null);
		alert.setTitle("Сообщение");
		alert.setContentText(message);
		alert.initStyle(StageStyle.UTILITY);
		alert.show();
	}

	public static void deleteRecursive(File file) {
		if(!file.exists()) {
			return;
		}
		if(file.isFile()) {
			boolean res = file.delete();
			//System.out.println("Удаление: " + file + " - " + res);
		} else if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				deleteRecursive(f);
			}
			file.delete();
		}
	}

	/**
	 * Получение SHA1 хеша файла
	 * @param file файл
	 * @return SHA1 хеш
	 */
	public static String sha1File(File file) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			FileInputStream fis = new FileInputStream(file);

			byte[] data = new byte[1024];
			int read = 0;
			while((read = fis.read(data)) != -1) {
				sha1.update(data, 0, read);
			}
			byte[] hashBytes = sha1.digest();

			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < hashBytes.length; i++) {
				sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			fis.close();
			return sb.toString().toLowerCase();
		} catch (NoSuchAlgorithmException | IOException ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public static List<File> scanDir(File dir, List<String> exclude, int deepth) {
		ArrayList<File> files = new ArrayList<>();
		if(dir.isDirectory()) {
			for(File file : dir.listFiles()) {
				if(file.isFile()) {
					files.add(file);
				} else if(file.isDirectory() && !(deepth == 0 && exclude.contains(file.getName()))) {
					files.addAll(scanDir(file, exclude, deepth + 1));
				}
			}
		}
		return files;
	}

}
