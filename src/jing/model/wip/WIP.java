package jing.model.wip;

import java.math.BigDecimal;
import java.util.Date;

public class WIP {
	private String customerNo; // 客户号
	private String customerName; // 客户名称
	private String wbsNo; // WBS编号
	private String contractNo; // 合同号
	private String businessType; // 业务类型
	private String productLine; // 产品线
	private String pr; // 利润中心
	private String saleDepartment; // 销售办事处
	private String projectDesc; // 项目描述
	private BigDecimal DPMinusWIP; // DP - WIP
	private BigDecimal DPAmount; // DP(定金）合计
	private BigDecimal planTicket; // 计划开票VO
	private BigDecimal WIPAmount; // WIP合计
	private BigDecimal planTicketV9; // 计划开票V9
	private BigDecimal planWIP; // 计划WIP
	private BigDecimal importStuff; // 进口材料
	private BigDecimal internalStuff; // 国内材料
	private BigDecimal otherStuff; // 其他材料
	private BigDecimal importCustom; // 进口关税
	private BigDecimal oceanShippingFee; // 海运费
	private BigDecimal subPackageFee; // 分包费
	private BigDecimal rebuildSetupCost; // 改造安装成本
	private String contractCarryDate; // 合同承接日期
	private String orderDate; // 下达订单日期
	private String warehouseDate; // 入库成台日期
	private String lastBatchDate; // 最后一批日期
	private BigDecimal Equal0Month; // 0 months
	private BigDecimal lessThan6Month; // <6 months
	private BigDecimal between6To12Month; // 6 12 months
	private BigDecimal between12To24Month; // 12 24 months
	private BigDecimal between24To36Month; // 24 36 months
	private BigDecimal greaterThan36Month; // > 36 months
	private BigDecimal cheapenPrepare; // 跌价准备
	private BigDecimal v9BillingMinusWIP; // V9 Billing-WIP
	private BigDecimal actWIPMinusPlaWIP;	//act wip - pla wip

	public WIP(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getCustomerNo() {
		return customerNo;
	}

	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getWbsNo() {
		return wbsNo;
	}

	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getProductLine() {
		return productLine;
	}

	public void setProductLine(String productLine) {
		this.productLine = productLine;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

	public String getSaleDepartment() {
		return saleDepartment;
	}

	public void setSaleDepartment(String saleDepartment) {
		this.saleDepartment = saleDepartment;
	}

	public String getProjectDesc() {
		return projectDesc;
	}

	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
	}

	public BigDecimal getDPMinusWIP() {
		return DPMinusWIP;
	}

	public void setDPMinusWIP(BigDecimal dPMinusWIP) {
		DPMinusWIP = dPMinusWIP;
	}

	public BigDecimal getDPAmount() {
		return DPAmount;
	}

	public void setDPAmount(BigDecimal dPAmount) {
		DPAmount = dPAmount;
	}

	public BigDecimal getPlanTicket() {
		return planTicket;
	}

	public void setPlanTicket(BigDecimal planTicket) {
		this.planTicket = planTicket;
	}

	public BigDecimal getWIPAmount() {
		return WIPAmount;
	}

	public void setWIPAmount(BigDecimal wIPAmount) {
		WIPAmount = wIPAmount;
	}

	public BigDecimal getPlanTicketV9() {
		return planTicketV9;
	}

	public void setPlanTicketV9(BigDecimal planTicketV9) {
		this.planTicketV9 = planTicketV9;
	}

	public BigDecimal getPlanWIP() {
		return planWIP;
	}

	public void setPlanWIP(BigDecimal planWIP) {
		this.planWIP = planWIP;
	}

	public BigDecimal getImportStuff() {
		return importStuff;
	}

	public void setImportStuff(BigDecimal importStuff) {
		this.importStuff = importStuff;
	}

	public BigDecimal getInternalStuff() {
		return internalStuff;
	}

	public void setInternalStuff(BigDecimal internalStuff) {
		this.internalStuff = internalStuff;
	}

	public BigDecimal getOtherStuff() {
		return otherStuff;
	}

	public void setOtherStuff(BigDecimal otherStuff) {
		this.otherStuff = otherStuff;
	}

	public BigDecimal getImportCustom() {
		return importCustom;
	}

	public void setImportCustom(BigDecimal importCustom) {
		this.importCustom = importCustom;
	}

	public BigDecimal getOceanShippingFee() {
		return oceanShippingFee;
	}

	public void setOceanShippingFee(BigDecimal oceanShippingFee) {
		this.oceanShippingFee = oceanShippingFee;
	}

	public BigDecimal getSubPackageFee() {
		return subPackageFee;
	}

	public void setSubPackageFee(BigDecimal subPackageFee) {
		this.subPackageFee = subPackageFee;
	}

	public BigDecimal getRebuildSetupCost() {
		return rebuildSetupCost;
	}

	public void setRebuildSetupCost(BigDecimal rebuildSetupCost) {
		this.rebuildSetupCost = rebuildSetupCost;
	}

	public String getContractCarryDate() {
		return contractCarryDate;
	}

	public void setContractCarryDate(String contractCarryDate) {
		this.contractCarryDate = contractCarryDate;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getWarehouseDate() {
		return warehouseDate;
	}

	public void setWarehouseDate(String warehouseDate) {
		this.warehouseDate = warehouseDate;
	}

	public String getLastBatchDate() {
		return lastBatchDate;
	}

	public void setLastBatchDate(String lastBatchDate) {
		this.lastBatchDate = lastBatchDate;
	}

	public BigDecimal getEqual0Month() {
		return Equal0Month;
	}

	public void setEqual0Month(BigDecimal equal0Month) {
		Equal0Month = equal0Month;
	}

	public BigDecimal getLessThan6Month() {
		return lessThan6Month;
	}

	public void setLessThan6Month(BigDecimal lessThan6Month) {
		this.lessThan6Month = lessThan6Month;
	}

	public BigDecimal getBetween6To12Month() {
		return between6To12Month;
	}

	public void setBetween6To12Month(BigDecimal between6To12Month) {
		this.between6To12Month = between6To12Month;
	}

	public BigDecimal getBetween12To24Month() {
		return between12To24Month;
	}

	public void setBetween12To24Month(BigDecimal between12To24Month) {
		this.between12To24Month = between12To24Month;
	}

	public BigDecimal getBetween24To36Month() {
		return between24To36Month;
	}

	public void setBetween24To36Month(BigDecimal between24To36Month) {
		this.between24To36Month = between24To36Month;
	}

	public BigDecimal getGreaterThan36Month() {
		return greaterThan36Month;
	}

	public void setGreaterThan36Month(BigDecimal greaterThan36Month) {
		this.greaterThan36Month = greaterThan36Month;
	}

	public BigDecimal getCheapenPrepare() {
		return cheapenPrepare;
	}

	public void setCheapenPrepare(BigDecimal cheapenPrepare) {
		this.cheapenPrepare = cheapenPrepare;
	}

	public BigDecimal getV9BillingMinusWIP() {
		return v9BillingMinusWIP;
	}

	public void setV9BillingMinusWIP(BigDecimal v9BillingMinusWIP) {
		this.v9BillingMinusWIP = v9BillingMinusWIP;
	}

	public BigDecimal getActWIPMinusPlaWIP() {
		if(this.actWIPMinusPlaWIP == null){
			this.actWIPMinusPlaWIP = this.getWIPAmount().subtract(this.getPlanWIP());
		}
		return actWIPMinusPlaWIP;
	}

	public void setActWIPMinusPlaWIP(BigDecimal actWIPMinusPlaWIP) {
		this.actWIPMinusPlaWIP = actWIPMinusPlaWIP;
	}
}
