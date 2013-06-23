package jing.model.salary.type;

import jing.model.salary.SalaryType;


public class WorkBreak extends SalaryType {
	public static final int TYPE = 5;
	public WorkBreak(String month) {
		setMonth(month);
		setTypeName("����");
		setChart("5101501");
		setReverseChar("2181110");
		setText("����" + month + "�·ݹ��˱���");
		setAssignment(month + "�¹���");
		setSecondAssignment(month + "���Ϻ��������");
		setType(TYPE);
	}
}
