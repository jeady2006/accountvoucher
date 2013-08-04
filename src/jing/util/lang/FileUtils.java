package jing.util.lang;

import java.io.IOException;

public class FileUtils {
	public static String getSystemPath() {
		return System.getProperty("user.dir");
	}

	public static void popupFilePath(String filePath) {
		try {
			Runtime.getRuntime()
					.exec("explorer " + getSystemPath()
							+ filePath.replace("/", "\\"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
