package jing.model.accrual;

public class WBS {
	private String project; // 项目
	private String salesVoucher; // 销售凭证
	private String productLine; // 产品线
	private String company; // 分公司/办事处
	private String productGroup; // 产品组
	private String contractNo; // 合同号
	private String wbsNo; // wbs号码
	private String projectDes; // 项目描述
	private String companyCode; // 公司代码
	private String pr; // 利润中心
	private String salesOrganization; // 销售组织
	private String salesOrderType; // 销售订单类型

	public WBS(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getSalesVoucher() {
		return salesVoucher;
	}

	public void setSalesVoucher(String salesVoucher) {
		this.salesVoucher = salesVoucher;
	}

	public String getProductLine() {
		return productLine;
	}

	public void setProductLine(String productLine) {
		this.productLine = productLine;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getProductGroup() {
		return productGroup;
	}

	public void setProductGroup(String productGroup) {
		this.productGroup = productGroup;
	}

	public String getContractNo() {
		return contractNo;
	}

	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}

	public String getWbsNo() {
		return wbsNo;
	}

	public void setWbsNo(String wbsNo) {
		this.wbsNo = wbsNo;
	}

	public String getProjectDes() {
		return projectDes;
	}

	public void setProjectDes(String projectDes) {
		this.projectDes = projectDes;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getPr() {
		return pr;
	}

	public void setPr(String pr) {
		this.pr = pr;
	}

	public String getSalesOrganization() {
		return salesOrganization;
	}

	public void setSalesOrganization(String salesOrganization) {
		this.salesOrganization = salesOrganization;
	}

	public String getSalesOrderType() {
		return salesOrderType;
	}

	public void setSalesOrderType(String salesOrderType) {
		this.salesOrderType = salesOrderType;
	}

}
