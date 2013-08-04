package jing.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jing.model.global.CostCenter;
import jing.model.global.SOA;
import jing.util.excel.POIReader;
import jing.util.lang.NumbericUtils;

public class RuntimeData {
	private static RuntimeData instance;

	private HashMap<String, String> ccMaps = new HashMap<String, String>();
	private HashMap<String, List<CostCenter>> prMaps = new HashMap<String, List<CostCenter>>();
	private HashMap<String, CostCenter> costCenters = new HashMap<String, CostCenter>();
	private HashMap<String, SOA> soas = new HashMap<String, SOA>();
	private HashMap<String, String> companyMapper = new HashMap<String, String>();
	private HashMap<String, String> companyCodeMapper = new HashMap<String, String>();

	private RuntimeData() {
		loadCostCentere();
		loadSOA();
		initCompanyMapper();
		initCompanyCodeMapper();
	}

	private void initCompanyMapper() {
		companyMapper.put("3920", "天津");
		companyMapper.put("9201", "天津");
		companyMapper.put("3923", "河北/太原");
		companyMapper.put("9231", "河北/太原");
		companyMapper.put("9232", "河北/太原");
		companyMapper.put("9233", "河北/太原");
		companyMapper.put("3916", "北京");
		companyMapper.put("9161", "天津");
		companyMapper.put("9162", "河北/太原");
		companyMapper.put("9163", "河北/太原");
		companyMapper.put("9164", "北京");
		companyMapper.put("3910", "FON");
	}
	
	private void initCompanyCodeMapper() {
		companyCodeMapper.put("3920", "TJ");
		companyCodeMapper.put("9201", "TJ");
		companyCodeMapper.put("3923", "HB");
		companyCodeMapper.put("9231", "HB");
		companyCodeMapper.put("9232", "HB");
		companyCodeMapper.put("9233", "HB");
		companyCodeMapper.put("3916", "BJ");
		companyCodeMapper.put("9161", "TJ");
		companyCodeMapper.put("9162", "HB");
		companyCodeMapper.put("9163", "HB");
		companyCodeMapper.put("9164", "BJ");
		companyCodeMapper.put("3910", "FON");
	}

	private void loadSOA() {
		String s = null;
		int i = 0;
		SOA soa = null;
		POIReader reader = new POIReader("./resource/SOA.xls");
		reader.loadSheet(0);
		soas.clear();
		while (reader.hasRow()) {
			reader.nextRow();
			s = reader.getNextStringFormat();
			if (s.equals("0") || !s.matches("[0-9]+"))
				continue;
			soa = new SOA(s);
			soa.setText(reader.getNextStringFormat());
			soa.setName(reader.getNextStringFormat());
			soas.put(s, soa);
		}
		reader.destroy();
		System.out.println("SOA loaded.");
	}

	private void loadCostCentere() {
		String s = null;
		int i = 0;
		CostCenter costCenter = null;
		POIReader reader = new POIReader("./resource/CostCenter.xls");
		reader.loadSheet(0);
		costCenters.clear();
		while (reader.hasRow()) {
			reader.nextRow();
			i = 0;
			if (reader.isCellNull())
				break;
			if (reader.getCellType(i) == POIReader.CELL_TYPE_STRING) {
				s = reader.getStringByIndex(i);
			} else {
				s = NumbericUtils.doubleToString(reader.getNumbericByIndex(i));
			}
			if (s.equals("0") || !s.matches("[0-9]+"))
				continue;
			costCenter = new CostCenter(s);
			if (reader.getNextCellType() == POIReader.CELL_TYPE_STRING) {
				costCenter.setName(reader.getNextString());
			} else {
				costCenter.setName(NumbericUtils.doubleToString(reader
						.getNextNumberic()));
			}
			reader.skipCell(1);
			if (reader.getNextCellType() == POIReader.CELL_TYPE_STRING) {
				costCenter.setPr(reader.getNextString());
			} else {
				costCenter.setPr(NumbericUtils.doubleToString(reader
						.getNextNumberic()));
			}
			costCenters.put(s, costCenter);
			if (!prMaps.containsKey(costCenter.getPr())) {
				prMaps.put(costCenter.getPr(), new ArrayList<CostCenter>());
			}
			prMaps.get(costCenter.getPr()).add(costCenter);
			ccMaps.put(costCenter.getCode(), costCenter.getPr());
		}
		reader.destroy();
		System.out.println("Cost Center loaded.");
	}

	public static RuntimeData getInstance() {
		if (instance == null) {
			instance = new RuntimeData();
		}
		return instance;
	}

	public Set<String> getPrSet() {
		return this.prMaps.keySet();
	}

	public String getPrByCode(String costCenterCode) {
		return this.ccMaps.get(costCenterCode);
	}

	public CostCenter getCostCenterByCode(String code) {
		if (code == null)
			return null;
		return this.costCenters.get(code);
	}

	public SOA getSOAByCode(String code) {
		if (code == null)
			return null;
		return this.soas.get(code);
	}

	public String getSOANameByCode(String code) {
		SOA s = this.getSOAByCode(code);
		if (s == null || s.getName() == null || s.getName().equals(""))
			return "未找到会计科目名称";
		return s.getName();
	}

	public String getCompanyName(String code) {
		if (code == null || code.equals(""))
			return "";
		return this.companyMapper.get(code);
	}

	public String getBillAge(int billMonth) {
		if (billMonth <= 6) {
			return "A";
		} else if (billMonth <= 12) {
			return "B";
		} else if (billMonth <= 24) {
			return "C";
		} else if (billMonth <= 36) {
			return "D";
		} else {
			return "F";
		}
	}
	
	public String getCompanyCodeByPr(String pr){
		return this.companyCodeMapper.get(pr);
	}
}
