package jing.model.salary.type;

import jing.model.salary.SalaryType;

public class LostWork extends SalaryType {
	public static final int TYPE = 4;
	public LostWork(String month) {
		setMonth(month);
		setTypeName("ʧҵ");
		setChart("5101404");
		setReverseChar("2181090");
		setText("����" + month + "��ʧҵ");
		setAssignment(month + "��ʧҵ");
		setSecondAssignment(month + "���Ϻ����ʧҵ");
		setType(TYPE);
	}
}
