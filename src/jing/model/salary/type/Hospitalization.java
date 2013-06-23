package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class Hospitalization extends SalaryType {
	public static final int TYPE = 2;
	public Hospitalization(String month) {
		setMonth(month);
		setTypeName("ҽ��");
		setChart("5101403");
		setReverseChar("2181070");
		setText("����" + month + "�·�ҽ�Ʊ���");
		setAssignment(month + "��ҽ��");
		setSecondAssignment(month + "���Ϻ����ҽ��");
		setType(TYPE);
	}

}
