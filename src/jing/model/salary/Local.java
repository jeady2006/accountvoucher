package jing.model.salary;

import java.math.BigDecimal;

import jing.model.data.RuntimeData;

public class Local {
	public static final String NAME = "�籣��ϸ-Local";
	
	private String costCenter;
	private String pr;
	private BigDecimal accumulationFund; // ������
	private BigDecimal annuitiesFund; // ���Ͻ�
	private BigDecimal hospitalizationInsurance; // ҽ�ƽ�
	private BigDecimal idlenessFund; // ʧҵ��
	private BigDecimal publicAccumulationFund; // ��������
	private BigDecimal publicAnnuitiesFund; // ���Ͻ���
	private BigDecimal publicHospitalizationInsurance; // ҽ�ƽ���
	private BigDecimal publicIdlenessFund; // ʧҵ����
	private BigDecimal publicProcreateFund; // ��������
	private BigDecimal publicWorkHurt; // ���˽���
	private BigDecimal managementFare; // �����

	public Local(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getCostCenter() {
		return costCenter;
	}

	public void setCostCenter(String costCenter) {
		this.costCenter = costCenter;
	}

	public String getPr() {
		if(this.pr == null){
			this.pr = RuntimeData.getInstance().getPrByCode(costCenter);
		}
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
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
