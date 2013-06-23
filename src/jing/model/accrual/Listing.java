package jing.model.accrual;

import java.math.BigDecimal;
import java.util.Date;

public class Listing {
	private String wbsNo; // wbs����
	private String subject; // ��Ŀ
	private String pr; // ��������
	private String userName; // �û�����
	private String billDate; // ��������
	private String voucherNo; // ƾ֤���
	private String voucherType; // ƾ֤����
	private Date voucherDate; // ƾ֤����
	private String passBillNo; // ���˴���
	private BigDecimal amount; // ���ҽ��
	private String amountType; // ����
	private String company; // �ֹ�˾����Ҫʹ��WBS���������

	public Listing(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getAmountType() {
		return amountType;
	}

	public void setAmountType(String amountType) {
		this.amountType = amountType;
	}

	private String text; // �ı�

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
