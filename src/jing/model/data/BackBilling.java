package jing.model.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.TreeMap;

import jing.model.accrual.Listing;

public class BackBilling {
	public static final int TYPE_SETUP_COST_NORMAL = 0; // 只有"冲其他安装成本预提"
	public static final int TYPE_SETUP_COST_OTHER_COST = 1; // 有冲其他安装成本预提,后面指定冲的wbs号
	public static final int TYPE_SPLIT_BILLING_NORMAL = 10; // 只有"冲分包费预提"
	public static final int TYPE_SPLIT_OTHER_BILLING = 11; // 有"冲分包费预提",后台指定冲的wbs号
	public static final int TYPE_BACK_BILLING = 20; // 只有"追补"

	public static final String SPLIT_PACKAGE_COST_ELEMENT_NO = "4411010";
	private String year; // 年度
	private String voucherDate; // 凭证日期
	private String voucherNo; // 凭证编号
	private String passBillingDate; // 过帐日期
	private String referenceVoucherNo; // 参考凭证号
	private String wbsNo; // WBS元素
	private String costElement; // 成本要素
	private String costElementName; // 成本要素名称
	private BigDecimal ValCOArCur = new BigDecimal(0); // VCOArCur
	private BigDecimal splitRemaining = new BigDecimal(0); // 分包费预提余额
	private BigDecimal otherRemaining = new BigDecimal(0); // 其他成本预提余额
	private BigDecimal notEnoughRemaining = new BigDecimal(0); // 余额不足金额
	private String name; // 名称
	private String user; // 用户
	private String location; // 地区
	private String pr; // 利润中民
	private String projectName; // 项目名称
	private boolean isSplitPackage; // 是否分包
	private String handleText; // 如何处理

	// RuntimeData
	private String[] otherCostWbsNo; // 需要用其他wbs号冲预提
	private BigDecimal remainingAmount; // 冲预提后剩下的数
	private int type;
	private Listing referenceListing = null; // 引用的Listing对象
	private TreeMap<String, BackBilling> referenceWbsNoMap = null;
	private BigDecimal listAmount; // 生成追补时记录清单的金额

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
