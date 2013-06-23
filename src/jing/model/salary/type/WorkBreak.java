package jing.model.salary.type;

import jing.model.salary.SalaryType;


public class WorkBreak extends SalaryType {
	public static final int TYPE = 5;
	public WorkBreak(String month) {
		setMonth(month);
		setTypeName("工伤");
		setChart("5101501");
		setReverseChar("2181110");
		setText("计提" + month + "月份工伤保险");
		setAssignment(month + "月工伤");
		setSecondAssignment(month + "月上海外服工伤");
		setType(TYPE);
	}
}
