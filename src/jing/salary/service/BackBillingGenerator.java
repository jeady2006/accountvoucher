package jing.salary.service;

public interface BackBillingGenerator {
	void loadBackBilling(String wbsFile, String list2192041File,
			String list2193000File, String backBillingListFile);

	String generateBackBilling(String month);

	void loadReply(String replyFile, String list2192041FileName,
			String list2193000FileName);

	void generateReplyVoucher(String month, String postingDate);
}
