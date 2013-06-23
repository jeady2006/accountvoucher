package jing.util.lang;

public class StringUtils {
	public static String padLeft(String s, int len, String padString) {
		int fill = len - s.length();
		if (fill > 0) {
			for (int i = 0; i < fill; i++) {
				s = padString + s;
			}
		}
		return s;
	}
}
