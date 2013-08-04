package jing.salary.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import jing.model.accrual.Listing;
import jing.model.accrual.WBS;
import jing.model.data.RuntimeData;
import jing.salary.service.AccrualGenerator;
import jing.util.excel.POIReader;
import jing.util.excel.POIWriter;
import jing.util.lang.DateUtils;

public class AccrualGeneratorImpl implements AccrualGenerator {
	private static final int WBS_NO_INDEX = 7;
	private static final String IGNORE_WBS_NO = "FREIGHT";
	private POIReader reader;
	private POIWriter write = null;

	private String mainBillSubject;
	private String month;
	private String wbsFiel;
	private String listingFile;
	private HashMap<String, WBS> wbses = new HashMap<String, WBS>();
	private HashMap<String, List<Listing>> listings = new HashMap<String, List<Listing>>();

	@Override
	public void load(String wbsFile, String listingFile) {
		this.wbsFiel = wbsFile;
		this.listingFile = listingFile;
		reader = new POIReader(wbsFile);
		// reader = new POIReader("./temp/WBS120830.xls");
		reader.loadSheet(0);
		reader.skipRow(1); // 跳过标题

		String wbsNo = null;
		WBS wbs = null;
		wbses.clear();
		listings.clear();
		try {
			while (reader.hasRow()) {
				reader.nextRow();
				wbsNo = reader.getStringByIndex(WBS_NO_INDEX);
				if (wbsNo == null) {
					break;
				}
				if (wbsNo.equals(IGNORE_WBS_NO) || wbsNo.endsWith("M")) {
					continue;
				}
				wbsNo = wbsNo.toUpperCase();
				wbs = new WBS(wbsNo);
				wbs.setProject(reader.getNextStringFormat());
				wbs.setSalesVoucher(reader.getNextStringFormat());
				wbs.setProductLine(reader.getNextStringFormat());
				reader.skipCell(1); // 跳过空白列
				wbs.setCompany(reader.getNextStringFormat());
				wbs.setProductGroup(reader.getNextStringFormat());
				wbs.setContractNo(reader.getNextStringFormat());
				reader.skipCell(1); // 跳过wbsNo
				wbs.setProjectDes(reader.getNextStringFormat());
				wbs.setCompanyCode(reader.getNextStringFormat());
				wbs.setPr(reader.getNextStringFormat());
				wbs.setSalesOrganization(reader.getNextStringFormat());
				wbs.setSalesOrderType(reader.getNextStringFormat());

				wbses.put(wbsNo, wbs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reader.destroy();
		}
		System.out.println("WBS file loaded.");

		reader = new POIReader(listingFile);
		reader.loadSheet(0);
		reader.skipRow(1); // 跳过标题

		Listing listing = null;
		List<Listing> listingList = null;
		while (reader.hasRow()) {
			reader.nextRow();

			reader.skipCell(1);
			wbsNo = reader.getNextStringFormat();
			if (wbsNo == null || wbsNo.equals("") || wbsNo.equals("0")) {
				break;
			}
			wbsNo = wbsNo.toUpperCase();
			listing = new Listing(wbsNo);
			// listing.setSubject(reader.getNextStringFormat());
			listing.setPr(reader.getNextStringFormat());
			listing.setUserName(reader.getNextStringFormat());
			listing.setVoucherNo(reader.getNextStringFormat());
			reader.skipCell(1);
			listing.setVoucherType(reader.getNextStringFormat());
			listing.setVoucherDate(reader.getNextDateFormat());
			listing.setPassBillNo(reader.getNextStringFormat());
			listing.setAmount(reader.getNextBigDecimalFormat());
			listing.setAmountType(reader.getNextStringFormat());
			reader.skipCell(2);
			listing.setText(reader.getNextStringFormat());
			// listing.setBillDate(DateUtils.dateToString(reader
			// .getNextDateFormat()));
			wbs = wbses.get(wbsNo);
			if (wbs != null) {
				listing.setCompany(wbs.getCompany());
			}

			if (listings.get(wbsNo) == null) {
				listingList = new ArrayList<Listing>();
				listings.put(wbsNo, listingList);
			}
			listings.get(wbsNo).add(listing);
		}
		reader.destroy();
		System.out.println("WBS file and Listing file loaded.");
	}

	private String getMainBillSubject() {
		if (this.mainBillSubject == null) {
			String listingFileName = this.listingFile
					.substring(this.listingFile.lastIndexOf("\\") + 1);
			this.mainBillSubject = listingFileName.substring(0,
					listingFileName.indexOf("清"));
		}
		return this.mainBillSubject;

	}

	@Override
	public String generate(String month) {
		this.month = month;
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");
		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();

		mainBillSubject = getMainBillSubject();
		Date c = Calendar.getInstance().getTime();
		Date lastDay = DateUtils.getLastDateOfMonth(Integer.valueOf(month));
		String s = new SimpleDateFormat("yyyyMM").format(lastDay);
		fileName += "/" + "Accrual_" + mainBillSubject + "_North-"
				+ s.substring(2) + ".xls";
		try {
			write = new POIWriter(fileName);

			write.createNewSheet("Accrual_" + mainBillSubject + "_North");
			write.createRow();
			write.setNextStringData("总帐科目");
			write.skipCell(1);
			write.setNextStringData(mainBillSubject);
			write.skipCell(8);
			write.setNextStringData(" 预提费用分包费-安装 ");
			write.skipCell(1);
			write.setNextStringData("数据更新截止日期：");
			write.setNextStringData(DateUtils.dateToString(lastDay));

			write.createRow();
			write.setNextStringData("公司代码");
			write.skipCell(1);
			write.setNextStringData("3910");

			write.createRow();
			write.setNextStringData("分配");
			write.setNextStringData("合同号");
			write.setNextStringData("项目名称");
			write.setNextStringData("利润中心");
			write.setNextStringData("分公司");
			write.setNextStringData("凭证编号");
			write.setNextStringData("产品线");
			write.setNextStringData("凭证类型");
			write.setNextStringData("凭证日期");
			write.setNextStringData("帐龄(月)");
			write.setNextStringData("帐龄");
			write.setNextStringData("本币金额");
			write.setNextStringData("本币");
			write.setNextStringData("文本");

			TreeMap<String, List<Listing>> sortedListing = new TreeMap<String, List<Listing>>(
					this.listings);
			Iterator<Entry<String, List<Listing>>> ites = sortedListing
					.entrySet().iterator();
			Entry<String, List<Listing>> entry = null;
			List<Listing> listingList = null;
			Listing listing = null;
			BigDecimal total = null;
			WBS wbs = null;
			String projectDes = "";
			String contractNo = "";
			String company = "";
			String voucherNo = "";
			String productLine = "";
			while (ites.hasNext()) {
				entry = ites.next();
				listingList = entry.getValue();
				total = new BigDecimal(0);
				for (Listing l : listingList) {
					if (l.getAmount().doubleValue() < 0) {
						listing = l;
					}
					total = total.add(l.getAmount());
				}
				write.createRow();
				write.setNextStringData(listing.getWbsNo());
				wbs = this.wbses.get(listing.getWbsNo());
				if (wbs == null) {
					// System.out.println("WBS not found by  wbsNo:"
					// + listing.getWbsNo());
					contractNo = "";
					projectDes = "";
					company = "";
					voucherNo = "";
					productLine = "";
				} else {
					contractNo = wbs.getContractNo();
					projectDes = wbs.getProjectDes();
					company = RuntimeData.getInstance().getCompanyName(
							wbs.getCompany());
					voucherNo = listing.getVoucherNo();
					productLine = wbs.getProductLine();
				}

				write.setNextStringData(contractNo);
				write.setNextStringData(projectDes);
				write.setNextStringData(listing.getPr());
				write.setNextStringData(company);
				write.setNextStringData(voucherNo);
				write.setNextStringData(productLine);
				write.setNextStringData(listing.getVoucherType());
				write.setNextStringData(DateUtils.dateToString(listing
						.getVoucherDate()));
				int diffMonth = (lastDay.getYear() - 1 - listing
						.getVoucherDate().getYear())
						* 12
						+ (11 - listing.getVoucherDate().getMonth()
								+ lastDay.getMonth() + 1);
				if (lastDay.getDate() >= listing.getVoucherDate().getDate()) {
					diffMonth += 1;
				}
				write.setNextStringData(diffMonth + ""); // 账龄(月)
				write.setNextStringData(RuntimeData.getInstance().getBillAge(
						diffMonth)); // 账龄
				write.setNextNumbericData(total);
				write.setNextStringData("RMB");
				write.setNextStringData(listing.getText());
				// write.setNextStringData(data)
			}
			write.flush();
			write.destroy();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return fileName;
	}

}
