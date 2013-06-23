package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class SHManageFee extends SalaryType {
	public static final int TYPE = 7;

	public SHManageFee(String month) {
		setMonth(month);
		setTypeName("�Ϻ���������");
		setChart("5300117");
		setReverseChar("2181902");
		setText("����" + month + "�·��Ϻ���������");
		setAssignment(month + "���Ϻ���������");
		setSecondAssignment(month + "���Ϻ���������");
		setType(TYPE);
	}
}
