package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class LostWork extends SalaryType {
	public static final int TYPE = 4;
	public LostWork(String month) {
		setMonth(month);
		setTypeName("失业");
		setChart("5101404");
		setReverseChar("2181090");
		setText("计提" + month + "月失业");
		setAssignment(month + "月失业");
		setSecondAssignment(month + "月上海外服失业");
		setType(TYPE);
	}
}
