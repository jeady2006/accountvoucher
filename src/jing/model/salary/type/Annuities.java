package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class Annuities extends SalaryType {
	public static final int TYPE = 1;

	public Annuities(String month) {
		setMonth(month);
		setTypeName("����");
		setChart("5101401");
		setReverseChar("2181050");
		setText("����" + month + "�����ϱ���");
		setAssignment(month + "������");
		setSecondAssignment(month + "���Ϻ��������");
		setType(TYPE);
	}
}
