package jing.model.salary.type;

import jing.model.salary.SalaryType;


public class Procreate extends SalaryType {
	public static final int TYPE = 6;
	public Procreate(String month) {
		setMonth(month);
		setTypeName("����");
		setChart("5101405");
		setReverseChar("2181902");
		setText("����" + month + "�·���������");
		setAssignment(month + "�·�����");
		setSecondAssignment(month + "�·��Ϻ��������");
		setType(TYPE);
	}
}
