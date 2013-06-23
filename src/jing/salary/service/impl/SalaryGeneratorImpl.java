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
		// 加载wbs文件
		wbses = WBSLoader.load(wbsFile);
		if (wbses == null || wbses.size() < 1) {
			System.out.println("Wbs file has no record");
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"Wbs文件里没有记录");
			return;
		}

		// 读取明细文件
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"开始加载明细文件...");
		reader = new POIReader(file);
		// 加载第一个sheet
		reader.loadSheet(0);
		// 跳过开头的3行（包含标题行）
		reader.skipRow(SALARY_SKIP_ROW_COUNT);
		Summary summary = null;
		CostCenter cc = null;
		WBS w = null;
		String s = null;
		int i = 0;
		// 初始化明细文件的整个对象，里面包含所有的表
		report = new CostCenterReport();
		prSummaryMapper.clear();

		ProcessCache.setCacheValue(SalaryGenerator.class.getName(), 10);

		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			// 取得成本中心编号
			s = reader.getStringByIndex(i);
			// 如果读到了总计，说明已经读完了工资表则退出循环
			if (s.equals("总计"))
				break;
			// 使用成本中心编号实例化工资对象
			summary = new Summary(s);
			// 取得成本中心对象
			cc = RuntimeData.getInstance().getCostCenterByCode(s);
			// 如果成本中心对象不为空
			if (cc != null) {
				// 设置工资对象的利润中心为成本中心的pr
				summary.setPr(cc.getPr());
				// 如果工资映射对象不包含这个pr，新建一个列表，加到映射对象里
				if (!prSummaryMapper.containsKey(summary.getPr())) {
					prSummaryMapper.put(summary.getPr(),
							new ArrayList<String>());
				}
				// 把工资对象加到工资映射对象对应的列表里
				prSummaryMapper.get(summary.getPr()).add(s);
			}
			// 读取基薪
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
			// 把工资对象加到文件对象的工资映射对象里，以成本中心做为key
			report.getSummarys().put(summary.getCostCenter(), summary);
		}
		// reader.skipRow(FESCO_SKIP_ROW_COUNT);

		// 跳过N行，直到行的第一列为FEsco.NAME为止
		this.skipNext(FEsco.NAME);

		FEsco fesco = null;
		reader.skipRow(1); // 跳过标题
		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			s = reader.getStringByIndex(i);
			if (s.equals("总计"))
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
			if (s.equals("总计"))
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
				.publicMessage(Message.DEBUG, "明细文件已加载完成。");
	}

	/**
	 * 生成工资表
	 */
	@Override
	public String generate(String pr, int month, String postingDate) {
		// 需要生成的利润中心
		this.pr = pr;
		// 需要生成的月份
		this.month = month;
		// 需要生成的postingDate
		this.postingDate = postingDate;
		// 根据pr，取得pr对应的地区英文编码
		this.companyCode = RuntimeData.getInstance().getCompanyCodeByPr(pr);
		// 如果生成的是3920或3923，则还需要统计3910，只是只是生成一条凭证
		if (this.pr.equals("3923") || this.pr.equals("3920")
				|| this.pr.equals("3916")) {
			this.includePr = "3910";
		} else {
			this.includePr = NO_INCLUDE_PR;
		}
		// 设置日期格式
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");

		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();
		// 设置最后生成的文件名
		fileName += "/" + this.month + "月工资-" + pr + ".xls";
		try {
			write = new POIWriter(fileName);

			// 生成工资工作表
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"开始生成" + pr + "工资...");
			this.generateIntegration();
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					pr + "工资已生成。");
			// 生成实发工资工作表
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"开始生成" + pr + "实发工资...");
			this.generatePraticeSalary();
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					pr + "实发工资已生成。");

			// 只有3910需要生成第三张工作表，第三张工作表只有五险一金的往来凭证
			if (pr.equals("3910")) {
				MessageProvider.getInstance().publicMessage(Message.DEBUG,
						"开始生成" + pr + "往来...");
				if (detailFile.indexOf("-BJ-") < 0) {
					this.generate3910CA();
				}
				MessageProvider.getInstance().publicMessage(Message.DEBUG,
						pr + "往来已生成。");
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
		write.createNewSheet(pr + "工资");
		// 生成标题
		this.generateIntegrationTitle();

		// 生成工会经费
		this.generateIntegrationDetail(new LabourUnion(month + ""));
		// 生成养老保险
		this.generateIntegrationDetail(new Annuities(month + ""));
		// 生成医疗保险
		this.generateIntegrationDetail(new Hospitalization(month + ""));
		// 生成住房
		this.generateIntegrationDetail(new Accumulation(month + ""));
		// 生成失业保险
		this.generateIntegrationDetail(new LostWork(month + ""));
		// 生成工伤保险
		this.generateIntegrationDetail(new WorkBreak(month + ""));
		// 生成生育保险
		this.generateIntegrationDetail(new Procreate(month + ""));
		// 上海服务管理费
		this.generateIntegrationDetail(new SHManageFee(month + ""));

	}

	/**
	 * 根据工资类型，生成工资表
	 * 
	 * @param salaryType
	 *            工资类型
	 */
	private void generateIntegrationDetail(SalaryType salaryType) {
		// 工资对象
		Summary summary = null;
		BigDecimal amount = new BigDecimal(0);
		BigDecimal other = new BigDecimal(0);
		String prCode = RuntimeData.getInstance().getCompanyCodeByPr(this.pr);
		// 根据pr取出所有pr的工资，遍历工资对象
		for (String s : this.prSummaryMapper.get(pr)) {
			// 取得工资对象
			summary = report.getSummarys().get(s);
			if (summary == null) {
				continue;
			}

			// 根据这次需要生成的工资类型，取出这次循环的金额
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
			// 如果工资对象的金额是0，对不生成
			if (amount.intValue() == 0)
				continue;

			// 生成40工资凭证，一个工资对象会生成一条40凭证
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
			// 把这次金额累加
			other = other.add(amount);
		}

		if (this.pr.equals("3920") || this.pr.equals("3923")
				|| this.pr.equals("3916")) {
			BigDecimal includeTotal = new BigDecimal(0);
			amount = new BigDecimal(0);

			// 如果做的是五险一金，则包含3910的数时，是从fesco里找3910的数加在一起
			// 如果是做工会或上海外服管理，就从工资大长表里取总金额
			List<String> costCenters = this.prSummaryMapper.get(includePr);
			if (salaryType.getType() < 1 || salaryType.getType() > 6) {
				for (String costCenterNo : costCenters) {
					summary = report.getSummarys().get(costCenterNo);
					// 根据这次需要生成的工资类型，取出这次循环的金额
					if (salaryType.getType() == LabourUnion.TYPE) {
						amount = summary.getLabourUnionBaseFund();
					} else {
						amount = summary.getOverheadExpenses();
					}
					// 如果工资对象的金额是0，对不生成
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
					// 如果是当前的pr，才进行累加
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

				// 如果生成的是3916，还需要把local的数也加进来
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

			// 做工会与上海外服管理费
			// 循环includePr在工资大长表的数，合计起来，以方便做50时，使用这个数减去local的总金额
			// 如果pr是3916，并且生成的是工会，则不生成以下40
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

				// 3916不生成3910的工会
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
					write.setNextStringData(includePr); // 把分配写成includePr
					if (includePr.equals("3910")) {
						write.setNextStringData(prCode + "转HO"
								+ salaryType.getText());
					} else {
						write.setNextStringData("");
					}
				}
			}

		}

		// 如果当前pr不是3916;或者是3916，并且工资类型小于1（即工会）;或者是3916，并且工资类型大于6（即上海外服管理）
		// 则用工资的总金额生成一条50凭证，assignment用工资类型的secondAssignment
		if ((!pr.equals("3916") && !pr.equals("3910"))
				|| salaryType.getType() < 1 || salaryType.getType() > 6) {
			// 做3923或3920的50，如果是五险一金使用other的数减去local的总金额，如果是工会或上海外服管理，就只取other的金额
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
			// 否则，需要生成两条50凭证，分别使用local与fesco的总金额，并且使用一条使用类型的assignment，另一条使用类型的secondAssignment
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

			// 取得Local，遍历以累加取得local的总金额
			HashMap<String, Local> locals = report.getLocals();
			Collection<Local> l = locals.values();
			Iterator<Local> itl = l.iterator();
			Local local = null;
			BigDecimal annFundLocal = new BigDecimal(0);
			while (itl.hasNext()) {
				local = itl.next();
				// 如果是当前的pr,才进行累加
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
			// 取出所有的Fesco，遍历进行累加
			HashMap<String, FEsco> fescos = report.getFescos();
			Collection<FEsco> c = fescos.values();
			Iterator<FEsco> its = c.iterator();
			FEsco fEsco = null;
			BigDecimal annFund = new BigDecimal(0);
			while (its.hasNext()) {
				fEsco = its.next();
				// 如果是当前的pr，才进行累加
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
	 * 生成实发工资工作表
	 */
	private void generatePraticeSalary() {
		write.createNewSheet("实发工资");
		// 根据当前pr，取得pr对应的中文名称
		String prName = RuntimeData.getInstance().getCompanyName(this.pr);
		String prCode = RuntimeData.getInstance().getCompanyCodeByPr(this.pr);
		// 生成标题
		this.generateIntegrationTitle();

		// 工资对象
		Summary summary = null;
		// 工资金额
		BigDecimal salary = new BigDecimal(0);
		// 有加班费的工资列表
		List<Summary> otAllowances = new ArrayList<Summary>();
		// 有其他奖金的工资列表
		List<Summary> otherBonus = new ArrayList<Summary>();
		// 有安全回召奖金的工资列表
		List<Summary> saveRecallBonuses = new ArrayList<Summary>();
		// 安全回召奖金总额
		BigDecimal saveRecallBonusesTotal = new BigDecimal(0);
		// 维保销售奖金总额
		BigDecimal maintenanceBonusTotal = new BigDecimal(0);
		// 维保效率奖金总额
		BigDecimal maintenanceEfficiencyBonusTotal = new BigDecimal(0);
		// 高温、采暖费总额
		BigDecimal heatAllowanceTotal = new BigDecimal(0);
		// 销售奖金总额
		BigDecimal salesBonusTotal = new BigDecimal(0);
		// 安装管理奖金总额
		BigDecimal setupManagementBonusTotal = new BigDecimal(0);
		// 改造奖金总额
		BigDecimal rebuildBonusTotal = new BigDecimal(0);
		// 修理奖金总额
		BigDecimal fixBonusTotal = new BigDecimal(0);
		// 工会费总额
		BigDecimal labourUnionFundTotal = new BigDecimal(0);
		// 个人所得税总额
		BigDecimal personOwnDutyTotal = new BigDecimal(0);
		// 当月实发工资总额
		BigDecimal factSalaryTotal = new BigDecimal(0);
		// 工程绩效考核奖总额
		BigDecimal engineeringBonusTotal = new BigDecimal(0);
		// 自提公积金
		BigDecimal selfAccumulationFundTotal = new BigDecimal(0);
		// 自提养老保险
		BigDecimal selfAnnuitiesFundTotal = new BigDecimal(0);
		// 自提医疗保险
		BigDecimal selfHospitalizationInsuranceTotal = new BigDecimal(0);
		// 自提失业保险
		BigDecimal selfIdlenessFundTotal = new BigDecimal(0);
		// local的自提公积金
		BigDecimal localSelfAccumulationFundTotal = new BigDecimal(0);
		// local的自提养老保险
		BigDecimal localSelfAnnuitiesFundTotal = new BigDecimal(0);
		// local的自提医疗保险
		BigDecimal localSelfHospitalizationInsuranceTotal = new BigDecimal(0);
		// local的自提失业保险
		BigDecimal localSelfIdlenessFundTotal = new BigDecimal(0);
		// 应收帐款
		BigDecimal accountReceivableTotal = new BigDecimal(0);

		// BigDecimal salaryNeedMinus = new BigDecimal(0);

		BigDecimal includePrTotal = new BigDecimal(0);

		// 遍历当前pr从工资映射对象里找到工资对象的利润中心编号
		for (String s : this.prSummaryMapper.get(pr)) {
			// 根据利润中心编号取得工资对象
			summary = report.getSummarys().get(s);
			if (summary == null) {
				continue;
			}
			// 取出工资对象的工资金额
			salary = summary.getSalary();

			// 为每一条工资对象生成一条凭证
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			// 如果工资为正数，则是40，否则是50
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
			write.setNextStringData("发放" + prName + this.month + "月员工工资");

			// 如果工资对象的其他奖金不为0，则累加到其他奖金列表里
			if (summary.getOtherBonus().intValue() != 0) {
				otherBonus.add(summary);
			}
			// 如果工资对象的加班费不为0，则累加到加班费列表里
			if (summary.getOtAllowance().intValue() != 0) {
				otAllowances.add(summary);
			}
			// 如果安全回召奖金不为0，把工资对象加到安全回召奖金列表里，并且累加到安全回召奖金总额里
			if (summary.getSaveRecallBonus().intValue() != 0) {
				saveRecallBonuses.add(summary);
				saveRecallBonusesTotal = saveRecallBonusesTotal.add(summary
						.getSaveRecallBonus());
			}
			// 如果维保销售奖金不为0，则累加到维保销售奖金总额里
			if (summary.getMaintenanceBonus().intValue() != 0) {
				maintenanceBonusTotal = maintenanceBonusTotal.add(summary
						.getMaintenanceBonus());
			}
			// 如果维保效率奖金不为0，则累加到维保效率奖金总额里
			if (summary.getMaintenanceEfficiencyBonus().intValue() != 0) {
				maintenanceEfficiencyBonusTotal = maintenanceEfficiencyBonusTotal
						.add(summary.getMaintenanceEfficiencyBonus());
			}
			// 如果高温、采暖费不为0，则累加到高温、采暖费总额里
			if (summary.getHeatAllowance().intValue() != 0) {
				heatAllowanceTotal = heatAllowanceTotal.add(summary
						.getHeatAllowance());
			}
			// 如果销售奖金不为0，则累加到销售奖金总额里
			if (summary.getSalesBonus().intValue() != 0) {
				salesBonusTotal = salesBonusTotal.add(summary.getSalesBonus());
			}
			// 如果安装管理奖金不为0，则累加到安装管理奖金总额里
			if (summary.getSetupManagementBonus().intValue() != 0) {
				setupManagementBonusTotal = setupManagementBonusTotal
						.add(summary.getSetupManagementBonus());
			}
			// 如果改造奖金不为0，则累加到改造奖金总额里
			if (summary.getRebuildBonus().intValue() != 0) {
				// if(summary.)
				rebuildBonusTotal = rebuildBonusTotal.add(summary
						.getRebuildBonus());
			}
			// 如果修理奖金不为0，则累加到修理奖金总额里
			if (summary.getFixBonus().intValue() != 0) {
				fixBonusTotal = fixBonusTotal.add(summary.getFixBonus());
			}
			// 如果工会费不为0，则累加到工会费总额里
			if (summary.getLabourUnionFund().intValue() != 0) {
				labourUnionFundTotal = labourUnionFundTotal.add(summary
						.getLabourUnionFund());
			}
			// 如果个人所得税不为0，则累加到个人所得税总额里
			if (summary.getPersonOwnDuty().intValue() != 0) {
				personOwnDutyTotal = personOwnDutyTotal.add(summary
						.getPersonOwnDuty());
			}
			// 如果当月实发工资不为0，则累加到当月实发工资总额里
			if (summary.getFactSalary().intValue() != 0) {
				factSalaryTotal = factSalaryTotal.add(summary.getFactSalary());
			}
			// 如果工程绩效考核奖不为0，则累加到工程绩效考核奖总额里
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

		// 遍历加班费的工资列表
		System.out.println("otAllowances is :" + otAllowances.size());
		for (Summary s : otAllowances) {
			salary = s.getOtAllowance();
			// 每一条有加班费的工资，都生成一条凭证
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
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
			write.setNextStringData("发放" + prName + this.month + "月员工工资-加班费");
		}

		// 遍历其他奖金列表
		System.out.println("otherBonus is :" + otherBonus.size());
		for (Summary s : otherBonus) {
			salary = s.getOtherBonus();
			// 有其他奖金的工资都生成一条凭证
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
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
			write.setNextStringData("发放" + prName + this.month + "月员工工资-其他奖金");
		}

		System.out.println("saveRecallBonuses is :" + saveRecallBonuses.size());
		// 如果当前生成的pr是3916，遍历安全回召奖金列表，每条工资都生成一条凭证
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
				write.setNextStringData("发放" + this.month + "月员工工资");
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
				write.setNextStringData("发放" + prName + this.month
						+ "月员工工资-安全回召奖");
			}
		} else {
			// 否则，其他pr只生成一条凭证，前提是安全回召奖金总额不等于0
			if (saveRecallBonusesTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
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
				write.setNextStringData(this.pr + " 回召奖预提");
				write.setNextStringData("发放" + prName + this.month
						+ "月员工工资-安全回召奖");
			}
		}

		System.out
				.println("maintenanceBonusTotal is :" + maintenanceBonusTotal);

		// 如果维保销售奖不等于0，生成凭证
		if (maintenanceBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("2192061");

			write.setNextNumbericData(maintenanceBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " 保养奖预提");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-维保销售奖");
		}

		System.out.println("maintenanceEfficiencyBonusTotal is :"
				+ maintenanceEfficiencyBonusTotal);

		// 维保效率奖
		if (maintenanceEfficiencyBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("2192061");

			write.setNextNumbericData(maintenanceEfficiencyBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " 效率奖预提");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-维保效率奖");
		}

		System.out.println("heatAllowanceTotal is :" + heatAllowanceTotal);

		// 发放高温
		if (heatAllowanceTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("2192900");

			write.setNextNumbericData(heatAllowanceTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("高温");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-高温费");
		}

		System.out.println("salesBonusTotal is :" + salesBonusTotal);

		// 销售奖金，不管是哪个pr，都需要做一条40的凭证，凭证的数是销售奖金的总金额。
		// 最后还需要做“4”凭证，如果是做3910，只做一条40凭证，凭证的金额是总金额，如果不是3910，每一条销售奖金都要做一条40凭证。
		// 最后做“4”的50凭证，凭证的金额是以下这条凭证的金额
		if (salesBonusTotal.doubleValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("2181020");

			write.setNextNumbericData(salesBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(pr + " 销售员奖金");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-销售员奖金");
		}

		System.out.println("setupManagementBonusTotal is :"
				+ setupManagementBonusTotal);

		// 安装奖金
		if (setupManagementBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("2181020");

			write.setNextNumbericData(setupManagementBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(pr + " 安装奖金");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-安装管理奖");
		}

		System.out.println("rebuildBonusTotal is :" + rebuildBonusTotal);

		// 改造奖金
		BigDecimal reBuildBonusTotal_L = new BigDecimal(0);
		// M改造奖金总额
		BigDecimal reBuildBonusTotal_M = new BigDecimal(0);
		BigDecimal reBuildBonusTotal = new BigDecimal(0);
		// 取出所有的改造奖金
		Set<String> rebuildKey = report.getRebuildBonusDetails().keySet();
		RebuildBonusDetail rebuildDetail = null;
		BigDecimal rebuildBonus_M = new BigDecimal(0);
		BigDecimal rebuildBonus_L = new BigDecimal(0);
		HashMap<String, BigDecimal> reBuildBonusTotalMap_L = new HashMap<String, BigDecimal>();
		HashMap<String, BigDecimal> reBuildBonusTotalMap_M = new HashMap<String, BigDecimal>();
		// 遍历所有的改造奖金
		for (String s : rebuildKey) {
			// 取得改造奖金对象
			rebuildDetail = report.getRebuildBonusDetails().get(s);
			// 只取当前pr的改造奖金对象
			if (rebuildDetail.getPr() == null
					|| !rebuildDetail.getPr().equals(pr)) {
				continue;
			}
			// 如果改造奖金对象的wbs号以M或m结尾
			if (rebuildDetail.getWbsNumber().endsWith("M")
					|| rebuildDetail.getWbsNumber().endsWith("m")) {
				// 把改造奖金对象的金额累加到M改造奖金总额里
				reBuildBonusTotal_M = reBuildBonusTotal_M.add(rebuildDetail
						.getSum());
				// 取出当前改造奖金的wbs号的改造奖金总额
				rebuildBonus_M = reBuildBonusTotalMap_M.get(rebuildDetail
						.getWbsNumber());
				// 如果wbs号的改造奖金总额为空，新建一个总额
				if (rebuildBonus_M == null) {
					rebuildBonus_M = new BigDecimal(0);
				}
				// 把当前的改造奖金总额加到此wbs号的总额里
				rebuildBonus_M = rebuildBonus_M.add(rebuildDetail.getSum());
				// 把此wbs号重新加回去
				reBuildBonusTotalMap_M.put(rebuildDetail.getWbsNumber(),
						rebuildBonus_M);
				// 把整个改造奖金总额加上此次改造奖金金额
				reBuildBonusTotal = reBuildBonusTotal.add(rebuildDetail
						.getSum());
			}
			// 如果改造奖金对象的wbs号以L或l结尾
			if (rebuildDetail.getWbsNumber().endsWith("L")
					|| rebuildDetail.getWbsNumber().endsWith("l")) {
				// 把L改造奖金总额累加
				reBuildBonusTotal_L = reBuildBonusTotal_L.add(rebuildDetail
						.getSum());
				// 把这个改造奖金对象的wbs号改造奖金总额累加
				rebuildBonus_L = reBuildBonusTotalMap_L.get(rebuildDetail
						.getWbsNumber());
				if (rebuildBonus_L == null) {
					rebuildBonus_L = new BigDecimal(0);
				}
				rebuildBonus_L = rebuildBonus_L.add(rebuildDetail.getSum());
				reBuildBonusTotalMap_L.put(rebuildDetail.getWbsNumber(),
						rebuildBonus_L);
				// 把整个改造奖金总额累加
				reBuildBonusTotal = reBuildBonusTotal.add(rebuildDetail
						.getSum());
			}
		}
		// 如果改造奖金总额不为0
		if (rebuildBonusTotal.doubleValue() != 0) {
			// 生成改造奖金总额40与50的凭证
			// write.createRow();
			// write.setNextStringData("1");
			// write.setNextStringData(postingDate);
			// write.setNextStringData(postingDate);
			// write.setNextStringData("RMB");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("发放" + this.month + "月员工工资");
			// write.setNextStringData("40");
			// write.setNextStringData("1113001");
			// write.setNextNumbericData(rebuildBonusTotal);
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("3910");
			// write.setNextStringData("HO转发放" + prName + this.month + "月改造奖金");
			//
			// write.createRow();
			// write.setNextStringData("1");
			// write.setNextStringData(postingDate);
			// write.setNextStringData(postingDate);
			// write.setNextStringData("RMB");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("发放" + this.month + "月员工工资");
			// write.setNextStringData("50");
			// write.setNextStringData("2181020");
			// write.setNextNumbericData(rebuildBonusTotal);
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData(this.pr + " 改造奖金");
			// write.setNextStringData("发放" + prName + this.month +
			// "月员工工资-改造奖金");
			// // 把改造奖金累加到需要减去的工资金额
			// salaryNeedMinus = salaryNeedMinus.add(rebuildBonusTotal);

			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("2181020");
			write.setNextNumbericData(rebuildBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " 改造奖金");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-改造奖金");

			// write.createRow();
			// write.setNextStringData("1");
			// write.setNextStringData(postingDate);
			// write.setNextStringData(postingDate);
			// write.setNextStringData("RMB");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("发放" + this.month + "月员工工资");
			// write.setNextStringData("50");
			// write.setNextStringData("2192000");
			// write.setNextNumbericData(rebuildBonusTotal);
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData("");
			// write.setNextStringData(this.pr + " 改造奖金");
			// write.setNextStringData("发放" + prName + this.month + "月改造奖金");
		}

		// 改造凭证3放在最下面

		// 如果VO奖金明细有记录
		if (engineeringBonusTotal.doubleValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("2181020");
			write.setNextNumbericData(engineeringBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + "销售奖金");
			write.setNextStringData("发放" + prName + this.month
					+ "月员工工资-工程绩效考核奖VO奖金");
		}

		System.out.println("fixBonusTotal is :" + fixBonusTotal);

		// 如果修理备件奖金总额不为0，需要做一条凭证
		if (fixBonusTotal.intValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("40");
			write.setNextStringData("1113001");

			write.setNextNumbericData(fixBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("3910");
			write.setNextStringData("HO转" + this.month + "月" + prName
					+ "修理备件奖金");
		}

		BigDecimal localAnnuitiesTotal = new BigDecimal(0);
		BigDecimal localHospitalizationTotal = new BigDecimal(0);
		BigDecimal localLostWorkTotal = new BigDecimal(0);
		BigDecimal localAccumulationTotal = new BigDecimal(0);
		// 取出所有的local
		HashMap<String, Local> localMap = report.getLocals();
		Local local = null;
		// 遍历local
		for (String l : localMap.keySet()) {
			local = localMap.get(l);
			// 如果local的pr是当前的pr，才做累加。如果生成的是3916，也需要加上includePr里的数
			if (local.getPr().equals(pr)
					|| (this.pr.equals("3916") && local.getPr().equals(
							includePr))) {
				// 累加养老金
				localAnnuitiesTotal = localAnnuitiesTotal.add(local
						.getAnnuitiesFund());
				// 累加医疗金
				localHospitalizationTotal = localHospitalizationTotal.add(local
						.getHospitalizationInsurance());
				// 累加失业金
				localLostWorkTotal = localLostWorkTotal.add(local
						.getIdlenessFund());
				// 累加公积金
				localAccumulationTotal = localAccumulationTotal.add(local
						.getAccumulationFund());
			}
		}

		System.out.println("localAnnuitiesTotal is :" + localAnnuitiesTotal);
		// 如果生成的是3910，则不要生成养老保险到个税
		if (!this.pr.equals("3910")) {
			// 如果养老金总额不为0，需要做一条50的凭证
			if (localAnnuitiesTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181050");

				write.setNextNumbericData(localAnnuitiesTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月养老");
				write.setNextStringData("代扣" + prName + this.month + "月员工养老保险");
			}

			System.out.println("localHospitalizationTotal is :"
					+ localHospitalizationTotal);
			// 如果医疗金总额不为0，需要做一条50的凭证
			if (localHospitalizationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181070");

				write.setNextNumbericData(localHospitalizationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月医疗");
				write.setNextStringData("代扣" + prName + this.month + "月员工医疗保险");
			}

			System.out.println("localLostWorkTotal is :" + localLostWorkTotal);
			// 如果失业金总额不为0，需要做一条50的凭证
			if (localLostWorkTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181090");

				write.setNextNumbericData(localLostWorkTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月失业");
				write.setNextStringData("代扣" + prName + this.month + "月员工失业保险");
			}

			System.out.println("localAccumulationTotal is :"
					+ localAccumulationTotal);
			// 如果公积金总额不为0，需要做一条50的凭证
			if (localAccumulationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181100");

				write.setNextNumbericData(localAccumulationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月公积金");
				write.setNextStringData("代扣" + prName + this.month + "月员工住房公积金");
			}

			BigDecimal fescoAnnuitiesTotal = new BigDecimal(0);
			BigDecimal fescoHospitalizationTotal = new BigDecimal(0);
			BigDecimal fescoLostWorkTotal = new BigDecimal(0);
			BigDecimal fescoAccumulationTotal = new BigDecimal(0);
			// 取出所有的fesco
			HashMap<String, FEsco> fescoMap = report.getFescos();
			FEsco fesco = null;
			// 遍历fesco
			for (String f : fescoMap.keySet()) {
				fesco = fescoMap.get(f);
				// 只有fesco是当前pr的，才做累加；如果是生成3916，则需要加上includePr的数
				if (fesco.getPt().equals(pr) || fesco.getPt().equals(includePr)) {
					// 累加养老金
					fescoAnnuitiesTotal = fescoAnnuitiesTotal.add(fesco
							.getAnnuitiesFund());
					// 累加医疗金
					fescoHospitalizationTotal = fescoHospitalizationTotal
							.add(fesco.getHospitalizationInsurance());
					// 累加失业金
					fescoLostWorkTotal = fescoLostWorkTotal.add(fesco
							.getIdlenessFund());
					// 累加公积金
					fescoAccumulationTotal = fescoAccumulationTotal.add(fesco
							.getAccumulationFund());
				}
			}

			System.out
					.println("fescoAnnuitiesTotal is :" + fescoAnnuitiesTotal);
			// 如果养老金不为0，需要做一条上海外服的50凭证
			if (fescoAnnuitiesTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181050");

				write.setNextNumbericData(fescoAnnuitiesTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月上海外服养老");
				write.setNextStringData("代扣" + prName + this.month + "月员工养老保险");
			}

			System.out.println("fescoHospitalizationTotal is :"
					+ fescoHospitalizationTotal);
			// 如果医疗金不为0，需要做一条上海外服的50凭证
			if (fescoHospitalizationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181070");

				write.setNextNumbericData(fescoHospitalizationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月上海外服医疗");
				write.setNextStringData("代扣" + prName + this.month + "月员工医疗保险");
			}

			System.out.println("fescoLostWorkTotal is :" + fescoLostWorkTotal);
			// 如果失业金不为0，需要做一条上海外服的50凭证
			if (fescoLostWorkTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181090");

				write.setNextNumbericData(fescoLostWorkTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月上海外服失业");
				write.setNextStringData("代扣" + prName + this.month + "月员工失业保险");
			}

			System.out.println("fescoAccumulationTotal is :"
					+ fescoAccumulationTotal);
			// 如果公积金不为0，需要做一条上海外服的50凭证
			if (fescoAccumulationTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181100");

				write.setNextNumbericData(fescoAccumulationTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月上海外服公积金");
				write.setNextStringData("代扣" + prName + this.month + "月员工住房公积金");
			}

			System.out.println("labourUnionFundTotal is :"
					+ labourUnionFundTotal);

			// 个人工会经费不为0，做一条50凭证
			if (labourUnionFundTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2181140");

				write.setNextNumbericData(labourUnionFundTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月个人工会经费");
				write.setNextStringData("代扣" + prName + this.month + "月员工工会经费");
			}

			System.out.println("personOwnDutyTotal is :" + personOwnDutyTotal);

			// 个税不为0，做一条50凭证
			if (personOwnDutyTotal.intValue() != 0) {
				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
				write.setNextStringData("50");
				write.setNextStringData("2187001");

				write.setNextNumbericData(personOwnDutyTotal);
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData(this.month + "月个税");
				write.setNextStringData("代扣" + prName + this.month + "月员工个税");
			}
		}

		System.out.println("factSalaryTotal is :" + factSalaryTotal);

		// 如果需要包含的实发工资总额不为0，即做3920或3923时，需要把3910的实发总额做一条40
		if (includePrTotal.doubleValue() > 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
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
				write.setNextStringData("BJ转HO发放" + this.month
						+ "月员工工资及代扣员工个税工会上海外服及当地保险");
			} else {
				write.setNextStringData(prCode + "转HO发放" + prName + this.month
						+ "月员工工资及代扣保险工会个税");
			}
		}
		
		// 如果当月应收账户不为0，则做一个40
		if (accountReceivableTotal.doubleValue() != 0) {
			write.createRow();
			write.setNextStringData("1");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
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
				write.setNextStringData("3910AR奖金");
				write.setNextStringData("发放FON" + this.month + "月员工工资-应收账款奖");
			} else {
				write.setNextStringData("3910");
				write.setNextStringData("发放" + prName + this.month + "月员工工资-应收账款奖金");
			}
		}

		// 当月实发工资总额（包括includePr的）不为0，做一条50凭证
		// factSalaryTotal = factSalaryTotal.subtract(salaryNeedMinus);
		// factSalaryTotal = factSalaryTotal.add(includePrTotal);
		BigDecimal needSubtractLocal = new BigDecimal(0);
		if (this.pr.equals("3910")) {
			// 如果生成的是3910，实发工资需要加上自提四险、工会、个人所得，并且减去local的四险
			// 还要做另一条凭证，取local的四险做一条50
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
			write.setNextStringData("发放" + this.month + "月员工工资");
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
				write.setNextStringData(this.month + "月工资");
			}

			if (this.pr.equals("3910")) {
				write.setNextStringData(RuntimeData.getInstance()
						.getCompanyCodeByPr(getRealCode())
						+ "转HO发放"
						+ prName
						+ this.month + "月员工工资");

				write.createRow();
				write.setNextStringData("1");
				write.setNextStringData(postingDate);
				write.setNextStringData(postingDate);
				write.setNextStringData("RMB");
				write.setNextStringData("");
				write.setNextStringData("");
				write.setNextStringData("发放" + this.month + "月员工工资");
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
				write.setNextStringData("BJ转HO(FON)" + this.month + "月代扣北京当地保险");
			} else {
				write.setNextStringData("发放" + prName + this.month + "月员工工资");
			}
		}

		// 如果生成的是3910，不生成非"1"的凭证，直接返回
		if (this.pr.equals("3910")) {
			return;
		}
		// 安装凭证2 把明细表中安装的明细根据pr过滤出来，如果是当前pr，则统计出来
		// 如果安装管理奖总额不等于0
		if (setupManagementBonusTotal.doubleValue() != 0) {
			BigDecimal prSetupManageAmount = new BigDecimal(0);
			if (setupManageBonusDetailMapper.get(pr) != null
					&& setupManageBonusDetailMapper.get(pr).size() > 0) {
				// 遍历
				for (SetupManageBonusDetail detail : setupManageBonusDetailMapper
						.get(pr)) {
					// 如果明细对象的金额等于0，跳过
					if (detail.getAmount().doubleValue() == 0) {
						continue;
					}

					// 为每一条安装管理奖做一条凭证
					write.createRow();
					write.setNextStringData("2");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("发放" + this.month + "月员工工资-安装管理奖金");
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
					write.setNextStringData("发放" + prName + this.month
							+ "月员工工资-安装管理奖金");
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
					write.setNextStringData("发放" + this.month + "月员工工资-安装管理奖金");
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
					write.setNextStringData(setupKey + " 安装奖金");
					write.setNextStringData("发放" + prName + this.month
							+ "月员工工资-安装管理奖金");
				}
			}

			write.createRow();
			write.setNextStringData("2");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资-安装管理奖金");
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
			write.setNextStringData(this.pr + " 安装奖金");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-安装管理奖金");
		}

		// 改造凭证3
		// 如果有M开头的改造管理奖
		// if (reBuildBonusTotalMap_M.size() > 0) {
		// // 为每一条M开头的改造奖做一条40凭证
		// for (String wbsNo : reBuildBonusTotalMap_M.keySet()) {
		// write.createRow();
		// write.setNextStringData("3");
		// write.setNextStringData(postingDate);
		// write.setNextStringData(postingDate);
		// write.setNextStringData("RMB");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("发放" + this.month + "月员工工资-改造奖金");
		// write.setNextStringData("40");
		// write.setNextStringData("4291301");
		// write.setNextNumbericData(reBuildBonusTotalMap_M.get(wbsNo));
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData(wbsNo);
		// write.setNextStringData("");
		// write.setNextStringData("发放" + prName + this.month
		// + "月员工工资-改造奖金");
		// }
		// }
		//
		// // 如果有L开头的改造奖
		// if (reBuildBonusTotalMap_L.size() > 0) {
		// // 为每一条L开头的改造做一条40凭证
		// for (String wbsNo : reBuildBonusTotalMap_L.keySet()) {
		// write.createRow();
		// write.setNextStringData("3");
		// write.setNextStringData(postingDate);
		// write.setNextStringData(postingDate);
		// write.setNextStringData("RMB");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("发放" + this.month + "月员工工资-改造奖金");
		// write.setNextStringData("40");
		// write.setNextStringData("4291301");
		// write.setNextNumbericData(reBuildBonusTotalMap_L.get(wbsNo));
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData("");
		// write.setNextStringData(wbsNo);
		// write.setNextStringData("");
		// write.setNextStringData("发放" + prName + this.month
		// + "月员工工资-改造奖金");
		// }
		// }

		// 如果整个改造不等于0，做一条总的50凭证
		if (rebuildBonusTotal.doubleValue() != 0) {
			BigDecimal prRebuildBonusAmount = new BigDecimal(0);
			if (rebuildBonusDetailMapper.get(pr) != null
					&& rebuildBonusDetailMapper.get(pr).size() > 0) {
				// 遍历
				for (RebuildBonusDetail detail : rebuildBonusDetailMapper
						.get(pr)) {
					// 如果明细对象的金额等于0，跳过
					if (detail.getSum().doubleValue() == 0) {
						continue;
					}

					// 为每一条改造奖做一条凭证
					write.createRow();
					write.setNextStringData("3");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("发放" + this.month + "月员工工资-改造奖金");
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
					write.setNextStringData("发放" + prName + this.month
							+ "月员工工资-改造奖金");

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
					write.setNextStringData("发放" + this.month + "月员工工资-改造奖金");
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
						write.setNextStringData(r + "改造奖金");
					} else {
						write.setNextStringData(r);
					}
					write.setNextStringData("发放" + prName + this.month
							+ "月员工工资-改造奖金");
				}
			}
			write.createRow();
			write.setNextStringData("3");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资-改造奖金");
			write.setNextStringData("50");
			write.setNextStringData("2181020");
			write.setNextNumbericData(rebuildBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(pr + "改造奖金");
			write.setNextStringData("发放" + prName + this.month + "月员工工资-改造奖金");

		}

		// 凭证4
		// 做销售奖金的"4"
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
						write.setNextStringData("发放" + this.month
								+ "月员工工资-销售奖金");
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
						write.setNextStringData("发放" + prName + this.month
								+ "月员工工资-销售奖金");
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
					write.setNextStringData("发放" + this.month + "月员工工资-销售奖金");
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
					write.setNextStringData("发放" + prName + this.month
							+ "月员工工资-销售奖金");
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
				write.setNextStringData("发放" + this.month + "月员工工资-销售奖金");
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
				write.setNextStringData(pr + "销售员奖金");
				write.setNextStringData("发放" + prName + this.month
						+ "月员工工资-销售奖金");
			}
		}

		// 凭证5
		// VO奖金

		// 如果工程绩效考核奖总额不为0
		if (engineeringBonusTotal.intValue() != 0) {
			BigDecimal prVOBonusDetailAmount = new BigDecimal(0);
			if (voBonusDetailMapper.get(pr) != null
					&& voBonusDetailMapper.get(pr).size() > 0) {
				// 遍历VO奖金明细
				for (VOBonusDetail voBonusDetail : voBonusDetailMapper.get(pr)) {
					// 为每一条VO奖金明细生成40凭证
					write.createRow();
					write.setNextStringData("5");
					write.setNextStringData(postingDate);
					write.setNextStringData(postingDate);
					write.setNextStringData("RMB");
					write.setNextStringData("");
					write.setNextStringData("");
					write.setNextStringData("发放" + this.month + "月员工工资");
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
					write.setNextStringData("发放" + prName + this.month
							+ "月员工工资-工程绩效考核奖VO奖金");
					// 把VO奖金明细金额累加
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
					write.setNextStringData("发放" + this.month + "月员工工资");
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
					write.setNextStringData("发放" + prName + this.month
							+ "月员工工资-工程绩效考核奖VO奖金");
				}
			}
			write.createRow();
			write.setNextStringData("5");
			write.setNextStringData(postingDate);
			write.setNextStringData(postingDate);
			write.setNextStringData("RMB");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("发放" + this.month + "月员工工资");
			write.setNextStringData("50");
			write.setNextStringData("2181020");
			write.setNextNumbericData(engineeringBonusTotal);
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData("");
			write.setNextStringData(this.pr + " 销售员奖金");
			write.setNextStringData("发放" + prName + this.month
					+ "月员工工资-工程绩效考核奖VO奖金");
			// 把工程绩效考核奖加到需要减去的工资金额里
			// salaryNeedMinus = salaryNeedMinus.add(engineeringBonusTotal);

		}

	}

	private void generate3910CA() {
		write.createNewSheet("3916-3910往来");
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

		// 住房公积金
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月住房公积金");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicAccumulationFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("计提" + this.month + "月住房公积金");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月住房公积金");
		write.setNextStringData("50");
		write.setNextStringData("2181100");
		write.setNextNumbericData(publicAccumulationFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "月公积金");
		write.setNextStringData("计提" + this.month + "月住房公积金");

		// 养老金
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月养老保险");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicAnnuitiesFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("计提" + this.month + "月养老保险");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月养老保险");
		write.setNextStringData("50");
		write.setNextStringData("2181050");
		write.setNextNumbericData(publicAnnuitiesFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "月养老保险");
		write.setNextStringData("计提" + this.month + "月养老保险");

		// 医疗金
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月医疗保险");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicHospitalizationInsuranceAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("计提" + this.month + "月医疗保险");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月医疗保险");
		write.setNextStringData("50");
		write.setNextStringData("2181070");
		write.setNextNumbericData(publicHospitalizationInsuranceAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "月医疗保险");
		write.setNextStringData("计提" + this.month + "月医疗保险");

		// 失业金
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月失业保险");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicIdlenessFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("计提" + this.month + "月失业保险");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月失业保险");
		write.setNextStringData("50");
		write.setNextStringData("2181090");
		write.setNextNumbericData(publicIdlenessFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "月失业保险");
		write.setNextStringData("计提" + this.month + "月失业保险");

		// 生育金
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月生育保险");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicProcreateFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("计提" + this.month + "月生育保险");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月生育保险");
		write.setNextStringData("50");
		write.setNextStringData("2181902");
		write.setNextNumbericData(publicProcreateFundAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "月生育保险");
		write.setNextStringData("计提" + this.month + "月生育保险");

		// 工伤金
		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月工伤保险");
		write.setNextStringData("40");
		write.setNextStringData("1113001");
		write.setNextNumbericData(publicWorkHurtAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("3910");
		write.setNextStringData("计提" + this.month + "月工伤保险");

		write.createRow();
		write.setNextStringData("1");
		write.setNextStringData(postingDate);
		write.setNextStringData(postingDate);
		write.setNextStringData("RMB");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("计提" + this.month + "月工伤保险");
		write.setNextStringData("50");
		write.setNextStringData("2181110");
		write.setNextNumbericData(publicWorkHurtAmount);
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData("");
		write.setNextStringData(this.month + "月工伤保险");
		write.setNextStringData("计提" + this.month + "月工伤保险");
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
