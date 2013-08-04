package jing.util.lang;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	public static String dateToString(Date date, String format) {
		if (date == null) {
			return "";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}

	public static String dateToString(Date date) {
		return dateToString(date, "yyyy/MM/dd");
	}

	public static Date getCurrentLastDateOfMonth() {
		return getLastDateOfMonth(null);
	}

	public static Date getLastDateOfMonth(Integer month) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DATE, 1);
		if (month != null) {
			c.set(Calendar.MONTH, month - 1);
		}
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DATE, -1);
		System.out.println(c.getTime().toString());
		return c.getTime();
	}

	public static void main(String[] args) {
		DateUtils.getCurrentLastDateOfMonth();
	}
}
