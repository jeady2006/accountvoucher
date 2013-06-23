package jing.model.salary;

import java.math.BigDecimal;

public class VOBonusDetail {
	public static final String NAME = "VO½±½ðÃ÷Ï¸";
	private String wbsNo;
	private String pr;
	private BigDecimal amount;

	public VOBonusDetail(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getWbsNo() {
		return wbsNo;
	}

	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
