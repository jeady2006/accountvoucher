package jing.salary.service;



public interface SalaryGenerator{
	public static final String COMPANY_CODE = "3910";
	
	void load(String wbsFile, String file);
	String generate(String pr, int month, String postingDate);
}
