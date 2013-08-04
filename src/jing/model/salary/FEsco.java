package jing.model.salary;

import java.math.BigDecimal;

import jing.model.data.RuntimeData;

public class FEsco {
	public static final String NAME = "社保明细-FESCO";
	
	private String costCenter;
	private String pt;
	private BigDecimal accumulationFund; // 公积金
	private BigDecimal annuitiesFund; // 养老金
	private BigDecimal hospitalizationInsurance; // 医疗金
	private BigDecimal idlenessFund; // 失业金
	private BigDecimal publicAccumulationFund; // 公积金公提
	private BigDecimal publicAnnuitiesFund; // 养老金公提
	private BigDecimal publicHospitalizationInsurance; // 医疗金公提
	private BigDecimal publicIdlenessFund; // 失业金公提
	private BigDecimal publicProcreateFund; // 生育金公提
	private BigDecimal publicWorkHurt; // 工伤金公提
	private BigDecimal managementFare; // 管理费

	public FEsco(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getPt() {
		if(this.pt == null){
			this.pt = RuntimeData.getInstance().getPrByCode(costCenter);
		}
		return pt;
	}

	public void setPt(String pt) {
		this.pt = pt;
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

	public BigDecimal getIdlenessFund() {
		return idlenessFund;
	}

	public void setIdlenessFund(BigDecimal idlenessFund) {
		this.idlenessFund = idlenessFund;
	}

	public BigDecimal getPublicAccumulationFund() {
		return publicAccumulationFund;
	}

	public void setPublicAccumulationFund(BigDecimal publicAccumulationFund) {
		this.publicAccumulationFund = publicAccumulationFund;
	}

	public BigDecimal getPublicAnnuitiesFund() {
		return publicAnnuitiesFund;
	}

	public void setPublicAnnuitiesFund(BigDecimal publicAnnuitiesFund) {
		this.publicAnnuitiesFund = publicAnnuitiesFund;
	}

	public BigDecimal getPublicHospitalizationInsurance() {
		return publicHospitalizationInsurance;
	}

	public void setPublicHospitalizationInsurance(
			BigDecimal publicHospitalizationInsurance) {
		this.publicHospitalizationInsurance = publicHospitalizationInsurance;
	}

	public BigDecimal getPublicIdlenessFund() {
		return publicIdlenessFund;
	}

	public void setPublicIdlenessFund(BigDecimal publicIdlenessFund) {
		this.publicIdlenessFund = publicIdlenessFund;
	}

	public BigDecimal getPublicProcreateFund() {
		return publicProcreateFund;
	}

	public void setPublicProcreateFund(BigDecimal publicProcreateFund) {
		this.publicProcreateFund = publicProcreateFund;
	}

	public BigDecimal getPublicWorkHurt() {
		return publicWorkHurt;
	}

	public void setPublicWorkHurt(BigDecimal publicWorkHurt) {
		this.publicWorkHurt = publicWorkHurt;
	}

	public BigDecimal getManagementFare() {
		return managementFare;
	}

	public void setManagementFare(BigDecimal managementFare) {
		this.managementFare = managementFare;
	}

}
