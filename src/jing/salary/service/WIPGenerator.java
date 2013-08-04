package jing.salary.service;

public interface WIPGenerator {
	void loadDevice(String wip3910File, String wip3940File);
	void generateDevice(String month);
	
	void loadSetup(String wip3910File, String wip3940File);
	void generateSetup(String month);
}
