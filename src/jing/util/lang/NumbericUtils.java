package jing.util.lang;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class NumbericUtils {
	public static String doubleToString(double d) {
		String result = String.valueOf(d);
		return result.substring(0, result.indexOf("."));
	}

	public static boolean isBigDecimalEqual0(BigDecimal b) {
		if (Math.round(b.doubleValue() * 100) == 0) {
			return true;
		}
		return false;
	}

	public static boolean isBigDecimalGreatherThan0(BigDecimal b) {
		if (Math.round(b.doubleValue() * 100) > 0) {
			return true;
		}
		return false;
	}

	public static boolean isBigDecimalLessThan0(BigDecimal b) {
		if (Math.round(b.doubleValue() * 100) < 0) {
			return true;
		}
		return false;
	}

	public static String formatBigDecimal(BigDecimal number) {
		if (number == null) {
			return "";
		}
		NumberFormat format = NumberFormat.getInstance();
		format.setMinimumFractionDigits(2);
		format.setGroupingUsed(false);
		return format.format(number.doubleValue());
	}
}
