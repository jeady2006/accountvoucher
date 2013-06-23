package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class Accumulation extends SalaryType {
	public static final int TYPE = 3;
	public Accumulation(String month) {
		setMonth(month);
		setTypeName("ס��");
		setChart("5101402");
		setReverseChar("2181100");
		setText("����" + month + "�·�ס��������");
		setAssignment(month + "�¹�����");
		setSecondAssignment(month + "���Ϻ����������");
		setType(TYPE);
	}
}
