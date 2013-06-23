package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class LabourUnion extends SalaryType {
	public static final int TYPE = 0;

	public LabourUnion(String month) {
		setMonth(month);
		setTypeName("工会");
		setChart("5101801");
		setReverseChar("2181140");
		setText("计提" + month + "月份工会经费");
		setAssignment(month + "月工会经费");
		setSecondAssignment(month + "月工会经费");
		setType(TYPE);
	}
}
