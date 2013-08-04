package jing.salary.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jing.model.accrual.WBS;
import jing.model.data.RuntimeData;
import jing.model.global.CostCenter;
import jing.model.global.SOA;
import jing.salary.service.WBSLoader;
import jing.util.db.sqlite.SQLiteUtils;
import jing.util.excel.POIWriter;
import jing.util.fields.DBFields;
import jing.util.lang.StringUtils;
import jing.util.message.Message;
import jing.util.message.MessageProvider;

public class InputVoucherImpl {
	public static final String VOUCHER_TABLE = "voucher";
	private int voucherCount = 0;
	private boolean isLoadWBS = false;
	private float currentTotal = 0F;
	private HashMap<String, WBS> wbses = new HashMap<String, WBS>();
	private static NumberFormat numberFormat = NumberFormat.getNumberInstance();

	private static Map<String, String> documentTypeMap = new HashMap<String, String>();
	private static Map<String, String> _50AccountMap = new HashMap<String, String>();

	static {
		numberFormat.setMaximumFractionDigits(2);
		documentTypeMap.put("3916", "2H");
		documentTypeMap.put("3920", "2I");
		documentTypeMap.put("3913", "2Y");

		_50AccountMap.put("3916", "1023003");
		_50AccountMap.put("3920", "1023301");
		_50AccountMap.put("3923", "1023601");
	}

	public void loadWBS(String wbsFile) {
		wbses = WBSLoader.load(wbsFile);
		if (wbses == null || wbses.size() < 1) {
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"加载WBS文件，但未发现内容");
		}
		this.isLoadWBS = true;
	}

	public void insertVoucher(Map<String, String> data) {
		data.put(DBFields.TABLE, VOUCHER_TABLE);
		try {
			SQLiteUtils.getInstance().insert(data);
		} catch (Exception e) {
			e.printStackTrace();
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"保存凭证出错：" + e.getMessage());
		}
		float amount = Float.valueOf(data.get("Amount"));
		currentTotal += amount;
		voucherCount++;
	}

	public boolean isLoadWBS() {
		return this.isLoadWBS;
	}

	public String getCurrentAmount() {
		return numberFormat.format(currentTotal);
	}

	public int getVoucherCount() {
		return this.voucherCount;
	}

	public void clearAmount() {
		this.currentTotal = 0;
		this.voucherCount = 0;
	}

	public String export(String voucherExportDate) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DBFields.TABLE, VOUCHER_TABLE);
		Map<String, String> query = new HashMap<String, String>();
		query.put("InputDate", voucherExportDate);
		data.put(DBFields.QUERY_CONDITION, query);
		List<Map<String, String>> result = null;
		try {
			result = SQLiteUtils.getInstance().query(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"导出凭证错误：" + e.getMessage());
			return null;
		}
		TreeMap<String, List<Map<String, String>>> groups = null;
		if (result.size() > 1) {
			groups = new TreeMap<String, List<Map<String, String>>>();
			String seq = null;
			List<Map<String, String>> oneGroup = null;
			for (Map<String, String> item : result) {
				seq = item.get(DBFields.SEQ);
				oneGroup = groups.get(seq);
				if (oneGroup == null) {
					oneGroup = new ArrayList<Map<String, String>>();
					groups.put(seq, oneGroup);
				}
				oneGroup.add(item);
			}
		} else {
			MessageProvider.getInstance().publicMessage("未查询到记录导出");
			return null;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");
		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();
		dateFormat = new SimpleDateFormat("yy");
		fileName += "/凭证_" + voucherExportDate + ".xls";
		POIWriter writer = null;
		try {
			writer = new POIWriter(fileName);
			writer.createNewSheet("凭证_" + voucherExportDate);

			generateVoucher(groups, writer);

			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"导出凭证错误：" + e.getMessage());
			throw new RuntimeException(e);
		} finally {
			try {
				writer.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileName;
	}

	private void generateVoucher(
			TreeMap<String, List<Map<String, String>>> groups, POIWriter writer) {
		float groupCount = 0F;
		String wbs = null;
		String cc = null;
		String amount = null;
		String documentTypeCode = null;
		String _50TextString = "";
		String docHeaderText = null;
		String member = null;
		WBS w = null;
		CostCenter costCenter = null;
		String voucherDate = null;
		String code = null;
		String text = null;
		String ref = null;
		SOA coa = null;
		Set<String> seqs = groups.keySet();
		List<Map<String, String>> group = null;

		writer.createRow();
		writer.setNextStringData("No.");
		writer.setNextStringData("Document type");
		writer.setNextStringData("Document type");
		writer.setNextStringData("");
		writer.setNextStringData("Currency");
		writer.setNextStringData("Exchange Rate");
		writer.setNextStringData("Reference (16)");
		writer.setNextStringData("Doc.header text (25)");
		writer.setNextStringData("Posting Key");
		writer.setNextStringData("Account");
		writer.setNextStringData("Amount");
		writer.setNextStringData("Tax code");
		writer.setNextStringData("Determine tax base");
		writer.setNextStringData("Cost Center");
		writer.setNextStringData("Internal Order");
		writer.setNextStringData("WBS element");
		writer.setNextStringData("Assignment (18)");
		writer.setNextStringData("Text (50)");
		writer.setNextStringData("Reason code");

		for (String seq : seqs) {
			group = groups.get(seq);
			groupCount = 0F;
			documentTypeCode = null;
			for (Map<String, String> item : group) {
				writer.createRow();
				writer.setNextStringData(seq);

				cc = item.get(DBFields.CC);
				if (documentTypeCode == null) {
					costCenter = RuntimeData.getInstance().getCostCenterByCode(
							cc);
					documentTypeCode = documentTypeMap.get(costCenter.getPr());
				}
				writer.setNextStringData(documentTypeCode);

				voucherDate = item.get(DBFields.VOUCHER_DATE);
				writer.setNextStringData(voucherDate);
				writer.setNextStringData("");
				writer.setNextStringData("");
				writer.setNextStringData("");
				ref = item.get(DBFields.REF);
				writer.setNextStringData(ref);
				member = item.get(DBFields.MEMBER);
				docHeaderText = member + "报销费用";
				writer.setNextStringData(docHeaderText);
				writer.setNextStringData("40");

				code = item.get(DBFields.CODE);
				writer.setNextStringData(code);
				amount = item.get(DBFields.AMOUNT);
				writer.setNextStringData(amount);
				writer.setNextStringData("");
				writer.setNextStringData("");
				writer.setNextStringData(cc);
				writer.setNextStringData("");

				wbs = item.get(DBFields.WBS);
				writer.setNextStringData(wbs);

				if (wbs != null && !"".equals(wbs)) {
					w = wbses.get(wbs);
					text = w.getProjectDes();
				} else {
					text = voucherDate.substring(0, 4) + "年"
							+ voucherDate.substring(4, 6) + "月";
				}
				coa = RuntimeData.getInstance().getSOAByCode(code);
				if (coa != null) {
					text += coa.getText();
					_50TextString += coa.getText();
				}
				writer.setNextStringData(member + "报销" + text);

				groupCount += Float.valueOf(amount);
			}
			writer.createRow();
			writer.setNextStringData(seq);
			writer.setNextStringData(documentTypeCode);
			writer.setNextStringData(voucherDate);
			writer.setNextStringData("");
			writer.setNextStringData("");
			writer.setNextStringData("");
			writer.setNextStringData(ref);
			writer.setNextStringData(docHeaderText);
			writer.setNextStringData("50");
			writer.setNextStringData(this._50AccountMap.get(costCenter.getPr()));
			writer.setNextStringData("" + groupCount);
			writer.setNextStringData("");
			writer.setNextStringData("");
			writer.setNextStringData("");
			writer.setNextStringData("");
			writer.setNextStringData("");
			writer.setNextStringData("");
			writer.setNextStringData(member + "报销" + _50TextString);
			writer.setNextStringData("204");
		}
	}

	public String getNextSeq() {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put(DBFields.TABLE, VOUCHER_TABLE);
		List<String> returnFields = new ArrayList<String>();
		returnFields.add("max(" + DBFields.SEQ + ") as " + DBFields.SEQ);
		query.put(DBFields.RETURN_FIELDS, returnFields);
		try {
			List<Map<String, String>> returnData = SQLiteUtils.getInstance()
					.query(query);
			if (returnData.size() < 1) {
				return "1";
			} else {
				String nextSeq = returnData.get(0).get(DBFields.SEQ);
				return (Integer.valueOf(nextSeq) + 1) + "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "1";
		}

	}
}
