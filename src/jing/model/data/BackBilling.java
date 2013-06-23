package jing.model.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.TreeMap;

import jing.model.accrual.Listing;

public class BackBilling {
	public static final int TYPE_SETUP_COST_NORMAL = 0; // ֻ��"��������װ�ɱ�Ԥ��"
	public static final int TYPE_SETUP_COST_OTHER_COST = 1; // �г�������װ�ɱ�Ԥ��,����ָ�����wbs��
	public static final int TYPE_SPLIT_BILLING_NORMAL = 10; // ֻ��"��ְ���Ԥ��"
	public static final int TYPE_SPLIT_OTHER_BILLING = 11; // ��"��ְ���Ԥ��",��ָ̨�����wbs��
	public static final int TYPE_BACK_BILLING = 20; // ֻ��"׷��"

	public static final String SPLIT_PACKAGE_COST_ELEMENT_NO = "4411010";
	private String year; // ���
	private String voucherDate; // ƾ֤����
	private String voucherNo; // ƾ֤���
	private String passBillingDate; // ��������
	private String referenceVoucherNo; // �ο�ƾ֤��
	private String wbsNo; // WBSԪ��
	private String costElement; // �ɱ�Ҫ��
	private String costElementName; // �ɱ�Ҫ������
	private BigDecimal ValCOArCur = new BigDecimal(0); // VCOArCur
	private BigDecimal splitRemaining = new BigDecimal(0); // �ְ���Ԥ�����
	private BigDecimal otherRemaining = new BigDecimal(0); // �����ɱ�Ԥ�����
	private BigDecimal notEnoughRemaining = new BigDecimal(0); // ������
	private String name; // ����
	private String user; // �û�
	private String location; // ����
	private String pr; // ��������
	private String projectName; // ��Ŀ����
	private boolean isSplitPackage; // �Ƿ�ְ�
	private String handleText; // ��δ���

	// RuntimeData
	private String[] otherCostWbsNo; // ��Ҫ������wbs�ų�Ԥ��
	private BigDecimal remainingAmount; // ��Ԥ���ʣ�µ���
	private int type;
	private Listing referenceListing = null; // ���õ�Listing����
	private TreeMap<String, BackBilling> referenceWbsNoMap = null;
	private BigDecimal listAmount; // ����׷��ʱ��¼�嵥�Ľ��

	public BackBilling(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getVoucherDate() {
		return voucherDate;
	}

	public void setVoucherDate(String voucherDate) {
		this.voucherDate = voucherDate;
	}

	public String getVoucherNo() {
		return voucherNo;
	}

	public void setVoucherNo(String voucherNo) {
		this.voucherNo = voucherNo;
	}

	public String getPassBillingDate() {
		return passBillingDate;
	}

	public void setPassBillingDate(String passBillingDate) {
		this.passBillingDate = passBillingDate;
	}

	public String getReferenceVoucherNo() {
		return referenceVoucherNo;
	}

	public void setReferenceVoucherNo(String referenceVoucherNo) {
		this.referenceVoucherNo = referenceVoucherNo;
	}

	public String getWbsNo() {
		return wbsNo;
	}

	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getCostElement() {
		return costElement;
	}

	public void setCostElement(String costElement) {
		this.costElement = costElement;
	}

	public String getCostElementName() {
		return costElementName;
	}

	public void setCostElementName(String costElementName) {
		this.costElementName = costElementName;
	}

	public BigDecimal getValCOArCur() {
		return ValCOArCur;
	}

	public void setValCOArCur(BigDecimal valCOArCur) {
		ValCOArCur = valCOArCur;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

	public boolean isSplitPackage() {
		if (this.costElement != null) {
			return this.costElement.equals(SPLIT_PACKAGE_COST_ELEMENT_NO);
		} else {
			return this.isSplitPackage;
		}
	}

	public void setSplitPackage(boolean isSplitPackage) {
		this.isSplitPackage = isSplitPackage;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getHandleText() {
		return handleText;
	}

	public void setHandleText(String handleText) {
		this.handleText = handleText;
	}

	public BigDecimal getSplitRemaining() {
		return splitRemaining;
	}

	public void setSplitRemaining(BigDecimal splitRemaining) {
		this.splitRemaining = splitRemaining;
	}

	public BigDecimal getOtherRemaining() {
		return otherRemaining;
	}

	public void setOtherRemaining(BigDecimal otherRemaining) {
		this.otherRemaining = otherRemaining;
	}

	public BigDecimal getNotEnoughRemaining() {
		return notEnoughRemaining;
	}

	public void setNotEnoughRemaining(BigDecimal notEnoughRemaining) {
		this.notEnoughRemaining = notEnoughRemaining;
	}

	public String[] getOtherCostWbsNo() {
		return otherCostWbsNo;
	}

	public void setOtherCostWbsNo(String[] otherCostWbsNo) {
		this.otherCostWbsNo = otherCostWbsNo;
	}

	public BigDecimal getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(BigDecimal remainingAmount) {
		this.remainingAmount = remainingAmount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Listing getReferenceListing() {
		return referenceListing;
	}

	public void setReferenceListing(Listing referenceListing) {
		this.referenceListing = referenceListing;
	}

	public TreeMap<String, BackBilling> getReferenceWbsNoMap() {
		return referenceWbsNoMap;
	}

	public void setReferenceWbsNoMap(
			TreeMap<String, BackBilling> referenceWbsNoMap) {
		this.referenceWbsNoMap = referenceWbsNoMap;
	}

	public BigDecimal getListAmount() {
		return listAmount;
	}

	public void setListAmount(BigDecimal listAmount) {
		this.listAmount = listAmount;
	}

}
