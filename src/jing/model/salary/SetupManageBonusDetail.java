package jing.model.salary;

import java.math.BigDecimal;

public class SetupManageBonusDetail {
	public static final String NAME = "安装管理奖金明细";

	private String code;
	private String pr;
	private BigDecimal amount;

	public SetupManageBonusDetail(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

}
