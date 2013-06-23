package jing.salary.service.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.print.attribute.standard.NumberUp;

import jing.model.accrual.Listing;
import jing.model.accrual.WBS;
import jing.model.data.BackBilling;
import jing.model.data.RuntimeData;
import jing.salary.service.BackBillingGenerator;
import jing.salary.service.WBSLoader;
import jing.salary.service.listener.CallListener;
import jing.util.excel.POIReader;
import jing.util.excel.POIWriter;
import jing.util.lang.FileUtils;
import jing.util.lang.NumbericUtils;
import jing.util.lang.StringUtils;
import jing.util.message.Message;
import jing.util.message.MessageProvider;

public class BackBillingGeneratorImpl implements BackBillingGenerator {
	private static final int WBS_NO_INDEX = 5;
	private static final int COST_ELEMENT_INDEX = 6;
	private static final List<String> IGNORE_COST_ELEMENTS = new ArrayList<String>();
	private static final String IGNORE_WBS_NO_PREFIX = "8";

	private HashMap<String, WBS> wbses = new HashMap<String, WBS>();
	// 数据格式：{"是否分包":{"WBSNO":BackBilling}}
	private HashMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>> backBillings = new HashMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>>();
	private HashMap<String, HashMap<String, Listing>> notSplitListingProjects = new HashMap<String, HashMap<String, Listing>>();
	private HashMap<String, HashMap<String, Listing>> splitListingProjects = new HashMap<String, HashMap<String, Listing>>();
	private HashMap<String, Listing> listings2192041 = new HashMap<String, Listing>();
	private HashMap<String, Listing> listings2193000 = new HashMap<String, Listing>();
	private HashMap<String, HashMap<String, Listing>> listings2192041Map = null;
	private HashMap<String, HashMap<String, Listing>> listings2193000Map = null;
	private HashMap<String, HashMap<String, TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>>>> backBillingReplys = new HashMap<String, HashMap<String, TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>>>>();
	private POIReader reader = null;
	private String month;
	private String postingDate;
	private POIWriter write;

	public BackBillingGeneratorImpl() {
		IGNORE_COST_ELEMENTS.add("4291301");
		IGNORE_COST_ELEMENTS.add("4291302");
		IGNORE_COST_ELEMENTS.add("5905012");
		IGNORE_COST_ELEMENTS.add("4299002");
		IGNORE_COST_ELEMENTS.add("4299001");
	}

	@Override
	public void loadBackBilling(String wbsFile, String list2192041File,
			String list2193000File, String backBillingListFile) {
		wbses = WBSLoader.load(wbsFile);
		if (wbses == null || wbses.size() < 1) {
			return;
		}

		notSplitListingProjects.clear();
		splitListingProjects.clear();
		backBillings.clear();
		String wbsNo = null;
		WBS wbs = null;
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"开始加载2192041清单文件...");
		this.loadAccrualFile(listings2192041, list2192041File,
				splitListingProjects);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2192041清单文件已加载完成。");
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"开始加载2193000清单文件...");
		this.loadAccrualFile(listings2193000, list2193000File,
				notSplitListingProjects);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2193000清单文件已加载完成。");

		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"开始加载追补清单文件...");
		reader = new POIReader(backBillingListFile);
		reader.loadSheet(0);
		reader.skipRow(1); // 跳过标题

		String projectNo = null;
		String costElement = null;
		BackBilling backBilling = null;
		TreeMap<String, TreeMap<String, BackBilling>> backBillingProjectMap = null;
		TreeMap<String, BackBilling> backBillingMap = null;
		try {
			while (reader.hasRow()) {
				reader.nextRow();
				wbsNo = reader.getStringByIndex(WBS_NO_INDEX);
				if (wbsNo == null) {
					break;
				}
				if (wbsNo.startsWith(IGNORE_WBS_NO_PREFIX)) {
					continue;
				}
				wbsNo = wbsNo.toUpperCase();
				projectNo = wbsNo.substring(0, 5);
				costElement = reader.getStringByIndex(COST_ELEMENT_INDEX);
				if (costElement
						.equals(BackBilling.SPLIT_PACKAGE_COST_ELEMENT_NO)) {
					backBillingProjectMap = backBillings.get(Boolean.TRUE);
					if (backBillingProjectMap == null) {
						backBillingProjectMap = new TreeMap<String, TreeMap<String, BackBilling>>();
						backBillings.put(Boolean.TRUE, backBillingProjectMap);
					}
				} else {
					backBillingProjectMap = backBillings.get(Boolean.FALSE);
					if (backBillingProjectMap == null) {
						backBillingProjectMap = new TreeMap<String, TreeMap<String, BackBilling>>();
						backBillings.put(Boolean.FALSE, backBillingProjectMap);
					}
				}
				backBillingMap = backBillingProjectMap.get(projectNo);
				if (backBillingMap == null) {
					backBillingMap = new TreeMap<String, BackBilling>();
					backBillingProjectMap.put(projectNo, backBillingMap);
				}

				backBilling = backBillingMap.get(wbsNo);
				if (backBilling == null) {
					backBilling = new BackBilling(wbsNo);
				}
				backBilling.setYear(reader.getNextStringFormat());
				backBilling.setVoucherDate(reader.getNextStringFormat());
				backBilling.setVoucherNo(reader.getNextStringFormat());
				backBilling.setPassBillingDate(reader.getNextStringFormat());
				backBilling.setReferenceVoucherNo(reader.getNextStringFormat());
				reader.skipCell(1); // 跳过wbsNo列
				backBilling.setCostElement(reader.getNextStringFormat());
				if (IGNORE_COST_ELEMENTS.contains(backBilling.getCostElement())) {
					continue;
				}
				backBilling.setCostElementName(reader.getNextStringFormat());
				backBilling.setValCOArCur(backBilling.getValCOArCur().add(
						reader.getNextBigDecimalFormat()));
				backBilling.setName(reader.getNextStringFormat());
				backBilling.setUser(reader.getNextStringFormat());

				wbs = wbses.get(wbsNo);
				if (wbs == null) {
					continue;
				}
				backBilling.setPr(wbs.getPr());
				backBilling.setLocation(RuntimeData.getInstance()
						.getCompanyName(wbs.getCompany()));
				backBilling.setProjectName(wbs.getProjectDes());

				backBillingMap.put(wbsNo, backBilling);
			}
		} catch (Exception e) {
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"追补清单文件加载失败:" + e.getMessage());
		} finally {
			reader.destroy();
		}
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"追补清单文件已加载完成。");
	}

	private void loadAccrualFile(HashMap<String, Listing> listings,
			String fileName,
			HashMap<String, HashMap<String, Listing>> listProjects) {
		reader = new POIReader(fileName);
		reader.loadSheet(0);
		reader.skipRow(1); // 跳过标题

		String project = null;
		String wbsNo = null;
		WBS wbs = null;
		Listing listing = null;
		HashMap<String, Listing> projects = null;
		while (reader.hasRow()) {
			reader.nextRow();

			reader.skipCell(1);
			wbsNo = reader.getNextStringFormat();
			if (wbsNo == null || wbsNo.equals("") || wbsNo.equals("0")) {
				break;
			}
			if (wbsNo.endsWith("M") || wbsNo.endsWith("m")
					|| wbsNo.length() < 9) {
				continue;
			}
			wbsNo = wbsNo.toUpperCase();
			listing = listings.get(wbsNo);
			if (listing == null) {
				listing = new Listing(wbsNo);
				listings.put(wbsNo, listing);
			}
			// listing.setSubject(reader.getNextStringFormat());
			// listing.setPr(reader.getNextStringFormat());
			// listing.setUserName(reader.getNextStringFormat());
			listing.setVoucherNo(reader.getNextStringFormat());
			reader.skipCell(1);
			listing.setVoucherType(reader.getNextStringFormat());
			listing.setVoucherDate(reader.getNextDateFormat());
			listing.setPassBillNo(reader.getNextStringFormat());
			if (listing.getAmount() == null) {
				listing.setAmount(reader.getNextBigDecimalFormat());
			} else {
				listing.setAmount(listing.getAmount().add(
						reader.getNextBigDecimalFormat()));
			}
			listing.setAmountType(reader.getNextStringFormat());
			reader.skipCell(2);
			listing.setText(reader.getNextStringFormat());
			// listing.setBillDate(DateUtils.dateToString(reader
			// .getNextDateFormat()));
			wbs = wbses.get(wbsNo);
			if (wbs != null) {
				listing.setCompany(wbs.getCompany());
			}

			project = wbsNo.substring(0, 5);
			projects = listProjects.get(project);
			if (projects == null) {
				projects = new HashMap<String, Listing>();
				listProjects.put(project, projects);
			}
			projects.put(wbsNo, listing);
		}
		reader.destroy();
	}

	private void loadReplyAccrualFile(
			HashMap<String, HashMap<String, Listing>> listings, String fileName) {
		reader = new POIReader(fileName);
		reader.loadSheet(0);
		reader.skipRow(1); // 跳过标题

		String wbsNo = null;
		String projectNo = null;
		Listing listing = null;
		HashMap<String, Listing> listingMap = null;
		while (reader.hasRow()) {
			reader.nextRow();

			reader.skipCell(1);
			wbsNo = reader.getNextStringFormat();
			if (wbsNo == null || wbsNo.equals("") || wbsNo.equals("0")) {
				break;
			}

			if (wbsNo.endsWith("M") || wbsNo.endsWith("m")) {
				continue;
			}

			if (wbsNo.length() < 5) {
				continue;
			}
			wbsNo = wbsNo.toUpperCase();
			projectNo = wbsNo.substring(0, 5);
			listingMap = listings.get(projectNo);
			if (listingMap == null) {
				listingMap = new HashMap<String, Listing>();
				listings.put(projectNo, listingMap);
			}
			listing = listingMap.get(wbsNo);
			if (listing == null) {
				listing = new Listing(wbsNo);
				listingMap.put(wbsNo, listing);
			}
			// listing.setSubject(reader.getNextStringFormat());
			// listing.setPr(reader.getNextStringFormat());
			// listing.setUserName(reader.getNextStringFormat());
			listing.setVoucherNo(reader.getNextStringFormat());
			reader.skipCell(1);
			listing.setVoucherType(reader.getNextStringFormat());
			listing.setVoucherDate(reader.getNextDateFormat());
			listing.setPassBillNo(reader.getNextStringFormat());
			if (listing.getAmount() == null) {
				listing.setAmount(reader.getNextBigDecimalFormat());
			} else {
				listing.setAmount(listing.getAmount().add(
						reader.getNextBigDecimalFormat()));
			}
			listing.setAmountType(reader.getNextStringFormat());
			reader.skipCell(2);
			listing.setText(reader.getNextStringFormat());
			// listing.setBillDate(DateUtils.dateToString(reader
			// .getNextDateFormat()));
			// wbs = wbses.get(wbsNo);
			// if (wbs != null) {
			// listing.setCompany(wbs.getCompany());
			// }
		}
		reader.destroy();
	}

	@Override
	public String generateBackBilling(String month) {
		this.month = month;
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");
		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();
		dateFormat = new SimpleDateFormat("yy");
		fileName += "/追补清单-" + dateFormat.format(new Date())
				+ StringUtils.padLeft(this.month, 2, "0") + ".xls";
		try {
			write = new POIWriter(fileName);

			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"开始生成汇总回复表...");
			write.createNewSheet("汇总回复表");
			this.generateReplyTitle();

			TreeMap<String, TreeMap<String, BackBilling>> notSplitPackage = backBillings
					.get(Boolean.FALSE);

			this.generateNotSplitPackageReplayDetail(notSplitPackage);

			TreeMap<String, TreeMap<String, BackBilling>> splitPackage = backBillings
					.get(Boolean.TRUE);
			this.generateSplitPackageReplayDetail(splitPackage);
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"汇总回复表已生成。");

			write.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				write.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileName;
	}

	private void generateSplitPackageReplayDetail(
			TreeMap<String, TreeMap<String, BackBilling>> splitPackage) {
		BackBilling b = null;
		TreeMap<String, BackBilling> splitPackages = null;
		List<BackBilling> hasRemaining = new ArrayList<BackBilling>();
		Set<String> splitPackageProjectSet = splitPackage.keySet();
		for (String splitPackageKey : splitPackageProjectSet) {
			splitPackages = splitPackage.get(splitPackageKey);
			Set<String> splitPackageSet = splitPackages.keySet();
			hasRemaining.clear();
			for (String wbsNo : splitPackageSet) {
				b = splitPackages.get(wbsNo);
				BigDecimal remaining = b.getValCOArCur();
				if (remaining.doubleValue() <= 0) {
					continue;
				}

				BigDecimal listAmount = new BigDecimal(0);
				Listing l = listings2192041.get(b.getWbsNo());
				if (l != null) {
					listAmount = l.getAmount();
					if (!NumbericUtils.isBigDecimalEqual0(l.getAmount())) {
						remaining = l.getAmount().add(remaining);
						if (NumbericUtils.isBigDecimalGreatherThan0(remaining)) {
							l.setAmount(new BigDecimal(0));
						} else {
							l.setAmount(remaining);
						}
					}
				}

				if (NumbericUtils.isBigDecimalGreatherThan0(remaining)) {
					b.setRemainingAmount(remaining);
					b.setListAmount(listAmount);
					hasRemaining.add(b);
					continue;
				}
				b.setRemainingAmount(new BigDecimal(0));

				write.createRow();
				write.setNextStringData(b.getPr());
				write.setNextStringData(b.getLocation());
				write.setNextStringData("分包");
				write.setNextStringData(b.getWbsNo());
				write.setNextNumbericData(b.getValCOArCur());

				write.setNextNumbericData(listAmount);

				// if (isSplitPackage) {
				// write.skipCell(1);
				// } else {
				// Listing l = listings2193000.get(b.getWbsNo());
				// if (l != null) {
				// if (Math.round(l.getAmount().doubleValue() * 100) != 0) {
				// write.setNextNumbericData(l.getAmount());
				// remaining = l.getAmount().add(remaining);
				// } else {
				// write.skipCell(1);
				// }
				// } else {
				// write.skipCell(1);
				// }
				// }
				write.skipCell(1);

				// if (remaining.intValue() > 0) {
				// write.setNextNumbericData(remaining);
				// } else {
				write.skipCell(1);
				// }
				write.setNextStringData("冲分包费预提-" + wbsNo);
				write.setNextStringData(b.getProjectName());
			}

			if (hasRemaining.size() > 0) {
				for (BackBilling bb : hasRemaining) {
					write.createRow();
					write.setNextStringData(bb.getPr());
					write.setNextStringData(bb.getLocation());
					write.setNextStringData("分包");
					write.setNextStringData(bb.getWbsNo());
					write.setNextNumbericData(bb.getValCOArCur());
					write.setNextNumbericData(bb.getListAmount());
					write.skipCell(1);
					write.setNextNumbericData(bb.getRemainingAmount());
					String replyString = getReplyString(bb,
							splitListingProjects);
					if (replyString.split(",").length == 1
							&& replyString.endsWith("追补")) {
						write.setNextStringData("追补");
					} else {
						write.setNextStringData("冲分包费预提-" + replyString);
					}
					write.setNextStringData(bb.getProjectName());
				}
			}
		}
	}

	private void generateNotSplitPackageReplayDetail(
			TreeMap<String, TreeMap<String, BackBilling>> notSplitPackage) {
		BackBilling b = null;
		TreeMap<String, BackBilling> notSplitPackages = null;
		List<BackBilling> hasRemaining = new ArrayList<BackBilling>();
		Set<String> notSplitPackageProjectSet = notSplitPackage.keySet();
		for (String notSplitPackageKey : notSplitPackageProjectSet) {
			notSplitPackages = notSplitPackage.get(notSplitPackageKey);
			Set<String> notSplitPackageSet = notSplitPackages.keySet();
			hasRemaining.clear();

			for (String wbsNo : notSplitPackageSet) {
				b = notSplitPackages.get(wbsNo);
				BigDecimal remaining = b.getValCOArCur();
				if (NumbericUtils.isBigDecimalEqual0(remaining)
						|| NumbericUtils.isBigDecimalLessThan0(remaining)) {
					continue;
				}

				BigDecimal listAmount = new BigDecimal(0);
				Listing l = listings2193000.get(b.getWbsNo());
				if (l != null) {
					listAmount = l.getAmount();
					if (!NumbericUtils.isBigDecimalEqual0(l.getAmount())) {
						remaining = l.getAmount().add(remaining);
						if (NumbericUtils.isBigDecimalGreatherThan0(remaining)) {
							l.setAmount(new BigDecimal(0));
						} else {
							l.setAmount(remaining);
						}
					}
				}

				if (NumbericUtils.isBigDecimalGreatherThan0(remaining)) {
					b.setRemainingAmount(remaining);
					b.setListAmount(listAmount);
					hasRemaining.add(b);
					continue;
				}
				b.setRemainingAmount(new BigDecimal(0));

				write.createRow();
				write.setNextStringData(b.getPr());
				write.setNextStringData(b.getLocation());
				write.setNextStringData("非分包");
				write.setNextStringData(b.getWbsNo());
				write.setNextNumbericData(b.getValCOArCur());
				write.skipCell(1);

				write.setNextNumbericData(listAmount);

				// if (isSplitPackage) {
				// write.skipCell(1);
				// } else {
				// Listing l = listings2193000.get(b.getWbsNo());
				// if (l != null) {
				// if (Math.round(l.getAmount().doubleValue() * 100) != 0) {
				// write.setNextNumbericData(l.getAmount());
				// remaining = l.getAmount().add(remaining);
				// } else {
				// write.skipCell(1);
				// }
				// } else {
				// write.skipCell(1);
				// }
				// }

				// if (remaining.intValue() > 0) {
				// write.setNextNumbericData(remaining);
				// } else {
				write.skipCell(1);
				// }
				write.setNextStringData("冲其他安装成本预提-" + wbsNo);
				write.setNextStringData(b.getProjectName());
			}

			if (hasRemaining.size() > 0) {
				for (BackBilling bb : hasRemaining) {
					write.createRow();
					write.setNextStringData(bb.getPr());
					write.setNextStringData(bb.getLocation());
					write.setNextStringData("非分包");
					write.setNextStringData(bb.getWbsNo());
					write.setNextNumbericData(bb.getValCOArCur());
					write.skipCell(1);
					write.setNextNumbericData(bb.getListAmount());
					write.setNextNumbericData(bb.getRemainingAmount());
					String replyString = getReplyString(bb,
							notSplitListingProjects);
					if (replyString.split(",").length == 1
							&& replyString.endsWith("追补")) {
						write.setNextStringData("追补");
					} else {
						write.setNextStringData("冲其他安装成本预提-" + replyString);
					}
					write.setNextStringData(bb.getProjectName());
				}
			}
		}
	}

	private String getReplyString(BackBilling backBilling,
			HashMap<String, HashMap<String, Listing>> listProjects) {
		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		if (!NumbericUtils.isBigDecimalEqual0(backBilling.getListAmount())) {
			sb.append(backBilling.getWbsNo());
			isFirst = false;
		}
		if (!NumbericUtils.isBigDecimalEqual0(backBilling.getRemainingAmount())) {
			String projectNo = backBilling.getWbsNo().substring(0, 5);
			HashMap<String, Listing> projects = listProjects.get(projectNo);
			if (projects != null) {
				Set<String> keys = projects.keySet();
				Listing min = null;
				Listing list = null;
				BigDecimal remaining = null;
				while (true) {
					for (String k : keys) {
						list = projects.get(k);
						if (min == null) {
							min = list;
						} else {
							if (list.getAmount().doubleValue() < min
									.getAmount().doubleValue()) {
								min = list;
							}
						}
					}
					if (NumbericUtils.isBigDecimalLessThan0(min.getAmount())) {
						remaining = min.getAmount().add(
								backBilling.getRemainingAmount());
						if (isFirst) {
							sb.append(min.getWbsNo());
							isFirst = false;
						} else {
							sb.append(",").append(min.getWbsNo());
						}
						if (NumbericUtils.isBigDecimalLessThan0(remaining)) {
							min.setAmount(remaining);
							backBilling.setRemainingAmount(new BigDecimal(0));
							break;
						} else {
							min.setAmount(new BigDecimal(0));
							backBilling.setRemainingAmount(remaining);
							continue;
						}
					} else {
						if (!isFirst) {
							sb.append(",");
						} else {
							isFirst = false;
						}
						sb.append(NumbericUtils.formatBigDecimal(backBilling
								.getRemainingAmount()) + "追补");
						break;
					}
				}
			} else {
				sb.append("追补");
			}
		}
		return sb.toString();
	}

	private void generateReplyTitle() {
		write.createRow();
		write.setNextStringData("利润中心");
		write.setNextStringData("地区");
		write.setNextStringData("是否分包");
		write.setNextStringData("WBS 元素");
		write.setNextStringData("ValCOArCur");
		write.setNextStringData("分包费预提余额");
		write.setNextStringData("其他成本预提余额");
		write.setNextStringData("余额不足金额");
		write.setNextStringData("如何处理");
		write.setNextStringData("项目名称");
	}

	private void generateReplayDetail(BackBilling b) {
		if (NumbericUtils.isBigDecimalEqual0(b.getValCOArCur())) {
			return;
		}
		write.createRow();
		write.setNextStringData(b.getPr());
		write.setNextStringData(b.getLocation());
		boolean isSplitPackage = b.isSplitPackage();
		write.setNextStringData(isSplitPackage ? "分包" : "非分包");
		write.setNextStringData(b.getWbsNo());
		write.setNextNumbericData(b.getValCOArCur());
		BigDecimal remaining = b.getValCOArCur();
		if (isSplitPackage) {
			Listing l = listings2192041.get(b.getWbsNo());
			if (l != null) {
				if (Math.round(l.getAmount().doubleValue() * 100) != 0) {
					write.setNextNumbericData(l.getAmount());
					remaining = l.getAmount().add(remaining);
				} else {
					write.skipCell(1);
				}
			} else {
				write.skipCell(1);
			}
		} else {
			write.skipCell(1);
		}

		if (isSplitPackage) {
			write.skipCell(1);
		} else {
			Listing l = listings2193000.get(b.getWbsNo());
			if (l != null) {
				if (Math.round(l.getAmount().doubleValue() * 100) != 0) {
					write.setNextNumbericData(l.getAmount());
					remaining = l.getAmount().add(remaining);
				} else {
					write.skipCell(1);
				}
			} else {
				write.skipCell(1);
			}
		}

		if (remaining.intValue() > 0) {
			write.setNextNumbericData(remaining);
		} else {
			write.skipCell(1);
		}
		write.skipCell(1);
		write.setNextStringData(b.getProjectName());

	}

	@Override
	public void generateReplyVoucher(String month, String postingDate) {
		this.month = month;
		this.postingDate = postingDate;
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");
		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();
		dateFormat = new SimpleDateFormat("yyMM");
		fileName += "/追补记账-" + dateFormat.format(new Date()) + ".xls";
		try {
			write = new POIWriter(fileName);

			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"开始生成追补记账表...");

			int index = 0;
			BackBilling backBilling = null;
			BackBilling otherBackBilling = null;
			SimpleDateFormat format = new SimpleDateFormat("yy");
			String year = format.format(new Date());
			String docHeadText = "";
			String debitNo = "";
			String creditNo = "";
			String setupCostDocHeadText = year
					+ StringUtils.padLeft(this.month, 2, "0") + "##其他安装成本追补释放";
			String splitPackageDocHeadText = year
					+ StringUtils.padLeft(this.month, 2, "0") + "##分包费追补释放";
			BigDecimal normalRemainingValue = null;
			BigDecimal specialRemainingValue = null;
			Listing listing = null;
			HashMap<String, Listing> listingMap = null;
			HashMap<String, TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>>> locationMap = null;
			TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>> splitPackageMap = null;
			TreeMap<String, TreeMap<String, BackBilling>> projectNoMap = null;
			TreeMap<String, BackBilling> wbsNoMap = null;
			List<BackBilling> normalBackBillingList = new ArrayList<BackBilling>();
			List<BackBilling> specialBackBillingList = new ArrayList<BackBilling>();
			for (String pr : backBillingReplys.keySet()) {
				write.createNewSheet("追补记账-" + pr);
				this.generateReplyVoucherTitle();
				locationMap = backBillingReplys.get(pr);
				for (String location : locationMap.keySet()) {
					splitPackageMap = locationMap.get(location);
					for (Boolean b : splitPackageMap.keySet()) {
						projectNoMap = splitPackageMap.get(b);
						index = 0;
						for (String projectNo : projectNoMap.keySet()) {
							index += 1;
							normalBackBillingList.clear();
							specialBackBillingList.clear();
							wbsNoMap = projectNoMap.get(projectNo);
							// 循环遍历一个项目号的wbsNo
							for (String wbsNo : wbsNoMap.keySet()) {
								// 取出wbsNo的回复对象
								backBilling = wbsNoMap.get(wbsNo);
								if (backBilling.getValCOArCur().doubleValue() < 0) {
									continue;
								}
								// 设置回复对象的项目集合为当前的项目集合
								backBilling.setReferenceWbsNoMap(wbsNoMap);
								listingMap = null;
								String handleText = backBilling.getHandleText()
										.trim();
								if (handleText.equals("")
										|| handleText.equals("0")) {
									continue;
								}
								// 分割“如何处理”里填写的wbsNo
								handleText = handleText.replaceAll("－", "-")
										.replaceAll("，", ",");

								String[] textArr = handleText.split("-");
								// 如果“如何处理”填写的是“冲其他安装成本预提”
								if (textArr[0].indexOf("冲其他安装成本预提") > -1) {
									// 设置回复对象的类型为“普通安装成本”
									backBilling
											.setType(BackBilling.TYPE_SETUP_COST_NORMAL);
									debitNo = "2193000";
									creditNo = "4292401";

									// 取得回复对象需要冲的数目
									if (backBilling.getValCOArCur().abs()
											.doubleValue() <= backBilling
											.getOtherRemaining().abs()
											.doubleValue()) {
										normalRemainingValue = backBilling
												.getValCOArCur();
									} else {
										normalRemainingValue = backBilling
												.getOtherRemaining().abs();
									}

									// 取出当前回复对象的清单集合
									listingMap = listings2193000Map
											.get(backBilling.getWbsNo()
													.substring(0, 5));
									// 如果回复对象是“分包”
									if (backBilling.isSplitPackage()) {
										try {
											// 是“分包”的回复对象需要使用其他安装成本对应的项目冲，
											// 所以要把当前回复对象的项目集合设置为和其他安装成本一样项目号的项目集合
											backBilling
													.setReferenceWbsNoMap(backBillingReplys
															.get(backBilling
																	.getPr())
															.get(backBilling
																	.getLocation())
															.get(Boolean.FALSE)
															.get(backBilling
																	.getWbsNo()
																	.substring(
																			0,
																			5)));
										} catch (Exception ex) {
											MessageProvider
													.getInstance()
													.publicMessage(
															Message.ERROR,
															"分包的"
																	+ backBilling
																			.getWbsNo()
																	+ "需要冲其他安装成本，但在非分包中找不到对应的项目！");
											continue;
										}
									}
								} else if (textArr[0].indexOf("冲分包费预提") > -1) {// 如果“如何处理”是冲分包费预提
									// 设置回复对象的类型为普通分包费预提
									backBilling
											.setType(BackBilling.TYPE_SPLIT_BILLING_NORMAL);
									debitNo = "2192041";
									creditNo = "4411010";
									// 如果回复对象的ValCOArCur小于回复对象的分包费预提余额，则普通冲的数量取ValCOArCur，否则取分包费预提余额
									if (backBilling.getValCOArCur().abs()
											.doubleValue() < backBilling
											.getSplitRemaining().abs()
											.doubleValue()) {
										normalRemainingValue = backBilling
												.getValCOArCur();
									} else {
										normalRemainingValue = backBilling
												.getSplitRemaining().abs();
									}

									// 取当前回复对象的清单集合
									listingMap = listings2192041Map
											.get(backBilling.getWbsNo()
													.substring(0, 5));
								} else if (textArr[0].indexOf("追补") > -1) { // 如果是追补，就什么都不做
									continue;
									// backBilling
									// .setType(BackBilling.TYPE_BACK_BILLING);
									// normalRemainingValue = backBilling
									// .getSplitRemaining();
									// debitNo = "2192041";
									// creditNo = "4411010";
									// if (normalRemainingValue.intValue() == 0)
									// {
									// normalRemainingValue = backBilling
									// .getOtherRemaining();
									// debitNo = "2193000";
									// creditNo = "4292401";
									// }
									// if (normalRemainingValue.intValue() == 0)
									// {
									// continue;
									// }
								} else {
									MessageProvider.getInstance()
											.publicMessage(
													Message.ERROR,
													pr
															+ "中的"
															+ backBilling
																	.getWbsNo()
															+ "回复文本不符合规范！");
									continue;
								}

								// 根据回复对象是不是分包，设置docHeadText
								if (!backBilling.isSplitPackage()) {
									docHeadText = setupCostDocHeadText.replace(
											"##", backBilling.getLocation());
								} else {
									docHeadText = splitPackageDocHeadText
											.replace("##",
													backBilling.getLocation());
								}

								String[] wbsArr = null;
								// 如果分割的如何处理长度大于1，说明有填写冲其他的wbsNo
								if (textArr.length > 1) {
									// 分割需要冲其他的wbsNo
									wbsArr = textArr[1].split(",");
									if (wbsArr.length > 1
											|| (!wbsArr[0].equals(wbsNo) && wbsArr[0]
													.indexOf("追补") < 0)) {
										// 把其他wbsNo数组设置到回复对象
										backBilling.setOtherCostWbsNo(wbsArr);
										// 如果回复对象的类型是普通安装成本
										if (backBilling.getType() == BackBilling.TYPE_SETUP_COST_NORMAL) {
											// 设置回复对象的类型为“冲其他安装成本”
											backBilling
													.setType(BackBilling.TYPE_SETUP_COST_OTHER_COST);
											// 特殊回复对象是分包的，又需要冲其他安装成本，则不冲两个余额
											if (backBilling.isSplitPackage()) {
												// 设置特殊回复对象还需冲的余额
												backBilling
														.setRemainingAmount(backBilling
																.getNotEnoughRemaining());
											} else {
												// 如果回复对象是非分包的，又需要冲其他安装成本，并且其他成本预提余额有，就先冲其他成本预提余额
												if (NumbericUtils
														.isBigDecimalLessThan0(backBilling
																.getOtherRemaining())) {
													this.writeReplayVoucher(
															index,
															docHeadText,
															backBilling
																	.getLocation(),
															backBilling
																	.getOtherRemaining(),
															backBilling
																	.getWbsNo(),
															backBilling
																	.getProjectName(),
															debitNo, creditNo);

													// 设置特殊回复对象还需冲的余额
													backBilling
															.setRemainingAmount(backBilling
																	.getNotEnoughRemaining());
													// 更新被冲的回复对象wbsNo对应的清单的余额，以便让其他wbsNo冲时，用余额冲
													this.subtractListing(
															listingMap,
															backBilling
																	.getWbsNo(),
															backBilling
																	.getOtherRemaining()
																	.abs());
													// 因为已经冲掉了自己的余额，所以可以把自己的号从回复里删除掉
													backBilling
															.getOtherCostWbsNo()[0] = "";

												} else {
													backBilling
															.setRemainingAmount(backBilling
																	.getValCOArCur());
												}
											}

										} else if (backBilling.getType() == BackBilling.TYPE_SPLIT_BILLING_NORMAL) {
											// 如果回复对象类型是普通分包费预提，则设置回复对象类型为“其他分包费预提”
											backBilling
													.setType(BackBilling.TYPE_SPLIT_OTHER_BILLING);

											// 如果特殊回复对象的分包费预提余额有，先冲掉分包费预提余额
											if (NumbericUtils
													.isBigDecimalLessThan0(backBilling
															.getSplitRemaining())) {
												// 冲分包费预提余额
												this.writeReplayVoucher(
														index,
														docHeadText,
														backBilling
																.getLocation(),
														backBilling
																.getSplitRemaining(),
														backBilling.getWbsNo(),
														backBilling
																.getProjectName(),
														debitNo, creditNo);
												// 设置特殊回复对象还需冲的余额
												backBilling
														.setRemainingAmount(backBilling
																.getNotEnoughRemaining());
												// 更新清单文件的余额
												this.subtractListing(
														listingMap,
														backBilling.getWbsNo(),
														backBilling
																.getSplitRemaining()
																.abs());
												// 因为已经冲掉了自己的余额，所以可以把自己的号从回复里删除掉
												backBilling.getOtherCostWbsNo()[0] = "";
											} else {
												backBilling
														.setRemainingAmount(backBilling
																.getValCOArCur());
											}
										}

										// 把特殊的回复对象加入到特殊回复对象列表里
										specialBackBillingList.add(backBilling);
										continue;
									}
								}

								// 把普通的回复对象生成凭证
								this.writeReplayVoucher(index, docHeadText,
										backBilling.getLocation(),
										normalRemainingValue,
										backBilling.getWbsNo(),
										backBilling.getProjectName(), debitNo,
										creditNo);
								// 更新普通对象对应的清单文件里对应的剩余数，因为现在已经冲了，如果其他要用到它冲，只能用余额冲。
								this.subtractListing(listingMap,
										backBilling.getWbsNo(),
										normalRemainingValue);
								// 设置回复对象的需冲余额为回复对象的ValCOArCur与其他两个余额相加
								backBilling
										.setRemainingAmount(normalRemainingValue
												.add(backBilling
														.getOtherRemaining())
												.add(backBilling
														.getSplitRemaining()));
								// 把普通的回复对象加入到普通回复对象列表里
								normalBackBillingList.add(backBilling);
							}
							// 遍历特殊回复对象列表
							for (BackBilling special : specialBackBillingList) {
								// 如果特殊回复对象已经没有需要冲的余额了，就跳过
								if (NumbericUtils.isBigDecimalLessThan0(special
										.getRemainingAmount())) {
									continue;
								}
								// 如果特殊回复对象的类型是特殊安装成本
								if (special.getType() == BackBilling.TYPE_SETUP_COST_OTHER_COST) {
									// 取出特殊回复对象对应的清单列表
									listingMap = listings2193000Map.get(special
											.getWbsNo().substring(0, 5));
									// 特殊回复对象是分包的，又需要冲其他安装成本，则不冲两个余额
									// if (special.isSplitPackage()) {
									//
									// } else {
									// //
									// 如果回复对象是非分包的，又需要冲其他安装成本，并且其他成本预提余额有，就先冲其他成本预提余额
									// if (NumbericUtils
									// .isBigDecimalLessThan0(special
									// .getOtherRemaining())) {
									// this.writeReplayVoucher(
									// index,
									// docHeadText,
									// special.getLocation(),
									// special.getOtherRemaining(),
									// special.getWbsNo(),
									// special.getProjectName(),
									// debitNo, creditNo);
									// special.setRemainingAmount(special
									// .getRemainingAmount()
									// .add(special
									// .getOtherRemaining()));
									// //
									// 更新被冲的回复对象wbsNo对应的清单的余额，以便让其他wbsNo冲时，用余额冲
									// this.subtractListing(listingMap,
									// special.getWbsNo(),
									// special.getOtherRemaining()
									// .abs());
									//
									// }
									// }
								} else if (special.getType() == BackBilling.TYPE_SPLIT_OTHER_BILLING) {
									// 如果特殊回复对象是冲分包费预提
									// 取出特殊回复对象的清单列表
									listingMap = listings2192041Map
											.get(backBilling.getWbsNo()
													.substring(0, 5));
									// 如果特殊回复对象的分包费预提余额有，先冲掉分包费预提余额
									// if
									// (special.getSplitRemaining().intValue() <
									// 0) {
									// // 冲分包费预提余额
									// this.writeReplayVoucher(index,
									// docHeadText,
									// special.getLocation(),
									// special.getSplitRemaining(),
									// special.getWbsNo(),
									// special.getProjectName(),
									// debitNo, creditNo);
									// // 更新清单文件的余额
									// this.subtractListing(listingMap,
									// special.getWbsNo(), special
									// .getSplitRemaining()
									// .abs());
									// }
									// // 设置特殊回复对象还需冲的余额
									// special.setRemainingAmount(special
									// .getNotEnoughRemaining());
								}

								boolean isZhuiBu = false;
								// 遍历“如何处理”里填写的wbsNo
								for (String otherCostWbsNo : special
										.getOtherCostWbsNo()) {
									// 如果等于""，说明自己的数之前已经被冲了
									if (otherCostWbsNo.equals("")) {
										continue;
									}
									// 如果是自己的wbsNo，则跳过
									// if
									// (otherCostWbsNo.equalsIgnoreCase(special
									// .getWbsNo())) {
									// continue;
									// }
									// 如果特殊回复对象已经没有余额可冲了，就跳出
									if (special.getRemainingAmount()
											.doubleValue() < 0.0001) {
										break;
									}
									if (otherCostWbsNo.indexOf("追补") > -1) {
										isZhuiBu = true;
										continue;
									}
									// 如果“如何处理”填写的wbsNo与当前特殊回复对象的wbsNo不是同
									// 一个项目号，由提示
									if (!otherCostWbsNo.substring(0, 5).equals(
											special.getWbsNo().substring(0, 5))) {
										MessageProvider
												.getInstance()
												.publicMessage(
														Message.WARNING,
														"\"如何处理\"填写的WBS号与项目不对应:"
																+ otherCostWbsNo);
										continue;
									}
									// 初始化特殊回复对象
									otherBackBilling = null;
									// 根据填写的wbsNo从特殊回复对象的项目集合中取出其他回复对象
									if (special.getReferenceWbsNoMap() != null) {
										otherBackBilling = special
												.getReferenceWbsNoMap().get(
														otherCostWbsNo);
									}
									// 如果取不出，说明在回复表里找不到这个wbsNo，则需要从清单文件里找
									if (otherBackBilling == null) {
										MessageProvider
												.getInstance()
												.publicMessage(
														new Message(
																Message.INFO,
																otherCostWbsNo
																		+ "在回复表里找不到，需要到清单表里找-----开始",
																true));
										// 根据类型取清单列表
										if (special.getType() == BackBilling.TYPE_SETUP_COST_OTHER_COST) {
											listingMap = listings2193000Map
													.get(otherCostWbsNo
															.substring(0, 5));
										} else if (special.getType() == BackBilling.TYPE_SPLIT_OTHER_BILLING) {
											listingMap = listings2192041Map
													.get(otherCostWbsNo
															.substring(0, 5));
										}
										// 从清单列表里找其他回复对象
										boolean isOtherBackBillingFound = false;
										if (listingMap != null) {
											// 找到清单
											listing = listingMap
													.get(otherCostWbsNo);
											// 如果清单的数已经不能冲了，继续找下一个wbsNo冲
											if (listing != null) {
												if (listing.getAmount()
														.doubleValue() >= 0) {
													continue;
												}
												isOtherBackBillingFound = true;
												// 根据清单构建一个回复对象
												otherBackBilling = new BackBilling(
														listing.getWbsNo());
												// 设置新建的回复对象的余额为清单的数
												otherBackBilling
														.setRemainingAmount(listing
																.getAmount());
												// 设置新建的回复对象引用哪个清单
												otherBackBilling
														.setReferenceListing(listing);
												// hasUsedFromListings.put(
												// otherCostWbsNo,
												// otherBackBilling);
												MessageProvider
														.getInstance()
														.publicMessage(
																new Message(
																		Message.INFO,
																		otherCostWbsNo
																				+ "在回复表里找不到，需要到清单表里找-----找到"
																				+ listing
																						.getWbsNo(),
																		true));
											}
										}
										MessageProvider
												.getInstance()
												.publicMessage(
														new Message(
																Message.INFO,
																otherCostWbsNo
																		+ "在回复表里找不到，需要到清单表里找-----结束",
																true));

										// 如果从清单列表里找不到清单，给出提示
										if (!isOtherBackBillingFound) {
											MessageProvider
													.getInstance()
													.publicMessage(
															Message.ERROR,
															"根据\"如何处理\"填写的WBS号在清单文件里找不到对应的记录:"
																	+ otherCostWbsNo);
											continue;
										}
									} else {
										// 从回复表里可以找到“如何回复”填写的wbsNo对应的回复对象
										// 如果找到的回复对象可冲余额为空，说明找到的回复对象是自己都需要冲，但还没有冲，
										// 所以把可冲余额设置为ValCOArCur与其他两个余额的和
										if (otherBackBilling
												.getRemainingAmount() == null) {
											otherBackBilling
													.setRemainingAmount(otherBackBilling
															.getValCOArCur()
															.add(otherBackBilling
																	.getSplitRemaining())
															.add(otherBackBilling
																	.getOtherRemaining()));
											// if
											// (NumbericUtils.isBigDecimalEqual0(otherBackBilling
											// .getRemainingAmount())) {
											// continue;
											// }
										}
										otherBackBilling
												.setReferenceListing(listingMap
														.get(otherCostWbsNo));
									}
									// 如果这个回复对象已经没有可冲余额，说明这个wbsNo自己冲完就没余额了，需要跳过，找一个填写的wbsNo
									if (NumbericUtils
											.isBigDecimalEqual0(otherBackBilling
													.getRemainingAmount())) {
										continue;
									}
									// 如果特殊回复对象要冲的数比找到的回复对象可冲的数大，说明这个回复对象冲了以后，还是不够冲
									if (special.getRemainingAmount()
											.doubleValue() > otherBackBilling
											.getRemainingAmount().abs()
											.doubleValue()) {
										// 设置这次冲的数为找到的回复对象
										specialRemainingValue = otherBackBilling
												.getRemainingAmount();
										// 把特殊回复对象的余额更新为减去找到回复对象可冲余额
										special.setRemainingAmount(special
												.getRemainingAmount()
												.add(otherBackBilling
														.getRemainingAmount()));
										// 把找到的回复对象可冲余额更新为0
										otherBackBilling
												.setRemainingAmount(new BigDecimal(
														0));
									} else {
										// 如果特殊回复对象要冲的数比找到的回复对象要冲的数小，说明这个回复对象够冲了
										// 设置这次冲的数为特殊对象要冲的数
										specialRemainingValue = special
												.getRemainingAmount();
										// 更新找到的回复对象可冲余额为减去特殊对象要冲的数
										otherBackBilling
												.setRemainingAmount(special
														.getRemainingAmount()
														.add(otherBackBilling
																.getRemainingAmount()));
										// 设置特殊回复对象要冲的数为0
										special.setRemainingAmount(new BigDecimal(
												0));
									}
									// 如果找到的回复对象是从清单列表里找到的，则还需要更新清单列表里清单的可冲余额数为找到的回复对象可冲余额
									if (otherBackBilling.getReferenceListing() != null) {
										otherBackBilling
												.getReferenceListing()
												.setAmount(
														otherBackBilling
																.getRemainingAmount());
									}
									// 冲掉这次找到的数
									this.writeReplayVoucher(index, docHeadText,
											special.getLocation(),
											specialRemainingValue,
											otherBackBilling.getWbsNo(),
											special.getProjectName(), debitNo,
											creditNo);
								}
								// 根据“如何回复”里填写的wbsNo冲完后，特殊回复对象还有余额要冲
								if (special.getRemainingAmount().doubleValue() > 0) {
									MessageProvider
											.getInstance()
											.publicMessage(
													new Message(
															Message.INFO,
															special.getWbsNo()
																	+ "用回复表指定的wbs号不够冲，需要从清单文件里另找其他wbs号冲=====开始",
															true));
									// 循环，直到特殊回复对象没有余额要冲
									while (special.getRemainingAmount()
											.doubleValue() > 0) {
										// 在清单列表里查询余额最大的清单，并封装成回复对象
										otherBackBilling = this
												.getLessestBackBilling(special,
														normalBackBillingList,
														specialBackBillingList);
										// 如果能找到可冲的回复对象
										if (otherBackBilling != null) {
											// 如果特殊回复对象要冲的数大于找到的回复对象可冲的数
											if (special.getRemainingAmount()
													.doubleValue() > otherBackBilling
													.getRemainingAmount().abs()
													.doubleValue()) {
												// 把这次冲的数设置为找到的回复对象的数
												specialRemainingValue = otherBackBilling
														.getRemainingAmount();
												// 设置特殊回复对象剩余要冲余额为减去找到的回复对象的数
												special.setRemainingAmount(special
														.getRemainingAmount()
														.add(otherBackBilling
																.getRemainingAmount()));
												// 设置引用的清单的数为0
												otherBackBilling
														.getReferenceListing()
														.setAmount(
																new BigDecimal(
																		0));
											} else {
												// 如果特殊回复对象要冲的数小于等于找到的回复对象可冲的数，
												// 把这次要冲的数设置为特殊回复对象要冲的数
												specialRemainingValue = special
														.getRemainingAmount();
												// 设置找到的回复对象剩余可冲为减去特殊回复对象的数
												otherBackBilling
														.getReferenceListing()
														.setAmount(
																special.getRemainingAmount()
																		.add(otherBackBilling
																				.getRemainingAmount()));
												// 把特殊回复对象要冲余额为0
												special.setRemainingAmount(new BigDecimal(
														0));
											}
											// 冲掉这次的数
											this.writeReplayVoucher(
													index,
													docHeadText,
													special.getLocation(),
													specialRemainingValue,
													otherBackBilling.getWbsNo(),
													special.getProjectName(),
													debitNo, creditNo);
											MessageProvider
													.getInstance()
													.publicMessage(
															new Message(
																	Message.INFO,
																	special.getWbsNo()
																			+ "用回复表指定的wbs号不够冲，需要从清单文件里另找其他wbs号冲=====找到了："
																			+ otherBackBilling
																					.getWbsNo(),
																	true));
										} else {
											MessageProvider
													.getInstance()
													.publicMessage(
															Message.ERROR,
															"还有没有冲完的数，但已经找不到可用于冲的项："
																	+ special
																			.getWbsNo()
																	+ "("
																	+ (isZhuiBu ? "追补"
																			: "")
																	+ ")");
											break;
										}
									}
									MessageProvider
											.getInstance()
											.publicMessage(
													new Message(
															Message.INFO,
															special.getWbsNo()
																	+ "用回复表指定的wbs号不够冲，需要从清单文件里另找其他wbs号冲=====结束",
															true));
								}
								// 把特殊回复对象引用的对象为null
								special.setReferenceListing(null);
								special.setReferenceWbsNoMap(null);
							}
						}
					}
				}
			}
			write.flush();
			MessageProvider.getInstance().publicMessage(Message.INFO,
					"追补记账表生成成功!");
			FileUtils.popupFilePath("/" + POIWriter.DEFAULT_FOLDER + fileName);
		} catch (Exception e) {
			e.printStackTrace();
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"生成追补记账时出现错误：" + e.getMessage());
		} finally {
			try {
				write.destroy();
			} catch (IOException e) {
				e.printStackTrace();
			}
			CallListener.getInstance().notifyBackBillingFinish();
		}
	}

	private void subtractListing(HashMap<String, Listing> listingMap,
			String wbsNo, BigDecimal val) {
		if (listingMap == null) {
			return;
		}
		Listing l = listingMap.get(wbsNo);
		if (l != null) {
			if (l.getAmount().abs().doubleValue() > val.doubleValue()) {
				l.setAmount(l.getAmount().add(val));
			} else {
				l.setAmount(new BigDecimal(0));
			}
		}
	}

	private BackBilling getLessestBackBilling(BackBilling special,
			List<BackBilling> normalBackBilling,
			List<BackBilling> specialBackBilling) {
		HashMap<String, Listing> listingMap = null;
		if (special.getType() == BackBilling.TYPE_SETUP_COST_OTHER_COST) {
			listingMap = listings2193000Map.get(special.getWbsNo().substring(0,
					5));
		} else {
			listingMap = listings2192041Map.get(special.getWbsNo().substring(0,
					5));
		}
		Listing listing = null;
		Listing lessest = null;
		if (listingMap != null) {
			for (String wbsNo : listingMap.keySet()) {
				if (wbsNo.equalsIgnoreCase(special.getWbsNo())) {
					continue;
				}
				listing = listingMap.get(wbsNo);

				if (lessest == null) {
					lessest = listing;
				} else {
					if (listing.getAmount().doubleValue() < lessest.getAmount()
							.doubleValue()) {
						lessest = listing;
					}
				}
			}
		}
		if (lessest != null
				&& NumbericUtils.isBigDecimalGreatherThan0(lessest.getAmount()
						.abs())) {
			BackBilling b = new BackBilling(lessest.getWbsNo());
			b.setRemainingAmount(lessest.getAmount());
			b.setReferenceListing(lessest);
			return b;
		}
		return null;
	}

	private void writeReplayVoucher(int index, String headText,
			String location, BigDecimal amount, String wbsNo,
			String projectName, String debitNo, String creditNo) {
		write.createRow();
		write.setNextStringData(index + "");
		write.setNextStringData(this.postingDate);
		write.setNextStringData(this.postingDate);
		write.skipCell(3);
		String docHeadText = headText.replace("##", location);
		write.setNextStringData(docHeadText);
		write.setNextStringData("40");
		write.setNextStringData(debitNo);
		write.setNextNumbericData(amount.abs());
		write.skipCell(5);
		write.setNextStringData(wbsNo);
		write.setNextStringData(docHeadText + "-" + projectName);

		write.createRow();
		write.setNextStringData(index + "");
		write.setNextStringData(this.postingDate);
		write.setNextStringData(this.postingDate);
		write.skipCell(3);
		write.setNextStringData(docHeadText);
		write.setNextStringData("50");
		write.setNextStringData(creditNo);
		write.setNextNumbericData(amount.abs());
		write.skipCell(4);
		write.setNextStringData(wbsNo);
		write.skipCell(1);
		write.setNextStringData(docHeadText + "-" + projectName);
	}

	private void generateReplyVoucherTitle() {
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

	@Override
	public void loadReply(String replyFile, String list2192041File,
			String list2193000File) {
		CallListener.getInstance().notifyBackBillingStart();
		listings2192041Map = new HashMap<String, HashMap<String, Listing>>();
		listings2193000Map = new HashMap<String, HashMap<String, Listing>>();

		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"开始加载2192041清单文件...");
		this.loadReplyAccrualFile(listings2192041Map, list2192041File);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2192041清单文件已加载完成。");
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"开始加载2193000清单文件...");
		this.loadReplyAccrualFile(listings2193000Map, list2193000File);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2193000清单文件已加载完成。");

		reader = new POIReader(replyFile);
		reader.loadSheet(0);
		reader.skipRow(1); // 跳过标题

		String wbsNo = null;
		String projectNo = null;
		String pr = null;
		String location = null;
		String splitPackage = null;
		Boolean isSplitPackage = null;
		BackBilling backBilling = null;
		HashMap<String, TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>>> locationMap = null;
		TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>> splitPackageMap = null;
		TreeMap<String, TreeMap<String, BackBilling>> projectNoMap = null;
		TreeMap<String, BackBilling> wbsNoMap = null;
		while (reader.hasRow()) {
			reader.nextRow();

			pr = reader.getNextStringFormat();
			if (pr == null || pr.equals("") || pr.equals("总计")) {
				break;
			}

			locationMap = backBillingReplys.get(pr);
			if (locationMap == null) {
				locationMap = new HashMap<String, TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>>>();
				backBillingReplys.put(pr, locationMap);
			}

			location = reader.getNextStringFormat();
			splitPackageMap = locationMap.get(location);
			if (splitPackageMap == null) {
				splitPackageMap = new TreeMap<Boolean, TreeMap<String, TreeMap<String, BackBilling>>>();
				locationMap.put(location, splitPackageMap);
			}

			splitPackage = reader.getNextStringFormat();
			isSplitPackage = splitPackage.equals("分包");
			if (isSplitPackage) {
				projectNoMap = splitPackageMap.get(Boolean.TRUE);
			} else {
				projectNoMap = splitPackageMap.get(Boolean.FALSE);
			}
			if (projectNoMap == null) {
				projectNoMap = new TreeMap<String, TreeMap<String, BackBilling>>();
				splitPackageMap.put(isSplitPackage, projectNoMap);
			}

			wbsNo = reader.getNextStringFormat().trim();
			projectNo = wbsNo.substring(0, 5);
			wbsNoMap = projectNoMap.get(projectNo);
			if (wbsNoMap == null) {
				wbsNoMap = new TreeMap<String, BackBilling>();
				projectNoMap.put(projectNo, wbsNoMap);
			}
			backBilling = new BackBilling(wbsNo);
			backBilling.setPr(pr);
			backBilling.setLocation(location);
			backBilling.setSplitPackage(isSplitPackage);
			backBilling.setValCOArCur(reader.getNextBigDecimalFormat());
			backBilling.setSplitRemaining(reader.getNextBigDecimalFormat());
			backBilling.setOtherRemaining(reader.getNextBigDecimalFormat());
			backBilling.setNotEnoughRemaining(reader.getNextBigDecimalFormat());
			backBilling.setHandleText(reader.getNextStringFormat());
			backBilling.setProjectName(reader.getNextStringFormat());
			wbsNoMap.put(wbsNo, backBilling);
		}

		reader.destroy();
	}

}
