package jing.model.salary.type;

import jing.model.salary.SalaryType;


public class Procreate extends SalaryType {
	public static final int TYPE = 6;
	public Procreate(String month) {
		setMonth(month);
		setTypeName("生育");
		setChart("5101405");
		setReverseChar("2181902");
		setText("计提" + month + "月份生育保险");
		setAssignment(month + "月份生育");
		setSecondAssignment(month + "月份上海外服生育");
		setType(TYPE);
	}
}
