package jing.salary.service.listener;

public interface BackBillingReplyListener extends GeneratorListener {
	void backBillingStart();
	void backBillingFinish();
}
