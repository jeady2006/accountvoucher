package jing.salary.service;

import java.util.HashMap;

import jing.model.accrual.WBS;
import jing.util.excel.POIReader;
import jing.util.message.Message;
import jing.util.message.MessageProvider;

public class WBSLoader {
	private static POIReader reader = null;
	private static final int WBS_NO_INDEX = 5;
	private static final String IGNORE_WBS_NO = "FREIGHT";

	public static HashMap<String, WBS> load(String wbsFile) {
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"开始加载WBS文件...");
		HashMap<String, WBS> wbses = new HashMap<String, WBS>();
		reader = new POIReader(wbsFile);
		reader.loadSheet(0);
		reader.skipRow(1); // 跳过标题

		String wbsNo = null;
		WBS wbs = null;
		try {
			while (reader.hasRow()) {
				reader.nextRow();
				wbsNo = reader.getStringByIndex(WBS_NO_INDEX);
				if (wbsNo == null) {
					break;
				}
				if (wbsNo.equals(IGNORE_WBS_NO)) {
					continue;
				}
				wbsNo = wbsNo.toUpperCase();
				wbs = new WBS(wbsNo);
				wbs.setProject(reader.getNextStringFormat());
				wbs.setProjectDes(reader.getNextStringFormat());
				wbs.setCompany(reader.getNextStringFormat());
				wbs.setProductGroup(reader.getNextStringFormat());
				wbs.setContractNo(reader.getNextStringFormat());
				reader.skipCell(1); // 跳过wbsNo
				wbs.setSalesVoucher(reader.getNextStringFormat());
				wbs.setProductLine(reader.getNextStringFormat());
				wbs.setCompanyCode(reader.getNextStringFormat());
				wbs.setPr(reader.getNextStringFormat());
				wbs.setSalesOrganization(reader.getNextStringFormat());
				wbs.setSalesOrderType(reader.getNextStringFormat());
				wbses.put(wbsNo, wbs);
			}
		} catch (Exception e) {
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"WBS文件加载失败:" + e.getMessage());
			e.printStackTrace();
			return null;
		} finally {
			reader.destroy();
		}
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"WBS文件已加载完成。");
		return wbses;
	}
}
