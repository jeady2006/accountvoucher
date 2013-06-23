package jing.model.salary;

import java.math.BigDecimal;

public class SaleBonusDetail {
	public static final String NAME = "销售奖金明细";
	private String contractNum;
	private BigDecimal sum;
	private String pr;

	public SaleBonusDetail(String contractNum) {
		this.contractNum = contractNum;
	}

	public String getContractNum() {
		return contractNum;
	}

	public void setContractNum(String contractNum) {
		this.contractNum = contractNum;
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
