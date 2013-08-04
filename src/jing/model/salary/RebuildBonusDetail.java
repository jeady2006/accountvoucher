package jing.model.salary;

import java.math.BigDecimal;

public class RebuildBonusDetail {
	public static final String NAME = "改造奖金明细";

	private String wbsNumber;
	private String pr;
	private BigDecimal sum;

	public RebuildBonusDetail(String wbsNumber) {
		this.wbsNumber = wbsNumber;
	}

	public String getWbsNumber() {
		return wbsNumber;
	}

	public void setWbsNumber(String wbsNumber) {
		this.wbsNumber = wbsNumber;
	}

	public BigDecimal getSum() {
		return sum;
	}

	public void setSum(BigDecimal sum) {
		this.sum = sum;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

}
