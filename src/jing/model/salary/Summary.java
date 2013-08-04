package jing.model.salary;

import java.math.BigDecimal;

public class Summary {
	public static final String NAME = "Summary";
	
	private String costCenter; // �ɱ�����
	private String pr; // ��������
	private BigDecimal baseSalary; // ��н
	private BigDecimal baseSalarytotal; // ��н�ܺ�
	private BigDecimal lunchAllowance; // ��ͽ���
	private BigDecimal synthesisAllowance; // �ۺϽ���
	private BigDecimal liveAllowance; // �ֳ�����
	private BigDecimal fixBonus; // ������
	private BigDecimal rebuildBonus; // ���콱��
	private BigDecimal maintenanceBonus; // ά�����۽���
	private BigDecimal saveRecallBonus; // ��ȫ���ٽ���
	private BigDecimal maintenanceEfficiencyBonus; // ά��Ч�ʽ���
	private BigDecimal setupManagementBonus; // ��װ������
	private BigDecimal engineeringBonus; // ���̼�Ч���˽�
	private BigDecimal salesBonus; // ���۽���
	private BigDecimal accountReceivable; // Ӧ���ʿ�
	private BigDecimal translateBonus; // ���뽱��
	private BigDecimal otherBonus; // ��������
	private BigDecimal yearHolidayBonus; // �����н
	private BigDecimal trafficAllowance; // ��ͨ����
	private BigDecimal houseAllowance; // ס������
	private BigDecimal appointAllowance; // ���ɽ���
	private BigDecimal heatAllowance; // ���¡���ů��
	private BigDecimal goodEmployeeHortation; // ����Ա������
	private BigDecimal aimBonus; // Ŀ�꽱��
	private BigDecimal performanceBonus; // ��Ч����
	private BigDecimal accountDutyBonus; // ����һ���Է���˰
	private BigDecimal FTTTranceAllowance; // FTT��ѵ����
	private BigDecimal finacialCompensate; // ���ò�����
	private BigDecimal peopleSaveBonus; // �˲ű�����
	private BigDecimal otherAllowance; // �����ӿ�_˰ǰ
	private BigDecimal otherAfterAddFare; // �����ӿ-˰��
	private BigDecimal otherBeforeMinusFare; // �����ۿ��˰ǰ
	private BigDecimal otherAfterMinusFare; // �����ۿ��˰��
	private BigDecimal allowanceTotal; // �����ܶ�
	private BigDecimal middleNightAllowance; // ��ҹ��
	private BigDecimal otAllowance; // �Ӱ��
	private BigDecimal otTotal; // �Ӱ��ܶ�
	private BigDecimal accumulationFund; // ������/����
	private BigDecimal annuitiesFund; // ���Ͻ�/����
	private BigDecimal hospitalizationInsurance; // ҽ�Ʊ���
	private BigDecimal lostWorkFund; // ʧҵ����
	private BigDecimal procreateFund; // ����
	private BigDecimal workBreakFund; // ����
	private BigDecimal selfAccumulationFund; // ���ṫ����
	private BigDecimal selfAnnuitiesFund; // �������Ͻ�
	private BigDecimal selfHospitalizationInsurance; // ����ҽ�ƽ�
	private BigDecimal selfIdlenessFund; // ����ʧҵ��
	private BigDecimal labourUnionFund; // �����
	private BigDecimal birthdayFund; // �������
	private BigDecimal personOwnDuty; // ��������˰
	private BigDecimal bonusDuty; // ����˰
	private BigDecimal finacialCompensateDuty; // ���ò���������˰
	private BigDecimal OriSalary; // ����Ӧ������
	private BigDecimal FactSalary; // ����ʵ������
	private BigDecimal overheadExpenses; // �������
	private BigDecimal bf;
	private BigDecimal bg;
	private BigDecimal labourUnionBase; // �������
	private BigDecimal labourUnionBaseFund; // �����
	private BigDecimal salary; // ����

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
		// ����Ӧ��н��-�Ӱ��
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
