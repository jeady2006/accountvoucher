package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class Hospitalization extends SalaryType {
	public static final int TYPE = 2;
	public Hospitalization(String month) {
		setMonth(month);
		setTypeName("医疗");
		setChart("5101403");
		setReverseChar("2181070");
		setText("计提" + month + "月份医疗保险");
		setAssignment(month + "月医疗");
		setSecondAssignment(month + "月上海外服医疗");
		setType(TYPE);
	}

}
