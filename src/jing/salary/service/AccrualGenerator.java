package jing.salary.service;

import java.util.HashMap;


public interface AccrualGenerator{
	public static final HashMap<String, String> companyMapper = new HashMap<String, String>();
	
	void load(String wbsFile, String listingFile);
	String generate(String month);
}
