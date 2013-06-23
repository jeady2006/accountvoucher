package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class LabourUnion extends SalaryType {
	public static final int TYPE = 0;

	public LabourUnion(String month) {
		setMonth(month);
		setTypeName("����");
		setChart("5101801");
		setReverseChar("2181140");
		setText("����" + month + "�·ݹ��ᾭ��");
		setAssignment(month + "�¹��ᾭ��");
		setSecondAssignment(month + "�¹��ᾭ��");
		setType(TYPE);
	}
}
