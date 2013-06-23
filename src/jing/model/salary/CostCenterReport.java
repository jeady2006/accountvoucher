package jing.model.salary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CostCenterReport {
	private HashMap<String, Summary> summarys = new HashMap<String, Summary>();
	private HashMap<String, FEsco> fescos = new HashMap<String, FEsco>();
	private HashMap<String, Local> locals = new HashMap<String, Local>();
	private List<Bonus> bonuses = new ArrayList<Bonus>();
	private HashMap<String, RebuildBonusDetail> rebuildBonusDetails = new HashMap<String, RebuildBonusDetail>();
	private HashMap<String, SaleBonusDetail> saleBonusDetails = new HashMap<String, SaleBonusDetail>();
	private List<SaleBonusDetail> otherSaleBonusDetails = new ArrayList<SaleBonusDetail>();
	private HashMap<String, SetupManageBonusDetail> setupManageBonusDetails = new HashMap<String, SetupManageBonusDetail>();
	private HashMap<String, VOBonusDetail> voBonusDetails = new HashMap<String, VOBonusDetail>();

	public void clear() {
		summarys.clear();
		fescos.clear();
		locals.clear();
		bonuses.clear();
		rebuildBonusDetails.clear();
		saleBonusDetails.clear();
		otherSaleBonusDetails.clear();
		setupManageBonusDetails.clear();
		voBonusDetails.clear();
	}

	public HashMap<String, Summary> getSummarys() {
		return summarys;
	}

	public void setSummarys(HashMap<String, Summary> summarys) {
		this.summarys = summarys;
	}

	public HashMap<String, FEsco> getFescos() {
		return fescos;
	}

	public void setFescos(HashMap<String, FEsco> fescos) {
		this.fescos = fescos;
	}

	public HashMap<String, Local> getLocals() {
		return locals;
	}

	public void setLocals(HashMap<String, Local> locals) {
		this.locals = locals;
	}

	public List<Bonus> getBonuses() {
		return bonuses;
	}

	public void setBonuses(List<Bonus> bonuses) {
		this.bonuses = bonuses;
	}

	public HashMap<String, RebuildBonusDetail> getRebuildBonusDetails() {
		return rebuildBonusDetails;
	}

	public void setRebuildBonusDetails(
			HashMap<String, RebuildBonusDetail> rebuildBonusDetails) {
		this.rebuildBonusDetails = rebuildBonusDetails;
	}

	public HashMap<String, SaleBonusDetail> getSaleBonusDetails() {
		return saleBonusDetails;
	}

	public void setSaleBonusDetails(
			HashMap<String, SaleBonusDetail> saleBonusDetails) {
		this.saleBonusDetails = saleBonusDetails;
	}

	public List<SaleBonusDetail> getOtherSaleBonusDetails() {
		return otherSaleBonusDetails;
	}

	public void setOtherSaleBonusDetails(
			List<SaleBonusDetail> otherSaleBonusDetails) {
		this.otherSaleBonusDetails = otherSaleBonusDetails;
	}

	public HashMap<String, SetupManageBonusDetail> getSetupManageBonusDetails() {
		return setupManageBonusDetails;
	}

	public void setSetupManageBonusDetails(
			HashMap<String, SetupManageBonusDetail> setupManageBonusDetails) {
		this.setupManageBonusDetails = setupManageBonusDetails;
	}

	public HashMap<String, VOBonusDetail> getVoBonusDetails() {
		return voBonusDetails;
	}

	public void setVoBonusDetails(HashMap<String, VOBonusDetail> voBonusDetails) {
		this.voBonusDetails = voBonusDetails;
	}

}
