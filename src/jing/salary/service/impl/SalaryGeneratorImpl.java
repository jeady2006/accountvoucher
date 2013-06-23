package jing.salary.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jing.model.accrual.WBS;
import jing.model.data.RuntimeData;
import jing.model.global.CostCenter;
import jing.model.salary.Bonus;
import jing.model.salary.CostCenterReport;
import jing.model.salary.FEsco;
import jing.model.salary.Local;
import jing.model.salary.RebuildBonusDetail;
import jing.model.salary.SalaryType;
import jing.model.salary.SaleBonusDetail;
import jing.model.salary.SetupManageBonusDetail;
import jing.model.salary.Summary;
import jing.model.salary.VOBonusDetail;
import jing.model.salary.type.Accumulation;
import jing.model.salary.type.Annuities;
import jing.model.salary.type.Hospitalization;
import jing.model.salary.type.LabourUnion;
import jing.model.salary.type.LostWork;
import jing.model.salary.type.Procreate;
import jing.model.salary.type.SHManageFee;
import jing.model.salary.type.WorkBreak;
import jing.salary.service.SalaryGenerator;
import jing.salary.service.WBSLoader;
import jing.util.cache.ProcessCache;
import jing.util.excel.POIReader;
import jing.util.excel.POIWriter;
import jing.util.lang.NumbericUtils;
import jing.util.message.Message;
import jing.util.message.MessageProvider;

public class SalaryGeneratorImpl implements SalaryGenerator {
	private final static int SALARY_SKIP_ROW_COUNT = 3;
	private final static String NO_INCLUDE_PR = "-1";

	private int month;
	private String detailFile = null;
	private String pr;
	private String companyCode;
	private String postingDate;
	private HashMap<String, List<String>> prSummaryMapper = new HashMap<String, List<String>>();
	private HashMap<String, List<SaleBonusDetail>> saleBonusDetailMapper = new HashMap<String, List<SaleBonusDetail>>();
	private HashMap<String, List<SetupManageBonusDetail>> setupManageBonusDetailMapper = new HashMap<String, List<SetupManageBonusDetail>>();
	private HashMap<String, List<RebuildBonusDetail>> rebuildBonusDetailMapper = new HashMap<String, List<RebuildBonusDetail>>();
	private HashMap<String, List<VOBonusDetail>> voBonusDetailMapper = new HashMap<String, List<VOBonusDetail>>();
	private HashMap<String, WBS> wbses = new HashMap<String, WBS>();
	private POIReader reader = null;
	private CostCenterReport report = null;
	private POIWriter write = null;

	private String includePr;

	@Override
	public void load(String wbsFile, String file) {
		this.detailFile = file;
		// ����wbs�ļ�
		wbses = WBSLoader.load(wbsFile);
		if (wbses == null || wbses.size() < 1) {
			System.out.println("Wbs file has no record");
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"Wbs�ļ���û�м�¼");
			return;
		}

		// ��ȡ��ϸ�ļ�
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"��ʼ������ϸ�ļ�...");
		reader = new POIReader(file);
		// ���ص�һ��sheet
		reader.loadSheet(0);
		// ������ͷ��3�У����������У�
		reader.skipRow(SALARY_SKIP_ROW_COUNT);
		Summary summary = null;
		CostCenter cc = null;
		WBS w = null;
		String s = null;
		int i = 0;
		// ��ʼ����ϸ�ļ���������������������еı�
		report = new CostCenterReport();
		prSummaryMapper.clear();

		ProcessCache.setCacheValue(SalaryGenerator.class.getName(), 10);

		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			// ȡ�óɱ����ı��
			s = reader.getStringByIndex(i);
			// ����������ܼƣ�˵���Ѿ������˹��ʱ����˳�ѭ��
			if (s.equals("�ܼ�"))
				break;
			// ʹ�óɱ����ı��ʵ�������ʶ���
			summary = new Summary(s);
			// ȡ�óɱ����Ķ���
			cc = RuntimeData.getInstance().getCostCenterByCode(s);
			// ����ɱ����Ķ���Ϊ��
			if (cc != null) {
				// ���ù��ʶ������������Ϊ�ɱ����ĵ�pr
				summary.setPr(cc.getPr());
				// �������ӳ����󲻰������pr���½�һ���б��ӵ�ӳ�������
				if (!prSummaryMapper.containsKey(summary.getPr())) {
					prSummaryMapper.put(summary.getPr(),
							new ArrayList<String>());
				}
				// �ѹ��ʶ���ӵ�����ӳ������Ӧ���б���
				prSummaryMapper.get(summary.getPr()).add(s);
			}
			// ��ȡ��н
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setBaseSalary(new BigDecimal(0));
			} else {
				summary.setBaseSalary(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setBaseSalarytotal(new BigDecimal(0));
			} else {
				summary.setBaseSalarytotal(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setLunchAllowance(new BigDecimal(0));
			} else {
				summary.setLunchAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSynthesisAllowance(new BigDecimal(0));
			} else {
				summary.setSynthesisAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setLiveAllowance(new BigDecimal(0));
			} else {
				summary.setLiveAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setFixBonus(new BigDecimal(0));
			} else {
				summary.setFixBonus(new BigDecimal(reader.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setRebuildBonus(new BigDecimal(0));
			} else {
				summary.setRebuildBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setMaintenanceBonus(new BigDecimal(0));
			} else {
				summary.setMaintenanceBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSaveRecallBonus(new BigDecimal(0));
			} else {
				summary.setSaveRecallBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setMaintenanceEfficiencyBonus(new BigDecimal(0));
			} else {
				summary.setMaintenanceEfficiencyBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSetupManagementBonus(new BigDecimal(0));
			} else {
				summary.setSetupManagementBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setEngineeringBonus(new BigDecimal(0));
			} else {
				summary.setEngineeringBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSalesBonus(new BigDecimal(0));
			} else {
				summary.setSalesBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setAccountReceivable(new BigDecimal(0));
			} else {
				summary.setAccountReceivable(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setTranslateBonus(new BigDecimal(0));
			} else {
				summary.setTranslateBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOtherBonus(new BigDecimal(0));
			} else {
				summary.setOtherBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setYearHolidayBonus(new BigDecimal(0));
			} else {
				summary.setYearHolidayBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setTrafficAllowance(new BigDecimal(0));
			} else {
				summary.setTrafficAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setHouseAllowance(new BigDecimal(0));
			} else {
				summary.setHouseAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setAppointAllowance(new BigDecimal(0));
			} else {
				summary.setAppointAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setHeatAllowance(new BigDecimal(0));
			} else {
				summary.setHeatAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setGoodEmployeeHortation(new BigDecimal(0));
			} else {
				summary.setGoodEmployeeHortation(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setAimBonus(new BigDecimal(0));
			} else {
				summary.setAimBonus(new BigDecimal(reader.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setPerformanceBonus(new BigDecimal(0));
			} else {
				summary.setPerformanceBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setAccountDutyBonus(new BigDecimal(0));
			} else {
				summary.setAccountDutyBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setFTTTranceAllowance(new BigDecimal(0));
			} else {
				summary.setFTTTranceAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setFinacialCompensate(new BigDecimal(0));
			} else {
				summary.setFinacialCompensate(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setPeopleSaveBonus(new BigDecimal(0));
			} else {
				summary.setPeopleSaveBonus(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOtherAllowance(new BigDecimal(0));
			} else {
				summary.setOtherAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOtherAfterAddFare(new BigDecimal(0));
			} else {
				summary.setOtherAfterAddFare(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOtherBeforeMinusFare(new BigDecimal(0));
			} else {
				summary.setOtherBeforeMinusFare(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOtherAfterMinusFare(new BigDecimal(0));
			} else {
				summary.setOtherAfterMinusFare(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setAllowanceTotal(new BigDecimal(0));
			} else {
				summary.setAllowanceTotal(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setMiddleNightAllowance(new BigDecimal(0));
			} else {
				summary.setMiddleNightAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOtAllowance(new BigDecimal(0));
			} else {
				summary.setOtAllowance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOtTotal(new BigDecimal(0));
			} else {
				summary.setOtTotal(new BigDecimal(reader.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setAccumulationFund(new BigDecimal(0));
			} else {
				summary.setAccumulationFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setAnnuitiesFund(new BigDecimal(0));
			} else {
				summary.setAnnuitiesFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setHospitalizationInsurance(new BigDecimal(0));
			} else {
				summary.setHospitalizationInsurance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setLostWorkFund(new BigDecimal(0));
			} else {
				summary.setLostWorkFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setProcreateFund(new BigDecimal(0));
			} else {
				summary.setProcreateFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setWorkBreakFund(new BigDecimal(0));
			} else {
				summary.setWorkBreakFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSelfAccumulationFund(new BigDecimal(0));
			} else {
				summary.setSelfAccumulationFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSelfAnnuitiesFund(new BigDecimal(0));
			} else {
				summary.setSelfAnnuitiesFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSelfHospitalizationInsurance(new BigDecimal(0));
			} else {
				summary.setSelfHospitalizationInsurance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setSelfIdlenessFund(new BigDecimal(0));
			} else {
				summary.setSelfIdlenessFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setLabourUnionFund(new BigDecimal(0));
			} else {
				summary.setLabourUnionFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setBirthdayFund(new BigDecimal(0));
			} else {
				summary.setBirthdayFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setPersonOwnDuty(new BigDecimal(0));
			} else {
				summary.setPersonOwnDuty(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setBonusDuty(new BigDecimal(0));
			} else {
				summary.setBonusDuty(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setFinacialCompensateDuty(new BigDecimal(0));
			} else {
				summary.setFinacialCompensateDuty(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOriSalary(new BigDecimal(0));
			} else {
				summary.setOriSalary(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setFactSalary(new BigDecimal(0));
			} else {
				summary.setFactSalary(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				summary.setOverheadExpenses(new BigDecimal(0));
			} else {
				summary.setOverheadExpenses(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			// �ѹ��ʶ���ӵ��ļ�����Ĺ���ӳ�������Գɱ�������Ϊkey
			report.getSummarys().put(summary.getCostCenter(), summary);
		}
		// reader.skipRow(FESCO_SKIP_ROW_COUNT);

		// ����N�У�ֱ���еĵ�һ��ΪFEsco.NAMEΪֹ
		this.skipNext(FEsco.NAME);

		FEsco fesco = null;
		reader.skipRow(1); // ��������
		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			s = reader.getStringByIndex(i);
			if (s.equals("�ܼ�"))
				break;
			fesco = new FEsco(s);
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setAccumulationFund(new BigDecimal(0));
			} else {
				fesco.setAccumulationFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setAnnuitiesFund(new BigDecimal(0));
			} else {
				fesco.setAnnuitiesFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setHospitalizationInsurance(new BigDecimal(0));
			} else {
				fesco.setHospitalizationInsurance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setIdlenessFund(new BigDecimal(0));
			} else {
				fesco.setIdlenessFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setPublicAccumulationFund(new BigDecimal(0));
			} else {
				fesco.setPublicAccumulationFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setPublicAnnuitiesFund(new BigDecimal(0));
			} else {
				fesco.setPublicAnnuitiesFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setPublicHospitalizationInsurance(new BigDecimal(0));
			} else {
				fesco.setPublicHospitalizationInsurance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setPublicIdlenessFund(new BigDecimal(0));
			} else {
				fesco.setPublicIdlenessFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setPublicProcreateFund(new BigDecimal(0));
			} else {
				fesco.setPublicProcreateFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setPublicWorkHurt(new BigDecimal(0));
			} else {
				fesco.setPublicWorkHurt(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				fesco.setManagementFare(new BigDecimal(0));
			} else {
				fesco.setManagementFare(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			fesco.setPt(RuntimeData.getInstance().getPrByCode(s));
			report.getFescos().put(s, fesco);
		}

		// reader.skipRow(LOCAL_SKIP_ROW_COUNT);

		this.skipNext(Local.NAME);
		Local local = null;
		reader.skipRow(1);
		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			s = reader.getStringByIndex(i);
			if (s.equals("�ܼ�"))
				break;
			local = new Local(s);
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setAccumulationFund(new BigDecimal(0));
			} else {
				local.setAccumulationFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setAnnuitiesFund(new BigDecimal(0));
			} else {
				local.setAnnuitiesFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setHospitalizationInsurance(new BigDecimal(0));
			} else {
				local.setHospitalizationInsurance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setIdlenessFund(new BigDecimal(0));
			} else {
				local.setIdlenessFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setPublicAccumulationFund(new BigDecimal(0));
			} else {
				local.setPublicAccumulationFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setPublicAnnuitiesFund(new BigDecimal(0));
			} else {
				local.setPublicAnnuitiesFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setPublicHospitalizationInsurance(new BigDecimal(0));
			} else {
				local.setPublicHospitalizationInsurance(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setPublicIdlenessFund(new BigDecimal(0));
			} else {
				local.setPublicIdlenessFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setPublicProcreateFund(new BigDecimal(0));
			} else {
				local.setPublicProcreateFund(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setPublicWorkHurt(new BigDecimal(0));
			} else {
				local.setPublicWorkHurt(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				local.setManagementFare(new BigDecimal(0));
			} else {
				local.setManagementFare(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			local.setPr(RuntimeData.getInstance().getPrByCode(s));
			report.getLocals().put(s, local);
		}
		// reader.skipRow(BONUSES_SKIP_ROW_COUNT);

		this.skipNext(Bonus.NAME);
		Bonus bonus = null;
		reader.skipRow(1);
		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			bonus = new Bonus();
			bonus.setChargeFrom(reader.getStringByIndex(i));
			if (bonus.getChargeFrom().equals(""))
				break;
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				bonus.setChargeTo(reader.getStringByIndex(i));
			} else {
				bonus.setChargeTo(NumbericUtils.doubleToString(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				bonus.setPayrollMonth(reader.getStringByIndex(i));
			} else {
				bonus.setPayrollMonth(NumbericUtils.doubleToString(reader
						.getNumbericByIndex(i)));

			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				bonus.setItem(reader.getStringByIndex(i));
			} else {
				bonus.setItem(NumbericUtils.doubleToString(reader
						.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				bonus.setAmount(new BigDecimal(0));
			} else {
				bonus.setAmount(new BigDecimal(reader.getNumbericByIndex(i)));
			}
			i++;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				bonus.setRemark(reader.getStringByIndex(i));
			} else {
				bonus.setRemark(NumbericUtils.doubleToString(reader
						.getNumbericByIndex(i)));
			}
			report.getBonuses().add(bonus);
		}

		this.skipNext(RebuildBonusDetail.NAME);

		RebuildBonusDetail rebuild = null;
		reader.skipRow(1);
		BigDecimal orgSum = null;
		while (reader.hasRow()) {
			reader.nextRow();
			orgSum = null;
			i = 0;
			s = reader.getStringByIndex(i);
			if (s.equals(""))
				break;
			if (report.getRebuildBonusDetails().containsKey(s)) {
				rebuild = report.getRebuildBonusDetails().get(s);
				orgSum = rebuild.getSum();
			} else {
				rebuild = new RebuildBonusDetail(s);
				w = wbses.get(s);
				if (w != null && w != null) {
					rebuild.setPr(w.getPr());
				}
			}
			i += 2;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				if (orgSum == null)
					rebuild.setSum(new BigDecimal(0));
			} else {
				if (orgSum == null)
					rebuild.setSum(new BigDecimal(reader.getNumbericByIndex(i)));
				else
					rebuild.setSum(new BigDecimal(reader.getNumbericByIndex(i))
							.add(orgSum));
			}
			if (rebuild.getPr() != null) {
				if (rebuildBonusDetailMapper.get(rebuild.getPr()) == null) {
					rebuildBonusDetailMapper.put(rebuild.getPr(),
							new ArrayList<RebuildBonusDetail>());
				}
				if (orgSum != null) {
					for (RebuildBonusDetail d : rebuildBonusDetailMapper
							.get(rebuild.getPr())) {
						if (d.getWbsNumber().equals(s)) {
							d.setSum(rebuild.getSum());
						}
					}
				} else {
					rebuildBonusDetailMapper.get(rebuild.getPr()).add(rebuild);
				}
			}
			report.getRebuildBonusDetails().put(s, rebuild);
		}
		// reader.skipRow(SALEBONUSDETAIL_SKIP_ROW_COUNT);

		this.skipNext(SaleBonusDetail.NAME);
		SaleBonusDetail saleBonus = null;
		saleBonusDetailMapper.clear();
		reader.skipRow(1);
		List<SaleBonusDetail> saleBonusDetailList = null;
		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			s = reader.getStringByIndex(i);
			if (s.equals(""))
				break;
			saleBonus = new SaleBonusDetail(s);
			i += 2;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				saleBonus.setSum(new BigDecimal(0));
			} else {
				saleBonus.setSum(new BigDecimal(reader.getNumbericByIndex(i)));
			}
			if (s.endsWith("L") || s.endsWith("M")) {
				report.getSaleBonusDetails().put(s, saleBonus);
			} else {
				report.getOtherSaleBonusDetails().add(saleBonus);
			}
			w = wbses.get(s);
			if (w != null && w.getPr() != null) {
				if (saleBonusDetailMapper.get(w.getPr()) == null) {
					saleBonusDetailList = new ArrayList<SaleBonusDetail>();
					saleBonusDetailMapper.put(w.getPr(), saleBonusDetailList);
				}
				saleBonusDetailMapper.get(w.getPr()).add(saleBonus);
			}
		}

		this.skipNext(SetupManageBonusDetail.NAME);
		SetupManageBonusDetail setupDetail = null;
		reader.skipRow(1);
		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			s = reader.getStringByIndex(i);
			if (s.equals("")) {
				break;
			}
			setupDetail = new SetupManageBonusDetail(s);
			w = wbses.get(s);
			if (w != null) {
				setupDetail.setPr(w.getPr());
			}
			i += 2;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				setupDetail.setAmount(new BigDecimal(0));
			} else {
				setupDetail.setAmount(new BigDecimal(reader
						.getNumbericByIndex(i)));
			}
			if (setupDetail.getPr() != null) {
				if (setupManageBonusDetailMapper.get(setupDetail.getPr()) == null) {
					setupManageBonusDetailMapper.put(setupDetail.getPr(),
							new ArrayList<SetupManageBonusDetail>());
				}
				setupManageBonusDetailMapper.get(setupDetail.getPr()).add(
						setupDetail);
			}
			report.getSetupManageBonusDetails().put(s, setupDetail);
		}

		this.skipNext(VOBonusDetail.NAME);
		VOBonusDetail voBonusDetial = null;
		reader.skipRow(1);
		while (reader.hasRow()) {
			reader.nextRow();
			s = reader.getNextStringFormat();
			if (s.equals("")) {
				break;
			}
			voBonusDetial = report.getVoBonusDetails().get(s);
			if (voBonusDetial == null) {
				voBonusDetial = new VOBonusDetail(s);
				voBonusDetial.setAmount(new BigDecimal(0));
				w = wbses.get(s);
				if (w != null) {
					voBonusDetial.setPr(w.getPr());
				}
			}
			reader.skipCell(1);
			voBonusDetial.setAmount(voBonusDetial.getAmount().add(
					reader.getNextBigDecimalFormat()));
			if (voBonusDetial.getPr() != null
					&& !report.getVoBonusDetails().containsKey(s)) {
				if (voBonusDetailMapper.get(voBonusDetial.getPr()) == null) {
					voBonusDetailMapper.put(voBonusDetial.getPr(),
							new ArrayList<VOBonusDetail>());
				}
				voBonusDetailMapper.get(voBonusDetial.getPr()).add(
						voBonusDetial);
			}
			report.getVoBonusDetails().put(s, voBonusDetial);
		}

		reader.destroy();
		MessageProvider.getInstance()
				.publicMessage(Message.DEBUG, "��ϸ�ļ��Ѽ�����ɡ�");
	}

	/**
	 * ���ɹ��ʱ�
	 */
	@Override
	public String generate(String pr, int month, String postingDate) {
		// ��Ҫ���ɵ���������
		this.pr = pr;
		// ��Ҫ���ɵ��·�
		this.month = month;
		// ��Ҫ���ɵ�postingDate
		this.postingDate = postingDate;
		// ����pr��ȡ��pr��Ӧ�ĵ���Ӣ�ı���
		this.companyCode = RuntimeData.getInstance().getCompanyCodeByPr(pr);
		// ������ɵ���3920��3923������Ҫͳ��3910��ֻ��ֻ������һ��ƾ֤
		if (this.pr.equals("3923") || this.pr.equals("3920")
				|| this.pr.equals("3916")) {
			this.includePr = "3910";
		} else {
			this.includePr = NO_INCLUDE_PR;
		}
		// �������ڸ�ʽ
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");

		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();
		// ����������ɵ��ļ���
		fileName += "/" + this.month + "�¹���-" + pr + ".xls";
		try {
			write = new POIWriter(fileName);

			// ���ɹ��ʹ�����
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"��ʼ����" + pr + "����...");
			this.generateIntegration();
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					pr + "���������ɡ�");
			// ����ʵ�����ʹ�����
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"��ʼ����" + pr + "ʵ������...");
			this.generatePraticeSalary();
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					pr + "ʵ�����������ɡ�");

			// ֻ��3910��Ҫ���ɵ����Ź����������Ź�����ֻ������һ�������ƾ֤
			if (pr.equals("3910")) {
				MessageProvider.getInstance().publicMessage(Message.DEBUG,
						"��ʼ����" + pr + "����...");
				if (detailFile.indexOf("-BJ-") < 0) {
					this.generate3910CA();
				}
				MessageProvider.getInstance().publicMessage(Message.DEBUG,
						pr + "���������ɡ�");
			}

			write.flush();
			write.destroy();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return fileName;
	}

	private void generateIntegrationTitle() {
		write.createRow();
		write.setNextStringData("No.");
		write.setNextStringData("Doc. Date");
		write.setNextStringData("Posting Date");
		write.setNextStringData("Currency");
		write.setNextStringData("Exchange Rate");
		write.setNextStringData("Reference (16)");
		write.setNextStringData("Doc.header text (25)");
		write.setNextStringData("Posting Key");
		write.setNextStringData("Account");
		write.setNextStringData("Amount");
		write.setNextStringData("Tax code");
		write.setNextStringData("Determine tax base");
		write.setNextStringData("Cost Center");
		write.setNextStringData("Internal Order");
		write.setNextStringData("WBS element");
		write.setNextStringData("Assignment (18)");
		write.setNextStringData("Text (50)");
	}

	private void generateIntegration() {
		write.createNewSheet(pr + "����");
		// ���ɱ���
		this.generateIntegrationTitle();

		// ���ɹ��ᾭ��
		this.generateIntegrationDetail(new LabourUnion(month + ""));
		// �������ϱ���
		this.generateIntegrationDetail(new Annuities(month + ""));
		// ����ҽ�Ʊ���
		this.generateIntegrationDetail(new Hospitalization(month + ""));
		// ����ס��
		this.generateIntegrationDetail(new Accumulation(month + ""));
		// ����ʧҵ����
		this.generateIntegrationDetail(new LostWork(month + ""));
		// ���ɹ��˱���
		this.generateIntegrationDetail(new WorkBreak(month + ""));
		// ������������
		this.generateIntegrationDetail(new Procreate(month + ""));
		// �Ϻ���������
		this.generateIntegrationDetail(new SHManageFee(month + ""));

	}

	/**
	 * ���ݹ������ͣ����ɹ��ʱ�
	 * 
	 * @param salaryType
	 *            ��������
	 */
	private void generateIntegrationDetail(SalaryType salaryType) {
		// ���ʶ���
		Summary summary = null;
		BigDecimal amount = new BigDecimal(0);
		BigDecimal other = new BigDecimal(0);
		String prCode = RuntimeData.getInstance().getCompanyCodeByPr(this.pr);
		// ����prȡ������pr�Ĺ��ʣ��������ʶ���
		for (String s : this.prSummaryMapper.get(pr)) {
			// ȡ�ù��ʶ���
			summary = report.getSummarys().get(s);
			if (summary == null) {
				continue;
			}

			// ���������Ҫ���ɵĹ������ͣ�ȡ�����ѭ���Ľ��
			if (salaryType.getType() == LabourUnion.TYPE) {
				amount = summary.getLabourUnionBaseFund();
			} else if (salaryType.getType() == Annuities.TYPE) {
				amount = summary.getAnnuitiesFund();
			} else if (salaryType.getType() == Hospitalization.TYPE) {
				amount = summary.getHospitalizationInsurance();
			} else if (salaryType.getType() == Accumulation.TYPE) {
				amount = summary.getAccumulationFund();
			} else if (salaryType.getType() == LostWork.TYPE) {
				amount = summary.getLostWorkFund();
			} else if (salaryType.getType() == WorkBreak.TYPE) {
				amount = summary.getWorkBreakFund();
			} else if (salaryType.getType() == Procreate.TYPE) {
				amount = summary.getProcreateFund();
			} else {
				amount = summary.getOverheadExpenses();
			}
			// ������ʶ���Ľ����0���Բ�����
			if (amount.intValue() == 0)
				continue;

			// ����40����ƾ֤��һ�����ʶ��������һ��40ƾ֤
			write.createRow();
			write.setNextStringData((salaryType.getType() + 1) + "");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData("40");
			write.setNextStringData(salaryType.getChart());
			write.setNextNumbericData(amount);

			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(summary.getCostCenter());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			// ����ν���ۼ�
			other = other.add(amount);
		}

		if (this.pr.equals("3920") || this.pr.equals("3923")
				|| this.pr.equals("3916")) {
			BigDecimal includeTotal = new BigDecimal(0);
			amount = new BigDecimal(0);

			// �������������һ�������3910����ʱ���Ǵ�fesco����3910��������һ��
			// �������������Ϻ���������ʹӹ��ʴ󳤱���ȡ�ܽ��
			List<String> costCenters = this.prSummaryMapper.get(includePr);
			if (salaryType.getType() < 1 || salaryType.getType() > 6) {
				for (String costCenterNo : costCenters) {
					summary = report.getSummarys().get(costCenterNo);
					// ���������Ҫ���ɵĹ������ͣ�ȡ�����ѭ���Ľ��
					if (salaryType.getType() == LabourUnion.TYPE) {
						amount = summary.getLabourUnionBaseFund();
					} else {
						amount = summary.getOverheadExpenses();
					}
					// ������ʶ���Ľ����0���Բ�����
					if (amount.intValue() == 0)
						continue;
					includeTotal = includeTotal.add(amount);
				}
			} else {
				HashMap<String, FEsco> fescos = report.getFescos();
				Collection<FEsco> c = fescos.values();
				Iterator<FEsco> its = c.iterator();
				FEsco fEsco = null;
				while (its.hasNext()) {
					fEsco = its.next();
					// ����ǵ�ǰ��pr���Ž����ۼ�
					if (fEsco.getPt().equals(includePr)) {
						if (salaryType.getType() == Annuities.TYPE) {
							amount = fEsco.getPublicAnnuitiesFund();
						} else if (salaryType.getType() == Hospitalization.TYPE) {
							amount = fEsco.getPublicHospitalizationInsurance();
						} else if (salaryType.getType() == Accumulation.TYPE) {
							amount = fEsco.getPublicAccumulationFund();
						} else if (salaryType.getType() == LostWork.TYPE) {
							amount = fEsco.getPublicIdlenessFund();
						} else if (salaryType.getType() == WorkBreak.TYPE) {
							amount = fEsco.getPublicWorkHurt();
						} else if (salaryType.getType() == Procreate.TYPE) {
							amount = fEsco.getPublicProcreateFund();
						}
						includeTotal = includeTotal.add(amount);
					}
				}

				// ������ɵ���3916������Ҫ��local����Ҳ�ӽ���
				if (this.pr.equals("3916")) {
					HashMap<String, Local> locals = report.getLocals();
					Collection<Local> l = locals.values();
					Iterator<Local> localItes = l.iterator();
					Local local = null;
					while (localItes.hasNext()) {
						local = localItes.next();
						if (local.getPr().equals(includePr)) {
							if (salaryType.getType() == Annuities.TYPE) {
								amount = local.getPublicAnnuitiesFund();
							} else if (salaryType.getType() == Hospitalization.TYPE) {
								amount = local
										.getPublicHospitalizationInsurance();
							} else if (salaryType.getType() == Accumulation.TYPE) {
								amount = local.getPublicAccumulationFund();
							} else if (salaryType.getType() == LostWork.TYPE) {
								amount = local.getPublicIdlenessFund();
							} else if (salaryType.getType() == WorkBreak.TYPE) {
								amount = local.getPublicWorkHurt();
							} else if (salaryType.getType() == Procreate.TYPE) {
								amount = local.getPublicProcreateFund();
							}
							includeTotal = includeTotal.add(amount);
						}
					}
				}
			}

			// ���������Ϻ���������
			// ѭ��includePr�ڹ��ʴ󳤱�������ϼ��������Է�����50ʱ��ʹ���������ȥlocal���ܽ��
			// ���pr��3916���������ɵ��ǹ��ᣬ����������40
			if (this.pr.equals("3916")
					&& salaryType.getType() == LabourUnion.TYPE) {
				// do not generate 40
			} else {
				for (String costCenterNo : costCenters) {
					summary = report.getSummarys().get(costCenterNo);
					if (salaryType.getType() == LabourUnion.TYPE) {
						other = other.add(summary.getLabourUnionBaseFund());
					} else if (salaryType.getType() == SHManageFee.TYPE) {
						other = other.add(summary.getOverheadExpenses());
					} else if (salaryType.getType() == Annuities.TYPE) {
						other = other.add(summary.getAnnuitiesFund());
					} else if (salaryType.getType() == Hospitalization.TYPE) {
						other = other
								.add(summary.getHospitalizationInsurance());
					} else if (salaryType.getType() == Accumulation.TYPE) {
						other = other.add(summary.getAccumulationFund());
					} else if (salaryType.getType() == LostWork.TYPE) {
						other = other.add(summary.getLostWorkFund());
					} else if (salaryType.getType() == WorkBreak.TYPE) {
						other = other.add(summary.getWorkBreakFund());
					} else if (salaryType.getType() == Procreate.TYPE) {
						other = other.add(summary.getProcreateFund());
					}
				}

				// 3916������3910�Ĺ���
				if (includeTotal.doubleValue() != 0) {
					write.createRow();
					write.setNextStringData((salaryType.getType() + 1) + "");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(salaryType.getText());
					write.setNextStringData("40");
					if (includePr.equals("3910")) {
						write.setNextStringData("1113001");
					} else {
						write.setNextStringData("");
					}
					write.setNextNumbericData(includeTotal);

					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(includePr); // �ѷ���д��includePr
					if (includePr.equals("3910")) {
						write.setNextStringData(prCode + "תHO"
								+ salaryType.getText());
					} else {
						write.setNextStringData("");
					}
				}
			}

		}

		// �����ǰpr����3916;������3916�����ҹ�������С��1�������ᣩ;������3916�����ҹ������ʹ���6�����Ϻ��������
		// ���ù��ʵ��ܽ������һ��50ƾ֤��assignment�ù������͵�secondAssignment
		if ((!pr.equals("3916") && !pr.equals("3910"))
				|| salaryType.getType() < 1 || salaryType.getType() > 6) {
			// ��3923��3920��50�����������һ��ʹ��other������ȥlocal���ܽ�����ǹ�����Ϻ����������ֻȡother�Ľ��
			write.createRow();
			write.setNextStringData((salaryType.getType() + 1) + "");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData("50");
			if (pr.equals("3910")) {
				write.setNextStringData("2173001");
			} else {
				write.setNextStringData(salaryType.getReverseChar());
			}
			if (salaryType.getType() < 1 || salaryType.getType() > 6) {
				write.setNextNumbericData(other);
			} else {
				BigDecimal localAmount = null;
				BigDecimal needMinusAmount = new BigDecimal(0);
				if (!pr.equals("3916")) {
					Local local = null;
					Set<String> costCenter = report.getLocals().keySet();
					for (String c : costCenter) {
						local = report.getLocals().get(c);
						if (local.getPr() != null
								&& local.getPr().equals(includePr)) {
							if (salaryType.getType() == Annuities.TYPE) {
								localAmount = local.getPublicAnnuitiesFund();
							} else if (salaryType.getType() == Hospitalization.TYPE) {
								localAmount = local
										.getPublicHospitalizationInsurance();
							} else if (salaryType.getType() == Accumulation.TYPE) {
								localAmount = local.getPublicAccumulationFund();
							} else if (salaryType.getType() == LostWork.TYPE) {
								localAmount = local.getPublicIdlenessFund();
							} else if (salaryType.getType() == WorkBreak.TYPE) {
								localAmount = local.getPublicWorkHurt();
							} else if (salaryType.getType() == Procreate.TYPE) {
								localAmount = local.getPublicProcreateFund();
							}
						}
						if (localAmount.doubleValue() != 0) {
							needMinusAmount = needMinusAmount.add(localAmount);
						}
					}
				}
				write.setNextNumbericData(other.subtract(needMinusAmount));
			}
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			if (pr.equals("3910")) {
				if (detailFile.indexOf("-HB-") > -1
						|| detailFile.indexOf("-SJZ-") > -1) {
					write.setNextStringData("3923");
				} else if (detailFile.indexOf("-TJ-") > -1) {
					write.setNextStringData("3920");
				} else if (detailFile.indexOf("-BJ-") > -1) {
					write.setNextStringData("3916");
				}
			} else {
				write.setNextStringData(salaryType.getSecondAssignment());
			}
			write.setNextStringData(salaryType.getText());
		} else {
			// ������Ҫ��������50ƾ֤���ֱ�ʹ��local��fesco���ܽ�����ʹ��һ��ʹ�����͵�assignment����һ��ʹ�����͵�secondAssignment
			write.createRow();
			write.setNextStringData((salaryType.getType() + 1) + "");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData("50");
			if (pr.equals("3910")) {
				write.setNextStringData("2173001");
			} else {
				write.setNextStringData(salaryType.getReverseChar());
			}

			// ȡ��Local���������ۼ�ȡ��local���ܽ��
			HashMap<String, Local> locals = report.getLocals();
			Collection<Local> l = locals.values();
			Iterator<Local> itl = l.iterator();
			Local local = null;
			BigDecimal annFundLocal = new BigDecimal(0);
			while (itl.hasNext()) {
				local = itl.next();
				// ����ǵ�ǰ��pr,�Ž����ۼ�
				if (local.getPr().equals(pr) || local.getPr().equals(includePr)) {
					if (salaryType.getType() == Annuities.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicAnnuitiesFund());
					} else if (salaryType.getType() == Hospitalization.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicHospitalizationInsurance());
					} else if (salaryType.getType() == Accumulation.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicAccumulationFund());
					} else if (salaryType.getType() == LostWork.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicIdlenessFund());
					} else if (salaryType.getType() == WorkBreak.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicWorkHurt());
					} else if (salaryType.getType() == Procreate.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicProcreateFund());
					}
				}
			}
			write.setNextNumbericData(annFundLocal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			if (pr.equals("3910")) {
				write.setNextStringData("3916");
			} else {
				write.setNextStringData(salaryType.getAssignment());
			}
			write.setNextStringData(salaryType.getText());

			write.createRow();
			write.setNextStringData((salaryType.getType() + 1) + "");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData("50");
			if (pr.equals("3910")) {
				write.setNextStringData("2173001");
			} else {
				write.setNextStringData(salaryType.getReverseChar());
			}
			// ȡ�����е�Fesco�����������ۼ�
			HashMap<String, FEsco> fescos = report.getFescos();
			Collection<FEsco> c = fescos.values();
			Iterator<FEsco> its = c.iterator();
			FEsco fEsco = null;
			BigDecimal annFund = new BigDecimal(0);
			while (its.hasNext()) {
				fEsco = its.next();
				// ����ǵ�ǰ��pr���Ž����ۼ�
				if (fEsco.getPt().equals(pr) || fEsco.getPt().equals(includePr)) {
					if (salaryType.getType() == Annuities.TYPE) {
						annFund = annFund.add(fEsco.getPublicAnnuitiesFund());
					} else if (salaryType.getType() == Hospitalization.TYPE) {
						annFund = annFund.add(fEsco
								.getPublicHospitalizationInsurance());
					} else if (salaryType.getType() == Accumulation.TYPE) {
						annFund = annFund
								.add(fEsco.getPublicAccumulationFund());
					} else if (salaryType.getType() == LostWork.TYPE) {
						annFund = annFund.add(fEsco.getPublicIdlenessFund());
					} else if (salaryType.getType() == WorkBreak.TYPE) {
						annFund = annFund.add(fEsco.getPublicWorkHurt());
					} else if (salaryType.getType() == Procreate.TYPE) {
						annFund = annFund.add(fEsco.getPublicProcreateFund());
					}
				}
			}
			write.setNextNumbericData(annFund);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			if (pr.equals("3910")) {
				if (detailFile.indexOf("-HB-") > -1
						|| detailFile.indexOf("-SJZ-") > -1) {
					write.setNextStringData("3923");
				} else if (detailFile.indexOf("-TJ-") > -1) {
					write.setNextStringData("3920");
				} else if (detailFile.indexOf("-BJ-") > -1) {
					write.setNextStringData("3916");
				}
			} else {
				write.setNextStringData(salaryType.getSecondAssignment());
			}
			write.setNextStringData(salaryType.getText());
		}
	}

	/**
	 * ����ʵ�����ʹ�����
	 */
	private void generatePraticeSalary() {
		write.createNewSheet("ʵ������");
		// ���ݵ�ǰpr��ȡ��pr��Ӧ����������
		String prName = RuntimeData.getInstance().getCompanyName(this.pr);
		String prCode = RuntimeData.getInstance().getCompanyCodeByPr(this.pr);
		// ���ɱ���
		this.generateIntegrationTitle();

		// ���ʶ���
		Summary summary = null;
		// ���ʽ��
		BigDecimal salary = new BigDecimal(0);
		// �мӰ�ѵĹ����б�
		List<Summary> otAllowances = new ArrayList<Summary>();
		// ����������Ĺ����б�
		List<Summary> otherBonus = new ArrayList<Summary>();
		// �а�ȫ���ٽ���Ĺ����б�
		List<Summary> saveRecallBonuses = new ArrayList<Summary>();
		// ��ȫ���ٽ����ܶ�
		BigDecimal saveRecallBonusesTotal = new BigDecimal(0);
		// ά�����۽����ܶ�
		BigDecimal maintenanceBonusTotal = new BigDecimal(0);
		// ά��Ч�ʽ����ܶ�
		BigDecimal maintenanceEfficiencyBonusTotal = new BigDecimal(0);
		// ���¡���ů���ܶ�
		BigDecimal heatAllowanceTotal = new BigDecimal(0);
		// ���۽����ܶ�
		BigDecimal salesBonusTotal = new BigDecimal(0);
		// ��װ�������ܶ�
		BigDecimal setupManagementBonusTotal = new BigDecimal(0);
		// ���콱���ܶ�
		BigDecimal rebuildBonusTotal = new BigDecimal(0);
		// �������ܶ�
		BigDecimal fixBonusTotal = new BigDecimal(0);
		// ������ܶ�
		BigDecimal labourUnionFundTotal = new BigDecimal(0);
		// ��������˰�ܶ�
		BigDecimal personOwnDutyTotal = new BigDecimal(0);
		// ����ʵ�������ܶ�
		BigDecimal factSalaryTotal = new BigDecimal(0);
		// ���̼�Ч���˽��ܶ�
		BigDecimal engineeringBonusTotal = new BigDecimal(0);
		// ���ṫ����
		BigDecimal selfAccumulationFundTotal = new BigDecimal(0);
		// �������ϱ���
		BigDecimal selfAnnuitiesFundTotal = new BigDecimal(0);
		// ����ҽ�Ʊ���
		BigDecimal selfHospitalizationInsuranceTotal = new BigDecimal(0);
		// ����ʧҵ����
		BigDecimal selfIdlenessFundTotal = new BigDecimal(0);
		// local�����ṫ����
		BigDecimal localSelfAccumulationFundTotal = new BigDecimal(0);
		// local���������ϱ���
		BigDecimal localSelfAnnuitiesFundTotal = new BigDecimal(0);
		// local������ҽ�Ʊ���
		BigDecimal localSelfHospitalizationInsuranceTotal = new BigDecimal(0);
		// local������ʧҵ����
		BigDecimal localSelfIdlenessFundTotal = new BigDecimal(0);
		// Ӧ���ʿ�
		BigDecimal accountReceivableTotal = new BigDecimal(0);

		// BigDecimal salaryNeedMinus = new BigDecimal(0);

		BigDecimal includePrTotal = new BigDecimal(0);

		// ������ǰpr�ӹ���ӳ��������ҵ����ʶ�����������ı��
		for (String s : this.prSummaryMapper.get(pr)) {
			// �����������ı��ȡ�ù��ʶ���
			summary = report.getSummarys().get(s);
			if (summary == null) {
				continue;
			}
			// ȡ�����ʶ���Ĺ��ʽ��
			salary = summary.getSalary();

			// Ϊÿһ�����ʶ�������һ��ƾ֤
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			// �������Ϊ����������40��������50
			if (salary.abs() == salary) {
				write.setNextStringData("40");
			} else {
				write.setNextStringData("50");
			}
			write.setNextStringData("5101001");

			write.setNextNumbericData(salary.abs());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(summary.getCostCenter());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + prName + this.month + "��Ա������");

			// ������ʶ������������Ϊ0�����ۼӵ����������б���
			if (summary.getOtherBonus().intValue() != 0) {
				otherBonus.add(summary);
			}
			// ������ʶ���ļӰ�Ѳ�Ϊ0�����ۼӵ��Ӱ���б���
			if (summary.getOtAllowance().intValue() != 0) {
				otAllowances.add(summary);
			}
			// �����ȫ���ٽ���Ϊ0���ѹ��ʶ���ӵ���ȫ���ٽ����б�������ۼӵ���ȫ���ٽ����ܶ���
			if (summary.getSaveRecallBonus().intValue() != 0) {
				saveRecallBonuses.add(summary);
				saveRecallBonusesTotal = saveRecallBonusesTotal.add(summary
						.getSaveRecallBonus());
			}
			// ���ά�����۽���Ϊ0�����ۼӵ�ά�����۽����ܶ���
			if (summary.getMaintenanceBonus().intValue() != 0) {
				maintenanceBonusTotal = maintenanceBonusTotal.add(summary
						.getMaintenanceBonus());
			}
			// ���ά��Ч�ʽ���Ϊ0�����ۼӵ�ά��Ч�ʽ����ܶ���
			if (summary.getMaintenanceEfficiencyBonus().intValue() != 0) {
				maintenanceEfficiencyBonusTotal = maintenanceEfficiencyBonusTotal
						.add(summary.getMaintenanceEfficiencyBonus());
			}
			// ������¡���ů�Ѳ�Ϊ0�����ۼӵ����¡���ů���ܶ���
			if (summary.getHeatAllowance().intValue() != 0) {
				heatAllowanceTotal = heatAllowanceTotal.add(summary
						.getHeatAllowance());
			}
			// ������۽���Ϊ0�����ۼӵ����۽����ܶ���
			if (summary.getSalesBonus().intValue() != 0) {
				salesBonusTotal = salesBonusTotal.add(summary.getSalesBonus());
			}
			// �����װ������Ϊ0�����ۼӵ���װ�������ܶ���
			if (summary.getSetupManagementBonus().intValue() != 0) {
				setupManagementBonusTotal = setupManagementBonusTotal
						.add(summary.getSetupManagementBonus());
			}
			// ������콱��Ϊ0�����ۼӵ����콱���ܶ���
			if (summary.getRebuildBonus().intValue() != 0) {
				// if(summary.)
				rebuildBonusTotal = rebuildBonusTotal.add(summary
						.getRebuildBonus());
			}
			// ���������Ϊ0�����ۼӵ��������ܶ���
			if (summary.getFixBonus().intValue() != 0) {
				fixBonusTotal = fixBonusTotal.add(summary.getFixBonus());
			}
			// �������Ѳ�Ϊ0�����ۼӵ�������ܶ���
			if (summary.getLabourUnionFund().intValue() != 0) {
				labourUnionFundTotal = labourUnionFundTotal.add(summary
						.getLabourUnionFund());
			}
			// �����������˰��Ϊ0�����ۼӵ���������˰�ܶ���
			if (summary.getPersonOwnDuty().intValue() != 0) {
				personOwnDutyTotal = personOwnDutyTotal.add(summary
						.getPersonOwnDuty());
			}
			// �������ʵ�����ʲ�Ϊ0�����ۼӵ�����ʵ�������ܶ���
			if (summary.getFactSalary().intValue() != 0) {
				factSalaryTotal = factSalaryTotal.add(summary.getFactSalary());
			}
			// ������̼�Ч���˽���Ϊ0�����ۼӵ����̼�Ч���˽��ܶ���
			if (summary.getEngineeringBonus().intValue() != 0) {
				engineeringBonusTotal = engineeringBonusTotal.add(summary
						.getEngineeringBonus());
			}

			if (summary.getSelfAccumulationFund().doubleValue() != 0) {
				selfAccumulationFundTotal = selfAccumulationFundTotal
						.add(summary.getSelfAccumulationFund());
			}

			if (summary.getSelfAnnuitiesFund().doubleValue() != 0) {
				selfAnnuitiesFundTotal = selfAnnuitiesFundTotal.add(summary
						.getSelfAnnuitiesFund());
			}

			if (summary.getSelfHospitalizationInsurance().doubleValue() != 0) {
				selfHospitalizationInsuranceTotal = selfHospitalizationInsuranceTotal
						.add(summary.getSelfHospitalizationInsurance());
			}

			if (summary.getSelfIdlenessFund().doubleValue() != 0) {
				selfIdlenessFundTotal = selfIdlenessFundTotal.add(summary
						.getSelfIdlenessFund());
			}

			if (summary.getAccountReceivable().doubleValue() != 0) {
				accountReceivableTotal = accountReceivableTotal.add(summary
						.getAccountReceivable());
			}
		}

		if (!this.includePr.equals(NO_INCLUDE_PR)) {
			for (String s : this.prSummaryMapper.get(this.includePr)) {
				summary = report.getSummarys().get(s);
				includePrTotal = includePrTotal.add(summary.getFactSalary())
						.add(summary.getSelfAccumulationFund())
						.add(summary.getSelfAnnuitiesFund())
						.add(summary.getSelfHospitalizationInsurance())
						.add(summary.getSelfIdlenessFund())
						.add(summary.getLabourUnionFund())
						.add(summary.getPersonOwnDuty());
				factSalaryTotal = factSalaryTotal.add(summary.getFactSalary());
				selfAccumulationFundTotal = selfAccumulationFundTotal
						.add(summary.getAccumulationFund());
				selfAnnuitiesFundTotal = selfAnnuitiesFundTotal.add(summary
						.getAnnuitiesFund());
				selfHospitalizationInsuranceTotal = selfHospitalizationInsuranceTotal
						.add(summary.getHospitalizationInsurance());
				selfIdlenessFundTotal = selfIdlenessFundTotal.add(summary
						.getSelfIdlenessFund());
				labourUnionFundTotal = labourUnionFundTotal.add(summary
						.getLabourUnionFund());
				personOwnDutyTotal = personOwnDutyTotal.add(summary
						.getPersonOwnDuty());
			}

			if (!this.pr.equals("3916")) {
				Set<String> localKeys = report.getLocals().keySet();
				Local local = null;
				for (String k : localKeys) {
					local = report.getLocals().get(k);
					if (local.getPr().equals(this.includePr)) {
						includePrTotal = includePrTotal
								.subtract(local.getAnnuitiesFund())
								.subtract(local.getAccumulationFund())
								.subtract(local.getHospitalizationInsurance())
								.subtract(local.getIdlenessFund());
						selfAccumulationFundTotal = selfAccumulationFundTotal
								.subtract(local.getAccumulationFund());
						selfAnnuitiesFundTotal = selfAnnuitiesFundTotal
								.subtract(local.getAnnuitiesFund());
						selfHospitalizationInsuranceTotal = selfHospitalizationInsuranceTotal
								.subtract(local.getHospitalizationInsurance());
						selfIdlenessFundTotal = selfIdlenessFundTotal
								.subtract(local.getIdlenessFund());
					}
				}
			}
		}

		// �����Ӱ�ѵĹ����б�
		System.out.println("otAllowances is :" + otAllowances.size());
		for (Summary s : otAllowances) {
			salary = s.getOtAllowance();
			// ÿһ���мӰ�ѵĹ��ʣ�������һ��ƾ֤
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			if (salary.abs() == salary) {
				write.setNextStringData("40");
			} else {
				write.setNextStringData("50");
			}
			write.setNextStringData("5101002");

			write.setNextNumbericData(salary.abs());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(s.getCostCenter());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + prName + this.month + "��Ա������-�Ӱ��");
		}

		// �������������б�
		System.out.println("otherBonus is :" + otherBonus.size());
		for (Summary s : otherBonus) {
			salary = s.getOtherBonus();
			// ����������Ĺ��ʶ�����һ��ƾ֤
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			if (salary.abs() == salary) {
				write.setNextStringData("40");
			} else {
				write.setNextStringData("50");
			}
			write.setNextStringData("5101001");

			write.setNextNumbericData(salary.abs());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(s.getCostCenter());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + prName + this.month + "��Ա������-��������");
		}

		System.out.println("saveRecallBonuses is :" + saveRecallBonuses.size());
		// �����ǰ���ɵ�pr��3916��������ȫ���ٽ����б�ÿ�����ʶ�����һ��ƾ֤
		if (this.pr.equals("3916")) {
			for (Summary s : saveRecallBonuses) {
				salary = s.getSaveRecallBonus();
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				if (salary.abs() == salary) {
					write.setNextStringData("40");
				} else {
					write.setNextStringData("50");
				}
				write.setNextStringData("5101105");

				write.setNextNumbericData(salary.abs());
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(s.getCostCenter());
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + prName + this.month
						+ "��Ա������-��ȫ���ٽ�");
			}
		} else {
			// ��������prֻ����һ��ƾ֤��ǰ���ǰ�ȫ���ٽ����ܶ����0
			if (saveRecallBonusesTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				if (saveRecallBonusesTotal.abs() == saveRecallBonusesTotal) {
					write.setNextStringData("40");
				} else {
					write.setNextStringData("50");
				}
				write.setNextStringData("2192061");

				write.setNextNumbericData(saveRecallBonusesTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.pr + " ���ٽ�Ԥ��");
				write.setNextStringData("����" + prName + this.month
						+ "��Ա������-��ȫ���ٽ�");
			}
		}

		System.out
				.println("maintenanceBonusTotal is :" + maintenanceBonusTotal);

		// ���ά�����۽�������0������ƾ֤
		if (maintenanceBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("2192061");

			write.setNextNumbericData(maintenanceBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " ������Ԥ��");
			write.setNextStringData("����" + prName + this.month + "��Ա������-ά�����۽�");
		}

		System.out.println("maintenanceEfficiencyBonusTotal is :"
				+ maintenanceEfficiencyBonusTotal);

		// ά��Ч�ʽ�
		if (maintenanceEfficiencyBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("2192061");

			write.setNextNumbericData(maintenanceEfficiencyBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " Ч�ʽ�Ԥ��");
			write.setNextStringData("����" + prName + this.month + "��Ա������-ά��Ч�ʽ�");
		}

		System.out.println("heatAllowanceTotal is :" + heatAllowanceTotal);

		// ���Ÿ���
		if (heatAllowanceTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("2192900");

			write.setNextNumbericData(heatAllowanceTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����");
			write.setNextStringData("����" + prName + this.month + "��Ա������-���·�");
		}

		System.out.println("salesBonusTotal is :" + salesBonusTotal);

		// ���۽��𣬲������ĸ�pr������Ҫ��һ��40��ƾ֤��ƾ֤���������۽�����ܽ�
		// �����Ҫ����4��ƾ֤���������3910��ֻ��һ��40ƾ֤��ƾ֤�Ľ�����ܽ��������3910��ÿһ�����۽���Ҫ��һ��40ƾ֤��
		// �������4����50ƾ֤��ƾ֤�Ľ������������ƾ֤�Ľ��
		if (salesBonusTotal.doubleValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("2181020");

			write.setNextNumbericData(salesBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(pr + " ����Ա����");
			write.setNextStringData("����" + prName + this.month + "��Ա������-����Ա����");
		}

		System.out.println("setupManagementBonusTotal is :"
				+ setupManagementBonusTotal);

		// ��װ����
		if (setupManagementBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("2181020");

			write.setNextNumbericData(setupManagementBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(pr + " ��װ����");
			write.setNextStringData("����" + prName + this.month + "��Ա������-��װ����");
		}

		System.out.println("rebuildBonusTotal is :" + rebuildBonusTotal);

		// ���콱��
		BigDecimal reBuildBonusTotal_L = new BigDecimal(0);
		// M���콱���ܶ�
		BigDecimal reBuildBonusTotal_M = new BigDecimal(0);
		BigDecimal reBuildBonusTotal = new BigDecimal(0);
		// ȡ�����еĸ��콱��
		Set<String> rebuildKey = report.getRebuildBonusDetails().keySet();
		RebuildBonusDetail rebuildDetail = null;
		BigDecimal rebuildBonus_M = new BigDecimal(0);
		BigDecimal rebuildBonus_L = new BigDecimal(0);
		HashMap<String, BigDecimal> reBuildBonusTotalMap_L = new HashMap<String, BigDecimal>();
		HashMap<String, BigDecimal> reBuildBonusTotalMap_M = new HashMap<String, BigDecimal>();
		// �������еĸ��콱��
		for (String s : rebuildKey) {
			// ȡ�ø��콱�����
			rebuildDetail = report.getRebuildBonusDetails().get(s);
			// ֻȡ��ǰpr�ĸ��콱�����
			if (rebuildDetail.getPr() == null
					|| !rebuildDetail.getPr().equals(pr)) {
				continue;
			}
			// ������콱������wbs����M��m��β
			if (rebuildDetail.getWbsNumber().endsWith("M")
					|| rebuildDetail.getWbsNumber().endsWith("m")) {
				// �Ѹ��콱�����Ľ���ۼӵ�M���콱���ܶ���
				reBuildBonusTotal_M = reBuildBonusTotal_M.add(rebuildDetail
						.getSum());
				// ȡ����ǰ���콱���wbs�ŵĸ��콱���ܶ�
				rebuildBonus_M = reBuildBonusTotalMap_M.get(rebuildDetail
						.getWbsNumber());
				// ���wbs�ŵĸ��콱���ܶ�Ϊ�գ��½�һ���ܶ�
				if (rebuildBonus_M == null) {
					rebuildBonus_M = new BigDecimal(0);
				}
				// �ѵ�ǰ�ĸ��콱���ܶ�ӵ���wbs�ŵ��ܶ���
				rebuildBonus_M = rebuildBonus_M.add(rebuildDetail.getSum());
				// �Ѵ�wbs�����¼ӻ�ȥ
				reBuildBonusTotalMap_M.put(rebuildDetail.getWbsNumber(),
						rebuildBonus_M);
				// ���������콱���ܶ���ϴ˴θ��콱����
				reBuildBonusTotal = reBuildBonusTotal.add(rebuildDetail
						.getSum());
			}
			// ������콱������wbs����L��l��β
			if (rebuildDetail.getWbsNumber().endsWith("L")
					|| rebuildDetail.getWbsNumber().endsWith("l")) {
				// ��L���콱���ܶ��ۼ�
				reBuildBonusTotal_L = reBuildBonusTotal_L.add(rebuildDetail
						.getSum());
				// ��������콱������wbs�Ÿ��콱���ܶ��ۼ�
				rebuildBonus_L = reBuildBonusTotalMap_L.get(rebuildDetail
						.getWbsNumber());
				if (rebuildBonus_L == null) {
					rebuildBonus_L = new BigDecimal(0);
				}
				rebuildBonus_L = rebuildBonus_L.add(rebuildDetail.getSum());
				reBuildBonusTotalMap_L.put(rebuildDetail.getWbsNumber(),
						rebuildBonus_L);
				// ���������콱���ܶ��ۼ�
				reBuildBonusTotal = reBuildBonusTotal.add(rebuildDetail
						.getSum());
			}
		}
		// ������콱���ܶΪ0
		if (rebuildBonusTotal.doubleValue() != 0) {
			// ���ɸ��콱���ܶ�40��50��ƾ֤
			// write.createRow();
			// write.setNextStringData("1");
			// write.setNextStringData(postingDate);
			// write.setNextStringData(postingDate);
			// write.setNextStringData("RMB");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("����" + this.month + "��Ա������");
			// write.setNextStringData("40");
			// write.setNextStringData("1113001");
			// write.setNextNumbericData(rebuildBonusTotal);
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("3910");
			// write.setNextStringData("HOת����" + prName + this.month + "�¸��콱��");
			//
			// write.createRow();
			// write.setNextStringData("1");
			// write.setNextStringData(postingDate);
			// write.setNextStringData(postingDate);
			// write.setNextStringData("RMB");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("����" + this.month + "��Ա������");
			// write.setNextStringData("50");
			// write.setNextStringData("2181020");
			// write.setNextNumbericData(rebuildBonusTotal);
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData(this.pr + " ���콱��");
			// write.setNextStringData("����" + prName + this.month +
			// "��Ա������-���콱��");
			// // �Ѹ��콱���ۼӵ���Ҫ��ȥ�Ĺ��ʽ��
			// salaryNeedMinus = salaryNeedMinus.add(rebuildBonusTotal);

			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("2181020");
			write.setNextNumbericData(rebuildBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " ���콱��");
			write.setNextStringData("����" + prName + this.month + "��Ա������-���콱��");

			// write.createRow();
			// write.setNextStringData("1");
			// write.setNextStringData(postingDate);
			// write.setNextStringData(postingDate);
			// write.setNextStringData("RMB");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("����" + this.month + "��Ա������");
			// write.setNextStringData("50");
			// write.setNextStringData("2192000");
			// write.setNextNumbericData(rebuildBonusTotal);
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData(this.pr + " ���콱��");
			// write.setNextStringData("����" + prName + this.month + "�¸��콱��");
		}

		// ����ƾ֤3����������

		// ���VO������ϸ�м�¼
		if (engineeringBonusTotal.doubleValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("2181020");
			write.setNextNumbericData(engineeringBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + "���۽���");
			write.setNextStringData("����" + prName + this.month
					+ "��Ա������-���̼�Ч���˽�VO����");
		}

		System.out.println("fixBonusTotal is :" + fixBonusTotal);

		// ��������������ܶΪ0����Ҫ��һ��ƾ֤
		if (fixBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("1113001");

			write.setNextNumbericData(fixBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("3910");
			write.setNextStringData("HOת" + this.month + "��" + prName
					+ "����������");
		}

		BigDecimal localAnnuitiesTotal = new BigDecimal(0);
		BigDecimal localHospitalizationTotal = new BigDecimal(0);
		BigDecimal localLostWorkTotal = new BigDecimal(0);
		BigDecimal localAccumulationTotal = new BigDecimal(0);
		// ȡ�����е�local
		HashMap<String, Local> localMap = report.getLocals();
		Local local = null;
		// ����local
		for (String l : localMap.keySet()) {
			local = localMap.get(l);
			// ���local��pr�ǵ�ǰ��pr�������ۼӡ�������ɵ���3916��Ҳ��Ҫ����includePr�����
			if (local.getPr().equals(pr)
					|| (this.pr.equals("3916") && local.getPr().equals(
							includePr))) {
				// �ۼ����Ͻ�
				localAnnuitiesTotal = localAnnuitiesTotal.add(local
						.getAnnuitiesFund());
				// �ۼ�ҽ�ƽ�
				localHospitalizationTotal = localHospitalizationTotal.add(local
						.getHospitalizationInsurance());
				// �ۼ�ʧҵ��
				localLostWorkTotal = localLostWorkTotal.add(local
						.getIdlenessFund());
				// �ۼӹ�����
				localAccumulationTotal = localAccumulationTotal.add(local
						.getAccumulationFund());
			}
		}

		System.out.println("localAnnuitiesTotal is :" + localAnnuitiesTotal);
		// ������ɵ���3910����Ҫ�������ϱ��յ���˰
		if (!this.pr.equals("3910")) {
			// ������Ͻ��ܶΪ0����Ҫ��һ��50��ƾ֤
			if (localAnnuitiesTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181050");

				write.setNextNumbericData(localAnnuitiesTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "������");
				write.setNextStringData("����" + prName + this.month + "��Ա�����ϱ���");
			}

			System.out.println("localHospitalizationTotal is :"
					+ localHospitalizationTotal);
			// ���ҽ�ƽ��ܶΪ0����Ҫ��һ��50��ƾ֤
			if (localHospitalizationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181070");

				write.setNextNumbericData(localHospitalizationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "��ҽ��");
				write.setNextStringData("����" + prName + this.month + "��Ա��ҽ�Ʊ���");
			}

			System.out.println("localLostWorkTotal is :" + localLostWorkTotal);
			// ���ʧҵ���ܶΪ0����Ҫ��һ��50��ƾ֤
			if (localLostWorkTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181090");

				write.setNextNumbericData(localLostWorkTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "��ʧҵ");
				write.setNextStringData("����" + prName + this.month + "��Ա��ʧҵ����");
			}

			System.out.println("localAccumulationTotal is :"
					+ localAccumulationTotal);
			// ����������ܶΪ0����Ҫ��һ��50��ƾ֤
			if (localAccumulationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181100");

				write.setNextNumbericData(localAccumulationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "�¹�����");
				write.setNextStringData("����" + prName + this.month + "��Ա��ס��������");
			}

			BigDecimal fescoAnnuitiesTotal = new BigDecimal(0);
			BigDecimal fescoHospitalizationTotal = new BigDecimal(0);
			BigDecimal fescoLostWorkTotal = new BigDecimal(0);
			BigDecimal fescoAccumulationTotal = new BigDecimal(0);
			// ȡ�����е�fesco
			HashMap<String, FEsco> fescoMap = report.getFescos();
			FEsco fesco = null;
			// ����fesco
			for (String f : fescoMap.keySet()) {
				fesco = fescoMap.get(f);
				// ֻ��fesco�ǵ�ǰpr�ģ������ۼӣ����������3916������Ҫ����includePr����
				if (fesco.getPt().equals(pr) || fesco.getPt().equals(includePr)) {
					// �ۼ����Ͻ�
					fescoAnnuitiesTotal = fescoAnnuitiesTotal.add(fesco
							.getAnnuitiesFund());
					// �ۼ�ҽ�ƽ�
					fescoHospitalizationTotal = fescoHospitalizationTotal
							.add(fesco.getHospitalizationInsurance());
					// �ۼ�ʧҵ��
					fescoLostWorkTotal = fescoLostWorkTotal.add(fesco
							.getIdlenessFund());
					// �ۼӹ�����
					fescoAccumulationTotal = fescoAccumulationTotal.add(fesco
							.getAccumulationFund());
				}
			}

			System.out
					.println("fescoAnnuitiesTotal is :" + fescoAnnuitiesTotal);
			// ������Ͻ�Ϊ0����Ҫ��һ���Ϻ������50ƾ֤
			if (fescoAnnuitiesTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181050");

				write.setNextNumbericData(fescoAnnuitiesTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "���Ϻ��������");
				write.setNextStringData("����" + prName + this.month + "��Ա�����ϱ���");
			}

			System.out.println("fescoHospitalizationTotal is :"
					+ fescoHospitalizationTotal);
			// ���ҽ�ƽ�Ϊ0����Ҫ��һ���Ϻ������50ƾ֤
			if (fescoHospitalizationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181070");

				write.setNextNumbericData(fescoHospitalizationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "���Ϻ����ҽ��");
				write.setNextStringData("����" + prName + this.month + "��Ա��ҽ�Ʊ���");
			}

			System.out.println("fescoLostWorkTotal is :" + fescoLostWorkTotal);
			// ���ʧҵ��Ϊ0����Ҫ��һ���Ϻ������50ƾ֤
			if (fescoLostWorkTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181090");

				write.setNextNumbericData(fescoLostWorkTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "���Ϻ����ʧҵ");
				write.setNextStringData("����" + prName + this.month + "��Ա��ʧҵ����");
			}

			System.out.println("fescoAccumulationTotal is :"
					+ fescoAccumulationTotal);
			// ���������Ϊ0����Ҫ��һ���Ϻ������50ƾ֤
			if (fescoAccumulationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181100");

				write.setNextNumbericData(fescoAccumulationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "���Ϻ����������");
				write.setNextStringData("����" + prName + this.month + "��Ա��ס��������");
			}

			System.out.println("labourUnionFundTotal is :"
					+ labourUnionFundTotal);

			// ���˹��ᾭ�Ѳ�Ϊ0����һ��50ƾ֤
			if (labourUnionFundTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2181140");

				write.setNextNumbericData(labourUnionFundTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "�¸��˹��ᾭ��");
				write.setNextStringData("����" + prName + this.month + "��Ա�����ᾭ��");
			}

			System.out.println("personOwnDutyTotal is :" + personOwnDutyTotal);

			// ��˰��Ϊ0����һ��50ƾ֤
			if (personOwnDutyTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				write.setNextStringData("2187001");

				write.setNextNumbericData(personOwnDutyTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "�¸�˰");
				write.setNextStringData("����" + prName + this.month + "��Ա����˰");
			}
		}

		System.out.println("factSalaryTotal is :" + factSalaryTotal);

		// �����Ҫ������ʵ�������ܶΪ0������3920��3923ʱ����Ҫ��3910��ʵ���ܶ���һ��40
		if (includePrTotal.doubleValue() > 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			write.setNextStringData("1113001");

			write.setNextNumbericData(includePrTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(includePr);
			if (this.pr.equals("3916")) {
				write.setNextStringData("BJתHO����" + this.month
						+ "��Ա�����ʼ�����Ա����˰�����Ϻ���������ر���");
			} else {
				write.setNextStringData(prCode + "תHO����" + prName + this.month
						+ "��Ա�����ʼ����۱��չ����˰");
			}
		}
		
		// �������Ӧ���˻���Ϊ0������һ��40
		if (accountReceivableTotal.doubleValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("40");
			if (this.pr.equals("3910")) {
				write.setNextStringData("2181021");
			} else {
				write.setNextStringData("1113001");
			}

			write.setNextNumbericData(accountReceivableTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			if(this.pr.equals("3910")) {
				write.setNextStringData("3910AR����");
				write.setNextStringData("����FON" + this.month + "��Ա������-Ӧ���˿");
			} else {
				write.setNextStringData("3910");
				write.setNextStringData("����" + prName + this.month + "��Ա������-Ӧ���˿��");
			}
		}

		// ����ʵ�������ܶ����includePr�ģ���Ϊ0����һ��50ƾ֤
		// factSalaryTotal = factSalaryTotal.subtract(salaryNeedMinus);
		// factSalaryTotal = factSalaryTotal.add(includePrTotal);
		BigDecimal needSubtractLocal = new BigDecimal(0);
		if (this.pr.equals("3910")) {
			// ������ɵ���3910��ʵ��������Ҫ�����������ա����ᡢ�������ã����Ҽ�ȥlocal������
			// ��Ҫ����һ��ƾ֤��ȡlocal��������һ��50
			factSalaryTotal = factSalaryTotal.add(selfAnnuitiesFundTotal)
					.add(selfAccumulationFundTotal)
					.add(selfHospitalizationInsuranceTotal)
					.add(selfIdlenessFundTotal).add(labourUnionFundTotal)
					.add(personOwnDutyTotal);

			Set<String> localKeys = report.getLocals().keySet();

			for (String k : localKeys) {
				local = report.getLocals().get(k);
				if (local.getPr().equals(this.pr)) {
					factSalaryTotal = factSalaryTotal
							.subtract(local.getAccumulationFund())
							.subtract(local.getAnnuitiesFund())
							.subtract(local.getHospitalizationInsurance())
							.subtract(local.getIdlenessFund());
				}
				needSubtractLocal = needSubtractLocal
						.add(local.getAccumulationFund())
						.add(local.getAnnuitiesFund())
						.add(local.getHospitalizationInsurance())
						.add(local.getIdlenessFund());
			}
		}
		if (factSalaryTotal.doubleValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("50");
			if (this.pr.equals("3910")) {
				write.setNextStringData("2173001");
			} else {
				write.setNextStringData("2181000");
			}

			write.setNextNumbericData(factSalaryTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			if (this.pr.equals("3910")) {
				if (detailFile.indexOf("-HB-") > -1
						|| detailFile.indexOf("-SJZ-") > -1) {
					write.setNextStringData("3923");
				} else if (detailFile.indexOf("-TJ-") > -1) {
					write.setNextStringData("3920");
				} else if (detailFile.indexOf("-BJ-") > -1) {
					write.setNextStringData("3916");
				}
			} else {
				write.setNextStringData(this.month + "�¹���");
			}

			if (this.pr.equals("3910")) {
				write.setNextStringData(RuntimeData.getInstance()
						.getCompanyCodeByPr(getRealCode())
						+ "תHO����"
						+ prName
						+ this.month + "��Ա������");

				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������");
				write.setNextStringData("50");
				if (this.pr.equals("3910")) {
					write.setNextStringData("2173001");
				} else {
					write.setNextStringData("2181000");
				}

				write.setNextNumbericData(needSubtractLocal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("3916");
				write.setNextStringData("BJתHO(FON)" + this.month + "�´��۱������ر���");
			} else {
				write.setNextStringData("����" + prName + this.month + "��Ա������");
			}
		}

		// ������ɵ���3910�������ɷ�"1"��ƾ֤��ֱ�ӷ���
		if (this.pr.equals("3910")) {
			return;
		}
		// ��װƾ֤2 ����ϸ���а�װ����ϸ����pr���˳���������ǵ�ǰpr����ͳ�Ƴ���
		// �����װ�����ܶ����0
		if (setupManagementBonusTotal.doubleValue() != 0) {
			BigDecimal prSetupManageAmount = new BigDecimal(0);
			if (setupManageBonusDetailMapper.get(pr) != null
					&& setupManageBonusDetailMapper.get(pr).size() > 0) {
				// ����
				for (SetupManageBonusDetail detail : setupManageBonusDetailMapper
						.get(pr)) {
					// �����ϸ����Ľ�����0������
					if (detail.getAmount().doubleValue() == 0) {
						continue;
					}

					// Ϊÿһ����װ������һ��ƾ֤
					write.createRow();
					write.setNextStringData("2");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("����" + this.month + "��Ա������-��װ������");
					if (detail.getAmount().intValue() < 0) {
						write.setNextStringData("50");
					} else {
						write.setNextStringData("40");
					}
					write.setNextStringData("4291302");
					write.setNextNumbericData(detail.getAmount());
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(detail.getCode());
					write.setNextStringData("");
					write.setNextStringData("����" + prName + this.month
							+ "��Ա������-��װ������");
					prSetupManageAmount = prSetupManageAmount.add(detail
							.getAmount());
				}

			}
			BigDecimal otherSetupManageTotal = null;
			Set<String> setupManageKey = setupManageBonusDetailMapper.keySet();
			List<SetupManageBonusDetail> setupManageList = null;
			for (String setupKey : setupManageKey) {
				if (setupKey.equals(pr)) {
					continue;
				}
				setupManageList = setupManageBonusDetailMapper.get(setupKey);
				otherSetupManageTotal = new BigDecimal(0);
				for (SetupManageBonusDetail d : setupManageList) {
					if (d.getAmount().doubleValue() != 0) {
						otherSetupManageTotal = otherSetupManageTotal.add(d
								.getAmount());
					}
				}
				if (otherSetupManageTotal.doubleValue() != 0) {
					write.createRow();
					write.setNextStringData("2");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("����" + this.month + "��Ա������-��װ������");
					if (setupManagementBonusTotal.doubleValue() > prSetupManageAmount
							.doubleValue()) {
						write.setNextStringData("40");
						write.setNextStringData("1113001");
					} else {
						write.setNextStringData("50");
						write.setNextStringData("2173001");
					}
					write.setNextNumbericData(otherSetupManageTotal);
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(setupKey + " ��װ����");
					write.setNextStringData("����" + prName + this.month
							+ "��Ա������-��װ������");
				}
			}

			write.createRow();
			write.setNextStringData("2");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������-��װ������");
			if (setupManagementBonusTotal.doubleValue() > 0) {
				write.setNextStringData("50");
			} else {
				write.setNextStringData("40");
			}
			write.setNextStringData("2181020");
			write.setNextNumbericData(setupManagementBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " ��װ����");
			write.setNextStringData("����" + prName + this.month + "��Ա������-��װ������");
		}

		// ����ƾ֤3
		// �����M��ͷ�ĸ������
		// if (reBuildBonusTotalMap_M.size() > 0) {
		// // Ϊÿһ��M��ͷ�ĸ��콱��һ��40ƾ֤
		// for (String wbsNo : reBuildBonusTotalMap_M.keySet()) {
		// write.createRow();
		// write.setNextStringData("3");
		// write.setNextStringData(postingDate);
		// write.setNextStringData(postingDate);
		// write.setNextStringData("RMB");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("����" + this.month + "��Ա������-���콱��");
		// write.setNextStringData("40");
		// write.setNextStringData("4291301");
		// write.setNextNumbericData(reBuildBonusTotalMap_M.get(wbsNo));
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData(wbsNo);
		// write.setNextStringData("");
		// write.setNextStringData("����" + prName + this.month
		// + "��Ա������-���콱��");
		// }
		// }
		//
		// // �����L��ͷ�ĸ��콱
		// if (reBuildBonusTotalMap_L.size() > 0) {
		// // Ϊÿһ��L��ͷ�ĸ�����һ��40ƾ֤
		// for (String wbsNo : reBuildBonusTotalMap_L.keySet()) {
		// write.createRow();
		// write.setNextStringData("3");
		// write.setNextStringData(postingDate);
		// write.setNextStringData(postingDate);
		// write.setNextStringData("RMB");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("����" + this.month + "��Ա������-���콱��");
		// write.setNextStringData("40");
		// write.setNextStringData("4291301");
		// write.setNextNumbericData(reBuildBonusTotalMap_L.get(wbsNo));
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData(wbsNo);
		// write.setNextStringData("");
		// write.setNextStringData("����" + prName + this.month
		// + "��Ա������-���콱��");
		// }
		// }

		// ����������첻����0����һ���ܵ�50ƾ֤
		if (rebuildBonusTotal.doubleValue() != 0) {
			BigDecimal prRebuildBonusAmount = new BigDecimal(0);
			if (rebuildBonusDetailMapper.get(pr) != null
					&& rebuildBonusDetailMapper.get(pr).size() > 0) {
				// ����
				for (RebuildBonusDetail detail : rebuildBonusDetailMapper
						.get(pr)) {
					// �����ϸ����Ľ�����0������
					if (detail.getSum().doubleValue() == 0) {
						continue;
					}

					// Ϊÿһ�����콱��һ��ƾ֤
					write.createRow();
					write.setNextStringData("3");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("����" + this.month + "��Ա������-���콱��");
					if (detail.getSum().doubleValue() < 0) {
						write.setNextStringData("50");
					} else {
						write.setNextStringData("40");
					}
					write.setNextStringData("4291301");
					write.setNextNumbericData(detail.getSum());
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(detail.getWbsNumber());
					write.setNextStringData("");
					write.setNextStringData("����" + prName + this.month
							+ "��Ա������-���콱��");

					prRebuildBonusAmount = prRebuildBonusAmount.add(detail
							.getSum());
				}
			}

			BigDecimal otherRebuildBonusAmount = null;
			List<RebuildBonusDetail> otherRebuildBonus = null;
			Set<String> rebuildBonusKey = rebuildBonusDetailMapper.keySet();
			for (String r : rebuildBonusKey) {
				if (r.equals(pr)) {
					continue;
				}
				otherRebuildBonusAmount = new BigDecimal(0);
				otherRebuildBonus = rebuildBonusDetailMapper.get(r);
				for (RebuildBonusDetail rebuildBonusDetial : otherRebuildBonus) {
					otherRebuildBonusAmount = otherRebuildBonusAmount
							.add(rebuildBonusDetial.getSum());
				}

				if (otherRebuildBonusAmount.doubleValue() != 0) {
					write.createRow();
					write.setNextStringData("3");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("����" + this.month + "��Ա������-���콱��");
					if (rebuildBonusTotal.doubleValue() < prRebuildBonusAmount
							.doubleValue()) {
						write.setNextStringData("50");
						write.setNextStringData("2173001");
					} else {
						write.setNextStringData("40");
						write.setNextStringData("1113001");
					}
					write.setNextNumbericData(otherRebuildBonusAmount);
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					if (rebuildBonusTotal.doubleValue() < prRebuildBonusAmount
							.doubleValue()) {
						write.setNextStringData(r + "���콱��");
					} else {
						write.setNextStringData(r);
					}
					write.setNextStringData("����" + prName + this.month
							+ "��Ա������-���콱��");
				}
			}
			write.createRow();
			write.setNextStringData("3");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������-���콱��");
			write.setNextStringData("50");
			write.setNextStringData("2181020");
			write.setNextNumbericData(rebuildBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(pr + "���콱��");
			write.setNextStringData("����" + prName + this.month + "��Ա������-���콱��");

		}

		// ƾ֤4
		// �����۽����"4"
		if (salesBonusTotal.doubleValue() != 0) {
			BigDecimal prSaleBonusAmount = new BigDecimal(0);
			List<SaleBonusDetail> prSaleBonusDetails = saleBonusDetailMapper
					.get(pr);
			if (prSaleBonusDetails != null && prSaleBonusDetails.size() > 0) {
				for (SaleBonusDetail s : prSaleBonusDetails) {
					if (s.getSum().doubleValue() != 0) {
						write.createRow();
						write.setNextStringData("4");
						write.setNextStringData(postingDate);
						write.setNextStringData(postingDate);
						write.setNextStringData("RMB");
						write.setNextStringData("");
						write.setNextStringData("");
						write.setNextStringData("����" + this.month
								+ "��Ա������-���۽���");
						if (s.getSum().doubleValue() > 0) {
							write.setNextStringData("40");
						} else {
							write.setNextStringData("50");
						}
						write.setNextStringData("4291301");
						write.setNextNumbericData(s.getSum());
						write.setNextStringData("");
						write.setNextStringData("");
						write.setNextStringData("");
						write.setNextStringData("");
						write.setNextStringData(s.getContractNum());
						write.setNextStringData("");
						write.setNextStringData("����" + prName + this.month
								+ "��Ա������-���۽���");
						prSaleBonusAmount = prSaleBonusAmount.add(s.getSum());
					}
				}
			}

			BigDecimal otherSaleBunusTotal = null;
			Set<String> otherSaleBonusPr = saleBonusDetailMapper.keySet();
			for (String p : otherSaleBonusPr) {
				if (p.equals(pr)) {
					continue;
				}
				prSaleBonusDetails = saleBonusDetailMapper.get(p);
				otherSaleBunusTotal = new BigDecimal(0);
				for (SaleBonusDetail s : prSaleBonusDetails) {
					if (s.getSum().doubleValue() != 0) {
						otherSaleBunusTotal = otherSaleBunusTotal.add(s
								.getSum());
					}
				}
				if (otherSaleBunusTotal.doubleValue() != 0) {
					write.createRow();
					write.setNextStringData("4");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("����" + this.month + "��Ա������-���۽���");
					if (salesBonusTotal.doubleValue() > prSaleBonusAmount
							.doubleValue()) {
						write.setNextStringData("40");
						write.setNextStringData("1113001");
					} else {
						write.setNextStringData("50");
						write.setNextStringData("2173001");
					}
					write.setNextNumbericData(otherSaleBunusTotal);
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(p);
					write.setNextStringData("����" + prName + this.month
							+ "��Ա������-���۽���");
				}
			}

			if (salesBonusTotal.doubleValue() != 0) {
				write.createRow();
				write.setNextStringData("4");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("����" + this.month + "��Ա������-���۽���");
				if (salesBonusTotal.doubleValue() > 0) {
					write.setNextStringData("50");
				} else {
					write.setNextStringData("40");
				}
				write.setNextStringData("2181020");
				write.setNextNumbericData(salesBonusTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(pr + "����Ա����");
				write.setNextStringData("����" + prName + this.month
						+ "��Ա������-���۽���");
			}
		}

		// ƾ֤5
		// VO����

		// ������̼�Ч���˽��ܶΪ0
		if (engineeringBonusTotal.intValue() != 0) {
			BigDecimal prVOBonusDetailAmount = new BigDecimal(0);
			if (voBonusDetailMapper.get(pr) != null
					&& voBonusDetailMapper.get(pr).size() > 0) {
				// ����VO������ϸ
				for (VOBonusDetail voBonusDetail : voBonusDetailMapper.get(pr)) {
					// Ϊÿһ��VO������ϸ����40ƾ֤
					write.createRow();
					write.setNextStringData("5");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("����" + this.month + "��Ա������");
					if (voBonusDetail.getAmount().doubleValue() > 0) {
						write.setNextStringData("40");
					} else {
						write.setNextStringData("50");
					}
					write.setNextStringData("4291301");
					write.setNextNumbericData(voBonusDetail.getAmount());
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(voBonusDetail.getWbsNo());
					write.setNextStringData("");
					write.setNextStringData("����" + prName + this.month
							+ "��Ա������-���̼�Ч���˽�VO����");
					// ��VO������ϸ����ۼ�
					prVOBonusDetailAmount = prVOBonusDetailAmount
							.add(voBonusDetail.getAmount());
				}
			}

			BigDecimal otherVOBonusAmount = null;
			List<VOBonusDetail> voBonusList = null;
			Set<String> voBonusKeys = voBonusDetailMapper.keySet();
			for (String voBonusKey : voBonusKeys) {
				if (voBonusKey.equals(pr)) {
					continue;
				}
				otherVOBonusAmount = new BigDecimal(0);
				voBonusList = voBonusDetailMapper.get(voBonusKey);
				for (VOBonusDetail otherVoBonusDetail : voBonusList) {
					otherVOBonusAmount = otherVOBonusAmount
							.add(otherVoBonusDetail.getAmount());
				}
				if (otherVOBonusAmount.doubleValue() != 0) {
					write.createRow();
					write.setNextStringData("5");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("����" + this.month + "��Ա������");
					if (engineeringBonusTotal.doubleValue() > prVOBonusDetailAmount
							.doubleValue()) {
						write.setNextStringData("40");
						write.setNextStringData("1113001");
					} else {
						write.setNextStringData("50");
						write.setNextStringData("2173001");
					}
					write.setNextNumbericData(otherVOBonusAmount);
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData(voBonusKey);
					write.setNextStringData("����" + prName + this.month
							+ "��Ա������-���̼�Ч���˽�VO����");
				}
			}
			write.createRow();
			write.setNextStringData("5");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("����" + this.month + "��Ա������");
			write.setNextStringData("50");
			write.setNextStringData("2181020");
			write.setNextNumbericData(engineeringBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " ����Ա����");
			write.setNextStringData("����" + prName + this.month
					+ "��Ա������-���̼�Ч���˽�VO����");
			// �ѹ��̼�Ч���˽��ӵ���Ҫ��ȥ�Ĺ��ʽ����
			// salaryNeedMinus = salaryNeedMinus.add(engineeringBonusTotal);

		}

	}

	private void generate3910CA() {
		write.createNewSheet("3916-3910����");
		this.generateIntegrationTitle();
		BigDecimal publicAccumulationFundAmount = new BigDecimal(0);
		BigDecimal publicAnnuitiesFundAmount = new BigDecimal(0);
		BigDecimal publicHospitalizationInsuranceAmount = new BigDecimal(0);
		BigDecimal publicIdlenessFundAmount = new BigDecimal(0);
		BigDecimal publicProcreateFundAmount = new BigDecimal(0);
		BigDecimal publicWorkHurtAmount = new BigDecimal(0);
		Local local = null;
		Set<String> localKeys = report.getLocals().keySet();

		for (String k : localKeys) {
			local = report.getLocals().get(k);
			publicAccumulationFundAmount = publicAccumulationFundAmount
					.add(local.getAccumulationFund());
			publicAnnuitiesFundAmount = publicAnnuitiesFundAmount.add(local
					.getPublicAnnuitiesFund());
			publicHospitalizationInsuranceAmount = publicHospitalizationInsuranceAmount
					.add(local.getPublicHospitalizationInsurance());
			publicIdlenessFundAmount = publicIdlenessFundAmount.add(local
					.getPublicIdlenessFund());
			publicProcreateFundAmount = publicProcreateFundAmount.add(local
					.getPublicProcreateFund());
			publicWorkHurtAmount = publicWorkHurtAmount.add(local
					.getPublicWorkHurt());
		}

		// ס��������
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "��ס��������");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicAccumulationFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("����" + this.month + "��ס��������");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "��ס��������");
		write.setNextStringData("50");
		write.setNextStringData("2181100");
		write.setNextNumbericData(publicAccumulationFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "�¹�����");
		write.setNextStringData("����" + this.month + "��ס��������");

		// ���Ͻ�
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "�����ϱ���");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicAnnuitiesFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("����" + this.month + "�����ϱ���");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "�����ϱ���");
		write.setNextStringData("50");
		write.setNextStringData("2181050");
		write.setNextNumbericData(publicAnnuitiesFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "�����ϱ���");
		write.setNextStringData("����" + this.month + "�����ϱ���");

		// ҽ�ƽ�
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "��ҽ�Ʊ���");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicHospitalizationInsuranceAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("����" + this.month + "��ҽ�Ʊ���");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "��ҽ�Ʊ���");
		write.setNextStringData("50");
		write.setNextStringData("2181070");
		write.setNextNumbericData(publicHospitalizationInsuranceAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "��ҽ�Ʊ���");
		write.setNextStringData("����" + this.month + "��ҽ�Ʊ���");

		// ʧҵ��
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "��ʧҵ����");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicIdlenessFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("����" + this.month + "��ʧҵ����");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "��ʧҵ����");
		write.setNextStringData("50");
		write.setNextStringData("2181090");
		write.setNextNumbericData(publicIdlenessFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "��ʧҵ����");
		write.setNextStringData("����" + this.month + "��ʧҵ����");

		// ������
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "����������");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicProcreateFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("����" + this.month + "����������");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "����������");
		write.setNextStringData("50");
		write.setNextStringData("2181902");
		write.setNextNumbericData(publicProcreateFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "����������");
		write.setNextStringData("����" + this.month + "����������");

		// ���˽�
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "�¹��˱���");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicWorkHurtAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("����" + this.month + "�¹��˱���");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("����" + this.month + "�¹��˱���");
		write.setNextStringData("50");
		write.setNextStringData("2181110");
		write.setNextNumbericData(publicWorkHurtAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "�¹��˱���");
		write.setNextStringData("����" + this.month + "�¹��˱���");
	}

	private void generateApart() {
		this.generateDetail(new LabourUnion(month + ""));
		this.generateDetail(new Annuities(month + ""));
		this.generateDetail(new Hospitalization(month + ""));
		this.generateDetail(new Accumulation(month + ""));
		this.generateDetail(new LostWork(month + ""));
		this.generateDetail(new WorkBreak(month + ""));
		this.generateDetail(new Procreate(month + ""));
		this.generateDetail(new SHManageFee(month + ""));
	}

	private void generateTitle() {
		write.createRow();
		write.setNextStringData("Chart of Account");
		write.setNextStringData("");
		write.setNextStringData("D/C");
		write.setNextStringData("Debit");
		write.setNextStringData("Credit");
		write.setNextStringData("Profit Centre");
		write.setNextStringData("Cost Centre");
		write.setNextStringData("");
		write.setNextStringData("WBS Element");
		write.setNextStringData("Order No.");
		write.setNextStringData("Assignment No.");
		write.setNextStringData("Tax Code");
		write.setNextStringData("Text");
		write.setNextStringData("Company Code");
	}

	private void generateDetail(SalaryType salaryType) {
		write.createNewSheet(salaryType.getTypeName());
		this.generateTitle();

		Summary summary = null;
		BigDecimal amount = new BigDecimal(0);
		BigDecimal other = new BigDecimal(0);
		for (String s : this.prSummaryMapper.get(pr)) {
			summary = report.getSummarys().get(s);
			if (summary == null) {
				continue;
			}
			write.createRow();
			write.setNextStringData(salaryType.getChart());
			write.setNextStringData(RuntimeData.getInstance().getSOANameByCode(
					salaryType.getChart()));
			write.setNextStringData("");
			if (salaryType.getType() == LabourUnion.TYPE) {
				amount = summary.getLabourUnionBaseFund();
			} else if (salaryType.getType() == Annuities.TYPE) {
				amount = summary.getAnnuitiesFund();
			} else if (salaryType.getType() == Hospitalization.TYPE) {
				amount = summary.getHospitalizationInsurance();
			} else if (salaryType.getType() == Accumulation.TYPE) {
				amount = summary.getAccumulationFund();
			} else if (salaryType.getType() == LostWork.TYPE) {
				amount = summary.getLostWorkFund();
			} else if (salaryType.getType() == WorkBreak.TYPE) {
				amount = summary.getWorkBreakFund();
			} else if (salaryType.getType() == Procreate.TYPE) {
				amount = summary.getProcreateFund();
			} else {
				amount = summary.getOverheadExpenses();
			}
			write.setNextNumbericData(amount);

			write.setNextStringData("");
			write.setNextStringData(summary.getPr());
			write.setNextStringData(summary.getCostCenter());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData(COMPANY_CODE);
			other = other.add(amount);
		}

		if (!pr.equals("3916") || salaryType.getType() < 1
				|| salaryType.getType() > 6) {
			write.createRow();
			write.setNextStringData(salaryType.getReverseChar());
			write.setNextStringData(RuntimeData.getInstance().getSOANameByCode(
					salaryType.getReverseChar()));
			write.setNextStringData("");
			write.setNextNumbericData(other.negate());
			write.setNextStringData("");
			write.setNextStringData(summary.getPr());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getAssignment());
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData(COMPANY_CODE);
		} else {
			write.createRow();
			write.setNextStringData(salaryType.getReverseChar());
			write.setNextStringData(RuntimeData.getInstance().getSOANameByCode(
					salaryType.getReverseChar()));
			write.setNextStringData("");

			HashMap<String, Local> locals = report.getLocals();
			Collection<Local> l = locals.values();
			Iterator<Local> itl = l.iterator();
			Local local = null;
			BigDecimal annFundLocal = new BigDecimal(0);
			while (itl.hasNext()) {
				local = itl.next();
				if (local.getPr().equals(pr)) {
					if (salaryType.getType() == Annuities.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicAnnuitiesFund());
					} else if (salaryType.getType() == Hospitalization.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicHospitalizationInsurance());
					} else if (salaryType.getType() == Accumulation.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicAccumulationFund());
					} else if (salaryType.getType() == LostWork.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicIdlenessFund());
					} else if (salaryType.getType() == WorkBreak.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicWorkHurt());
					} else if (salaryType.getType() == Procreate.TYPE) {
						annFundLocal = annFundLocal.add(local
								.getPublicProcreateFund());
					}
				}
			}
			write.setNextNumbericData(annFundLocal.negate());

			write.setNextStringData("");
			write.setNextStringData(summary.getPr());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getAssignment());
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData(COMPANY_CODE);

			write.createRow();
			write.setNextStringData(salaryType.getReverseChar());
			write.setNextStringData(RuntimeData.getInstance()
					.getSOAByCode(salaryType.getReverseChar()).getName());
			write.setNextStringData("");

			HashMap<String, FEsco> fescos = report.getFescos();
			Collection<FEsco> c = fescos.values();
			Iterator<FEsco> its = c.iterator();
			FEsco fEsco = null;
			BigDecimal annFund = new BigDecimal(0);
			while (its.hasNext()) {
				fEsco = its.next();
				if (fEsco.getPt().equals(pr)) {
					if (salaryType.getType() == Annuities.TYPE) {
						annFund = annFund.add(fEsco.getPublicAnnuitiesFund());
					} else if (salaryType.getType() == Hospitalization.TYPE) {
						annFund = annFund.add(fEsco
								.getPublicHospitalizationInsurance());
					} else if (salaryType.getType() == Accumulation.TYPE) {
						annFund = annFund
								.add(fEsco.getPublicAccumulationFund());
					} else if (salaryType.getType() == LostWork.TYPE) {
						annFund = annFund.add(fEsco.getPublicIdlenessFund());
					} else if (salaryType.getType() == WorkBreak.TYPE) {
						annFund = annFund.add(fEsco.getPublicWorkHurt());
					} else if (salaryType.getType() == Procreate.TYPE) {
						annFund = annFund.add(fEsco.getPublicProcreateFund());
					}
				}
			}
			write.setNextNumbericData(annFund.negate());

			write.setNextStringData("");
			write.setNextStringData(summary.getPr());
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(salaryType.getSecondAssignment());
			write.setNextStringData("");
			write.setNextStringData(salaryType.getText());
			write.setNextStringData(COMPANY_CODE);
		}
	}

	private void skipNext(String nextName) {
		String s = null;
		while (true) {
			if (!reader.hasRow()) {
				reader.skipRow(1);
				continue;
			}
			reader.nextRow();
			s = reader.getStringByIndex(0);
			if (s != null && s.startsWith(nextName)) {
				break;
			}
		}
	}

	private String getRealCode() {
		if (detailFile.indexOf("-HB-") > -1 || detailFile.indexOf("-SJZ-") > -1) {
			return "3923";
		} else if (detailFile.indexOf("-TJ-") > -1) {
			return "3920";
		} else if (detailFile.indexOf("-BJ-") > -1) {
			return "3916";
		}
		return "";
	}

}
