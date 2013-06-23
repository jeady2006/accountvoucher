package jing.model.wip;

import java.math.BigDecimal;

public class WIPSetup {
	private String customerNo; // �ͻ���
	private String customerName; // �ͻ�����
	private String wbsNo; // WBS���
	private String contractNo; // ��ͬ��
	private String businessType; // ҵ������
	private String productLine; // ��Ʒ��
	private String projectDesc; // ��Ŀ����
	private String pr; // ��������
	private String saleDepartment; // ���۰��´�
	private BigDecimal DPMinusWIP; // DP - WIP
	private BigDecimal DPAmount; // DP(���𣩺ϼ�
	private BigDecimal planTicket; // �ƻ���ƱVO
	private BigDecimal planTicketV9; // �ƻ���ƱV9
	private BigDecimal planWIP; // �ƻ�WIP
	private BigDecimal WIPAmount; // WIP�ϼ�
	private BigDecimal componentSetupSalary; // ������װ����
	private BigDecimal componentSetupTripFee; // ������װ���÷�
	private BigDecimal projectManageSalaryCost; // ��Ŀ�����ʳɱ�
	private BigDecimal projectManageTripCost; // ��Ŀ������óɱ�
	private BigDecimal debugSalaryCost; // ���Թ��ʳɱ�
	private BigDecimal debugTripCost; // ���Բ��óɱ�
	private BigDecimal checkSalaryCost; // ���鹤�ʳɱ�
	private BigDecimal checkTripCost; // ������óɱ�
	private BigDecimal checkFee; // �����
	private BigDecimal splitFee; // �ְ���
	private BigDecimal unsureCost; // �Ᵽ�ɱ�
	private BigDecimal innerSettleCost; // �ڲ�����ɱ�
	private BigDecimal otherCost; // �����ɱ�
	private String setupPreCollectDate; // ��װԤ�ն�������
	private String componentSetupStartDate; // ������װ��ʼ����
	private String innerCheckEndDate; // �ڼ��������
	private String deviceTicketDate; // �豸��Ʊ����
	private String govAcceptDate; // ������������
	private BigDecimal Equal0Month; // 0 months
	private BigDecimal lessThan6Month; // <6 months
	private BigDecimal between6To12Month; // 6 12 months
	private BigDecimal between12To24Month; // 12 24 months
	private BigDecimal between24To36Month; // 24 36 months
	private BigDecimal greaterThan36Month; // > 36 months
	private BigDecimal cheapenPrepare; // ����׼��
	private BigDecimal v9BillingMinusWIP; // V9 Billing-WIP
	
	public WIPSetup(String wbsNo){
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

	public String getProjectDesc() {
		return projectDesc;
	}

	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
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

	public BigDecimal getWIPAmount() {
		return WIPAmount;
	}

	public void setWIPAmount(BigDecimal wIPAmount) {
		WIPAmount = wIPAmount;
	}

	public BigDecimal getComponentSetupSalary() {
		return componentSetupSalary;
	}

	public void setComponentSetupSalary(BigDecimal componentSetupSalary) {
		this.componentSetupSalary = componentSetupSalary;
	}

	public BigDecimal getComponentSetupTripFee() {
		return componentSetupTripFee;
	}

	public void setComponentSetupTripFee(BigDecimal componentSetupTripFee) {
		this.componentSetupTripFee = componentSetupTripFee;
	}

	public BigDecimal getProjectManageSalaryCost() {
		return projectManageSalaryCost;
	}

	public void setProjectManageSalaryCost(BigDecimal projectManageSalaryCost) {
		this.projectManageSalaryCost = projectManageSalaryCost;
	}

	public BigDecimal getProjectManageTripCost() {
		return projectManageTripCost;
	}

	public void setProjectManageTripCost(BigDecimal projectManageTripCost) {
		this.projectManageTripCost = projectManageTripCost;
	}

	public BigDecimal getDebugSalaryCost() {
		return debugSalaryCost;
	}

	public void setDebugSalaryCost(BigDecimal debugSalaryCost) {
		this.debugSalaryCost = debugSalaryCost;
	}

	public BigDecimal getDebugTripCost() {
		return debugTripCost;
	}

	public void setDebugTripCost(BigDecimal debugTripCost) {
		this.debugTripCost = debugTripCost;
	}

	public BigDecimal getCheckSalaryCost() {
		return checkSalaryCost;
	}

	public void setCheckSalaryCost(BigDecimal checkSalaryCost) {
		this.checkSalaryCost = checkSalaryCost;
	}

	public BigDecimal getCheckTripCost() {
		return checkTripCost;
	}

	public void setCheckTripCost(BigDecimal checkTripCost) {
		this.checkTripCost = checkTripCost;
	}

	public BigDecimal getCheckFee() {
		return checkFee;
	}

	public void setCheckFee(BigDecimal checkFee) {
		this.checkFee = checkFee;
	}

	public BigDecimal getSplitFee() {
		return splitFee;
	}

	public void setSplitFee(BigDecimal splitFee) {
		this.splitFee = splitFee;
	}

	public BigDecimal getUnsureCost() {
		return unsureCost;
	}

	public void setUnsureCost(BigDecimal unsureCost) {
		this.unsureCost = unsureCost;
	}

	public BigDecimal getInnerSettleCost() {
		return innerSettleCost;
	}

	public void setInnerSettleCost(BigDecimal innerSettleCost) {
		this.innerSettleCost = innerSettleCost;
	}

	public BigDecimal getOtherCost() {
		return otherCost;
	}

	public void setOtherCost(BigDecimal otherCost) {
		this.otherCost = otherCost;
	}

	public String getSetupPreCollectDate() {
		return setupPreCollectDate;
	}

	public void setSetupPreCollectDate(String setupPreCollectDate) {
		this.setupPreCollectDate = setupPreCollectDate;
	}

	public String getComponentSetupStartDate() {
		return componentSetupStartDate;
	}

	public void setComponentSetupStartDate(String componentSetupStartDate) {
		this.componentSetupStartDate = componentSetupStartDate;
	}

	public String getInnerCheckEndDate() {
		return innerCheckEndDate;
	}

	public void setInnerCheckEndDate(String innerCheckEndDate) {
		this.innerCheckEndDate = innerCheckEndDate;
	}

	public String getDeviceTicketDate() {
		return deviceTicketDate;
	}

	public void setDeviceTicketDate(String deviceTicketDate) {
		this.deviceTicketDate = deviceTicketDate;
	}

	public String getGovAcceptDate() {
		return govAcceptDate;
	}

	public void setGovAcceptDate(String govAcceptDate) {
		this.govAcceptDate = govAcceptDate;
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

}
