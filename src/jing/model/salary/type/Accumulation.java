package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class Accumulation extends SalaryType {
	public static final int TYPE = 3;
	public Accumulation(String month) {
		setMonth(month);
		setTypeName("住房");
		setChart("5101402");
		setReverseChar("2181100");
		setText("计提" + month + "月份住房公积金");
		setAssignment(month + "月公积金");
		setSecondAssignment(month + "月上海外服公积金");
		setType(TYPE);
	}
}
