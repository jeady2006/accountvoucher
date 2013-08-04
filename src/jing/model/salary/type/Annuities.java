package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class Annuities extends SalaryType {
	public static final int TYPE = 1;

	public Annuities(String month) {
		setMonth(month);
		setTypeName("养老");
		setChart("5101401");
		setReverseChar("2181050");
		setText("计提" + month + "月养老保险");
		setAssignment(month + "月养老");
		setSecondAssignment(month + "月上海外服养老");
		setType(TYPE);
	}
}
