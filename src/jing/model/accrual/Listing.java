package jing.model.accrual;

import java.math.BigDecimal;
import java.util.Date;

public class Listing {
	private String wbsNo; // wbs分配
	private String subject; // 科目
	private String pr; // 利润中心
	private String userName; // 用户名称
	private String billDate; // 过账日期
	private String voucherNo; // 凭证编号
	private String voucherType; // 凭证类型
	private Date voucherDate; // 凭证日期
	private String passBillNo; // 过账代码
	private BigDecimal amount; // 本币金额
	private String amountType; // 本币
	private String company; // 分公司（需要使用WBS表带出来）

	public Listing(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getAmountType() {
		return amountType;
	}

	public void setAmountType(String amountType) {
		this.amountType = amountType;
	}

	private String text; // 文本

	public String getWbsNo() {
		return wbsNo;
	}

	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBillDate() {
		return billDate;
	}

	public void setBillDate(String billDate) {
		this.billDate = billDate;
	}

	public String getVoucherNo() {
		return voucherNo;
	}

	public void setVoucherNo(String voucherNo) {
		this.voucherNo = voucherNo;
	}

	public String getVoucherType() {
		return voucherType;
	}

	public void setVoucherType(String voucherType) {
		this.voucherType = voucherType;
	}

	public Date getVoucherDate() {
		return voucherDate;
	}

	public void setVoucherDate(Date voucherDate) {
		this.voucherDate = voucherDate;
	}

	public String getPassBillNo() {
		return passBillNo;
	}

	public void setPassBillNo(String passBillNo) {
		this.passBillNo = passBillNo;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}
