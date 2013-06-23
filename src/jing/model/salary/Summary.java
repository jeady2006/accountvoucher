package jing.model.salary;

import java.math.BigDecimal;

public class Summary {
	public static final String NAME = "Summary";
	
	private String costCenter; // 成本中心
	private String pr; // 利润中心
	private BigDecimal baseSalary; // 基薪
	private BigDecimal baseSalarytotal; // 基薪总和
	private BigDecimal lunchAllowance; // 午餐津贴
	private BigDecimal synthesisAllowance; // 综合津贴
	private BigDecimal liveAllowance; // 现场津贴
	private BigDecimal fixBonus; // 修理奖金
	private BigDecimal rebuildBonus; // 改造奖金
	private BigDecimal maintenanceBonus; // 维保销售奖金
	private BigDecimal saveRecallBonus; // 安全回召奖金
	private BigDecimal maintenanceEfficiencyBonus; // 维保效率奖金
	private BigDecimal setupManagementBonus; // 安装管理奖金
	private BigDecimal engineeringBonus; // 工程绩效考核奖
	private BigDecimal salesBonus; // 销售奖金
	private BigDecimal accountReceivable; // 应收帐款
	private BigDecimal translateBonus; // 翻译奖金
	private BigDecimal otherBonus; // 其他奖金
	private BigDecimal yearHolidayBonus; // 年假折薪
	private BigDecimal trafficAllowance; // 交通津贴
	private BigDecimal houseAllowance; // 住房津贴
	private BigDecimal appointAllowance; // 外派津贴
	private BigDecimal heatAllowance; // 高温、采暖费
	private BigDecimal goodEmployeeHortation; // 优秀员工奖励
	private BigDecimal aimBonus; // 目标奖金
	private BigDecimal performanceBonus; // 绩效奖金
	private BigDecimal accountDutyBonus; // 奖金一次性房计税
	private BigDecimal FTTTranceAllowance; // FTT培训津贴
	private BigDecimal finacialCompensate; // 经济补偿金
	private BigDecimal peopleSaveBonus; // 人才保留金
	private BigDecimal otherAllowance; // 其他加款_税前
	private BigDecimal otherAfterAddFare; // 其它加款—-税后
	private BigDecimal otherBeforeMinusFare; // 其它扣款——税前
	private BigDecimal otherAfterMinusFare; // 其他扣款——税后
	private BigDecimal allowanceTotal; // 津贴总额
	private BigDecimal middleNightAllowance; // 中夜班
	private BigDecimal otAllowance; // 加班费
	private BigDecimal otTotal; // 加班总额
	private BigDecimal accumulationFund; // 公积金/公提
	private BigDecimal annuitiesFund; // 养老金/公提
	private BigDecimal hospitalizationInsurance; // 医疗保险
	private BigDecimal lostWorkFund; // 失业保险
	private BigDecimal procreateFund; // 生育
	private BigDecimal workBreakFund; // 工伤
	private BigDecimal selfAccumulationFund; // 自提公积金
	private BigDecimal selfAnnuitiesFund; // 自提养老金
	private BigDecimal selfHospitalizationInsurance; // 自提医疗金
	private BigDecimal selfIdlenessFund; // 自提失业金
	private BigDecimal labourUnionFund; // 工会费
	private BigDecimal birthdayFund; // 生日礼金
	private BigDecimal personOwnDuty; // 个人所得税
	private BigDecimal bonusDuty; // 奖金税
	private BigDecimal finacialCompensateDuty; // 经济补偿金所得税
	private BigDecimal OriSalary; // 当月应发工资
	private BigDecimal FactSalary; // 当月实发工资
	private BigDecimal overheadExpenses; // 管理费用
	private BigDecimal bf;
	private BigDecimal bg;
	private BigDecimal labourUnionBase; // 工会基数
	private BigDecimal labourUnionBaseFund; // 工会费
	private BigDecimal salary; // 工资

	public Summary(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

	public BigDecimal getBaseSalary() {
		return baseSalary;
	}

	public void setBaseSalary(BigDecimal baseSalary) {
		this.baseSalary = baseSalary;
	}

	public BigDecimal getBaseSalarytotal() {
		return baseSalarytotal;
	}

	public void setBaseSalarytotal(BigDecimal baseSalarytotal) {
		this.baseSalarytotal = baseSalarytotal;
	}

	public BigDecimal getLunchAllowance() {
		return lunchAllowance;
	}

	public void setLunchAllowance(BigDecimal lunchAllowance) {
		this.lunchAllowance = lunchAllowance;
	}

	public BigDecimal getSynthesisAllowance() {
		return synthesisAllowance;
	}

	public void setSynthesisAllowance(BigDecimal synthesisAllowance) {
		this.synthesisAllowance = synthesisAllowance;
	}

	public BigDecimal getLiveAllowance() {
		return liveAllowance;
	}

	public void setLiveAllowance(BigDecimal liveAllowance) {
		this.liveAllowance = liveAllowance;
	}

	public BigDecimal getFixBonus() {
		return fixBonus;
	}

	public void setFixBonus(BigDecimal fixBonus) {
		this.fixBonus = fixBonus;
	}

	public BigDecimal getRebuildBonus() {
		return rebuildBonus;
	}

	public void setRebuildBonus(BigDecimal rebuildBonus) {
		this.rebuildBonus = rebuildBonus;
	}

	public BigDecimal getMaintenanceBonus() {
		return maintenanceBonus;
	}

	public void setMaintenanceBonus(BigDecimal maintenanceBonus) {
		this.maintenanceBonus = maintenanceBonus;
	}

	public BigDecimal getSaveRecallBonus() {
		return saveRecallBonus;
	}

	public void setSaveRecallBonus(BigDecimal saveRecallBonus) {
		this.saveRecallBonus = saveRecallBonus;
	}

	public BigDecimal getMaintenanceEfficiencyBonus() {
		return maintenanceEfficiencyBonus;
	}

	public void setMaintenanceEfficiencyBonus(
			BigDecimal maintenanceEfficiencyBonus) {
		this.maintenanceEfficiencyBonus = maintenanceEfficiencyBonus;
	}

	public BigDecimal getSetupManagementBonus() {
		return setupManagementBonus;
	}

	public void setSetupManagementBonus(BigDecimal setupManagementBonus) {
		this.setupManagementBonus = setupManagementBonus;
	}

	public BigDecimal getEngineeringBonus() {
		return engineeringBonus;
	}

	public void setEngineeringBonus(BigDecimal engineeringBonus) {
		this.engineeringBonus = engineeringBonus;
	}

	public BigDecimal getSalesBonus() {
		return salesBonus;
	}

	public void setSalesBonus(BigDecimal salesBonus) {
		this.salesBonus = salesBonus;
	}

	public BigDecimal getAccountReceivable() {
		return accountReceivable;
	}

	public void setAccountReceivable(BigDecimal accountReceivable) {
		this.accountReceivable = accountReceivable;
	}

	public BigDecimal getTranslateBonus() {
		return translateBonus;
	}

	public void setTranslateBonus(BigDecimal translateBonus) {
		this.translateBonus = translateBonus;
	}

	public BigDecimal getOtherBonus() {
		return otherBonus;
	}

	public void setOtherBonus(BigDecimal otherBonus) {
		this.otherBonus = otherBonus;
	}

	public BigDecimal getYearHolidayBonus() {
		return yearHolidayBonus;
	}

	public void setYearHolidayBonus(BigDecimal yearHolidayBonus) {
		this.yearHolidayBonus = yearHolidayBonus;
	}

	public BigDecimal getTrafficAllowance() {
		return trafficAllowance;
	}

	public void setTrafficAllowance(BigDecimal trafficAllowance) {
		this.trafficAllowance = trafficAllowance;
	}

	public BigDecimal getHouseAllowance() {
		return houseAllowance;
	}

	public void setHouseAllowance(BigDecimal houseAllowance) {
		this.houseAllowance = houseAllowance;
	}

	public BigDecimal getAppointAllowance() {
		return appointAllowance;
	}

	public void setAppointAllowance(BigDecimal appointAllowance) {
		this.appointAllowance = appointAllowance;
	}

	public BigDecimal getHeatAllowance() {
		return heatAllowance;
	}

	public void setHeatAllowance(BigDecimal heatAllowance) {
		this.heatAllowance = heatAllowance;
	}

	public BigDecimal getGoodEmployeeHortation() {
		return goodEmployeeHortation;
	}

	public void setGoodEmployeeHortation(BigDecimal goodEmployeeHortation) {
		this.goodEmployeeHortation = goodEmployeeHortation;
	}

	public BigDecimal getAimBonus() {
		return aimBonus;
	}

	public void setAimBonus(BigDecimal aimBonus) {
		this.aimBonus = aimBonus;
	}

	public BigDecimal getPerformanceBonus() {
		return performanceBonus;
	}

	public void setPerformanceBonus(BigDecimal performanceBonus) {
		this.performanceBonus = performanceBonus;
	}

	public BigDecimal getAccountDutyBonus() {
		return accountDutyBonus;
	}

	public void setAccountDutyBonus(BigDecimal accountDutyBonus) {
		this.accountDutyBonus = accountDutyBonus;
	}

	public BigDecimal getFTTTranceAllowance() {
		return FTTTranceAllowance;
	}

	public void setFTTTranceAllowance(BigDecimal fTTTranceAllowance) {
		FTTTranceAllowance = fTTTranceAllowance;
	}

	public BigDecimal getFinacialCompensate() {
		return finacialCompensate;
	}

	public void setFinacialCompensate(BigDecimal finacialCompensate) {
		this.finacialCompensate = finacialCompensate;
	}

	public BigDecimal getPeopleSaveBonus() {
		return peopleSaveBonus;
	}

	public void setPeopleSaveBonus(BigDecimal peopleSaveBonus) {
		this.peopleSaveBonus = peopleSaveBonus;
	}

	public BigDecimal getOtherAfterAddFare() {
		return otherAfterAddFare;
	}

	public void setOtherAfterAddFare(BigDecimal otherAfterAddFare) {
		this.otherAfterAddFare = otherAfterAddFare;
	}

	public BigDecimal getOtherBeforeMinusFare() {
		return otherBeforeMinusFare;
	}

	public void setOtherBeforeMinusFare(BigDecimal otherBeforeMinusFare) {
		this.otherBeforeMinusFare = otherBeforeMinusFare;
	}

	public BigDecimal getOtherAfterMinusFare() {
		return otherAfterMinusFare;
	}

	public void setOtherAfterMinusFare(BigDecimal otherAfterMinusFare) {
		this.otherAfterMinusFare = otherAfterMinusFare;
	}

	public BigDecimal getOtherAllowance() {
		return otherAllowance;
	}

	public void setOtherAllowance(BigDecimal otherAllowance) {
		this.otherAllowance = otherAllowance;
	}

	public BigDecimal getAllowanceTotal() {
		return allowanceTotal;
	}

	public void setAllowanceTotal(BigDecimal allowanceTotal) {
		this.allowanceTotal = allowanceTotal;
	}

	public BigDecimal getMiddleNightAllowance() {
		return middleNightAllowance;
	}

	public void setMiddleNightAllowance(BigDecimal middleNightAllowance) {
		this.middleNightAllowance = middleNightAllowance;
	}

	public BigDecimal getOtAllowance() {
		return otAllowance;
	}

	public void setOtAllowance(BigDecimal otAllowance) {
		this.otAllowance = otAllowance;
	}

	public BigDecimal getOtTotal() {
		return otTotal;
	}

	public void setOtTotal(BigDecimal otTotal) {
		this.otTotal = otTotal;
	}

	public BigDecimal getAccumulationFund() {
		return accumulationFund;
	}

	public void setAccumulationFund(BigDecimal accumulationFund) {
		this.accumulationFund = accumulationFund;
	}

	public BigDecimal getAnnuitiesFund() {
		return annuitiesFund;
	}

	public void setAnnuitiesFund(BigDecimal annuitiesFund) {
		this.annuitiesFund = annuitiesFund;
	}

	public BigDecimal getHospitalizationInsurance() {
		return hospitalizationInsurance;
	}

	public void setHospitalizationInsurance(BigDecimal hospitalizationInsurance) {
		this.hospitalizationInsurance = hospitalizationInsurance;
	}

	public BigDecimal getProcreateFund() {
		return procreateFund;
	}

	public void setProcreateFund(BigDecimal procreateFund) {
		this.procreateFund = procreateFund;
	}

	public BigDecimal getWorkBreakFund() {
		return workBreakFund;
	}

	public void setWorkBreakFund(BigDecimal workBreakFund) {
		this.workBreakFund = workBreakFund;
	}

	public BigDecimal getSelfAccumulationFund() {
		return selfAccumulationFund;
	}

	public void setSelfAccumulationFund(BigDecimal selfAccumulationFund) {
		this.selfAccumulationFund = selfAccumulationFund;
	}

	public BigDecimal getSelfAnnuitiesFund() {
		return selfAnnuitiesFund;
	}

	public void setSelfAnnuitiesFund(BigDecimal selfAnnuitiesFund) {
		this.selfAnnuitiesFund = selfAnnuitiesFund;
	}

	public BigDecimal getSelfHospitalizationInsurance() {
		return selfHospitalizationInsurance;
	}

	public void setSelfHospitalizationInsurance(
			BigDecimal selfHospitalizationInsurance) {
		this.selfHospitalizationInsurance = selfHospitalizationInsurance;
	}

	public BigDecimal getSelfIdlenessFund() {
		return selfIdlenessFund;
	}

	public void setSelfIdlenessFund(BigDecimal selfIdlenessFund) {
		this.selfIdlenessFund = selfIdlenessFund;
	}

	public BigDecimal getLabourUnionFund() {
		return labourUnionFund;
	}

	public void setLabourUnionFund(BigDecimal labourUnionFund) {
		this.labourUnionFund = labourUnionFund;
	}

	public BigDecimal getBirthdayFund() {
		return birthdayFund;
	}

	public void setBirthdayFund(BigDecimal birthdayFund) {
		this.birthdayFund = birthdayFund;
	}

	public BigDecimal getPersonOwnDuty() {
		return personOwnDuty;
	}

	public void setPersonOwnDuty(BigDecimal personOwnDuty) {
		this.personOwnDuty = personOwnDuty;
	}

	public BigDecimal getBonusDuty() {
		return bonusDuty;
	}

	public void setBonusDuty(BigDecimal bonusDuty) {
		this.bonusDuty = bonusDuty;
	}

	public BigDecimal getFinacialCompensateDuty() {
		return finacialCompensateDuty;
	}

	public void setFinacialCompensateDuty(BigDecimal finacialCompensateDuty) {
		this.finacialCompensateDuty = finacialCompensateDuty;
	}

	public BigDecimal getOriSalary() {
		return OriSalary;
	}

	public void setOriSalary(BigDecimal oriSalary) {
		OriSalary = oriSalary;
	}

	public BigDecimal getFactSalary() {
		return FactSalary;
	}

	public void setFactSalary(BigDecimal factSalary) {
		FactSalary = factSalary;
	}

	public BigDecimal getOverheadExpenses() {
		return overheadExpenses;
	}

	public void setOverheadExpenses(BigDecimal overheadExpenses) {
		this.overheadExpenses = overheadExpenses;
	}

	public BigDecimal getBf() {
		return bf;
	}

	public void setBf(BigDecimal bf) {
		this.bf = bf;
	}

	public BigDecimal getBg() {
		return bg;
	}

	public void setBg(BigDecimal bg) {
		this.bg = bg;
	}

	public BigDecimal getLabourUnionBase() {
		// 当月应发薪资-加班费
		if (labourUnionBase == null) {
			labourUnionBase = this.OriSalary.subtract(otAllowance);
		}
		return labourUnionBase;
	}

	public void setLabourUnionBase(BigDecimal labourUnionBase) {
		this.labourUnionBase = labourUnionBase;
	}

	public BigDecimal getLabourUnionBaseFund() {
		if (labourUnionBaseFund == null) {
			labourUnionBaseFund = this.getLabourUnionBase().multiply(
					new BigDecimal(0.02));
		}
		return labourUnionBaseFund;
	}

	public void setLabourUnionBaseFund(BigDecimal labourUnionBaseFund) {
		this.labourUnionBaseFund = labourUnionBaseFund;
	}

	public BigDecimal getSalary() {
		if (salary == null) {
			BigDecimal d = fixBonus.add(rebuildBonus).add(maintenanceBonus)
					.add(saveRecallBonus).add(maintenanceEfficiencyBonus)
					.add(setupManagementBonus).add(engineeringBonus)
					.add(salesBonus).add(accountReceivable).add(translateBonus)
					.add(otherBonus);
			salary = labourUnionBase.subtract(d).subtract(heatAllowance);
		}
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public BigDecimal getLostWorkFund() {
		return lostWorkFund;
	}

	public void setLostWorkFund(BigDecimal lostWorkFund) {
		this.lostWorkFund = lostWorkFund;
	}

}
