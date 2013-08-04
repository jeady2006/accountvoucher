package jing.model.salary;

import java.math.BigDecimal;

public class Bonus {
	public static final String NAME = "内部费用往来明细及其他加款/奖金说明";
	
	private String chargeFrom;
	private String chargeTo;
	private String payrollMonth;
	private String item;
	private BigDecimal amount;
	private String remark;

	public String getChargeFrom() {
		return chargeFrom;
	}

	public void setChargeFrom(String chargeFrom) {
		this.chargeFrom = chargeFrom;
	}

	public String getPayrollMonth() {
		return payrollMonth;
	}

	public void setPayrollMonth(String payrollMonth) {
		this.payrollMonth = payrollMonth;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getChargeTo() {
		return chargeTo;
	}

	public void setChargeTo(String chargeTo) {
		this.chargeTo = chargeTo;
	}

}
