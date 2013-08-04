package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class SHManageFee extends SalaryType {
	public static final int TYPE = 7;

	public SHManageFee(String month) {
		setMonth(month);
		setTypeName("上海外服管理费");
		setChart("5300117");
		setReverseChar("2181902");
		setText("计提" + month + "月份上海外服管理费");
		setAssignment(month + "月上海外服管理费");
		setSecondAssignment(month + "月上海外服管理费");
		setType(TYPE);
	}
}
