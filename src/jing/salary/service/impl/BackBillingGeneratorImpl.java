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
	// ���ݸ�ʽ��{"�Ƿ�ְ�":{"WBSNO":BackBilling}}
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
				"��ʼ����2192041�嵥�ļ�...");
		this.loadAccrualFile(listings2192041, list2192041File,
				splitListingProjects);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2192041�嵥�ļ��Ѽ�����ɡ�");
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"��ʼ����2193000�嵥�ļ�...");
		this.loadAccrualFile(listings2193000, list2193000File,
				notSplitListingProjects);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2193000�嵥�ļ��Ѽ�����ɡ�");

		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"��ʼ����׷���嵥�ļ�...");
		reader = new POIReader(backBillingListFile);
		reader.loadSheet(0);
		reader.skipRow(1); // ��������

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
				reader.skipCell(1); // ����wbsNo��
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
					"׷���嵥�ļ�����ʧ��:" + e.getMessage());
		} finally {
			reader.destroy();
		}
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"׷���嵥�ļ��Ѽ�����ɡ�");
	}

	private void loadAccrualFile(HashMap<String, Listing> listings,
			String fileName,
			HashMap<String, HashMap<String, Listing>> listProjects) {
		reader = new POIReader(fileName);
		reader.loadSheet(0);
		reader.skipRow(1); // ��������

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
		reader.skipRow(1); // ��������

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
		fileName += "/׷���嵥-" + dateFormat.format(new Date())
				+ StringUtils.padLeft(this.month, 2, "0") + ".xls";
		try {
			write = new POIWriter(fileName);

			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"��ʼ���ɻ��ܻظ���...");
			write.createNewSheet("���ܻظ���");
			this.generateReplyTitle();

			TreeMap<String, TreeMap<String, BackBilling>> notSplitPackage = backBillings
					.get(Boolean.FALSE);

			this.generateNotSplitPackageReplayDetail(notSplitPackage);

			TreeMap<String, TreeMap<String, BackBilling>> splitPackage = backBillings
					.get(Boolean.TRUE);
			this.generateSplitPackageReplayDetail(splitPackage);
			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"���ܻظ��������ɡ�");

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
				write.setNextStringData("�ְ�");
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
				write.setNextStringData("��ְ���Ԥ��-" + wbsNo);
				write.setNextStringData(b.getProjectName());
			}

			if (hasRemaining.size() > 0) {
				for (BackBilling bb : hasRemaining) {
					write.createRow();
					write.setNextStringData(bb.getPr());
					write.setNextStringData(bb.getLocation());
					write.setNextStringData("�ְ�");
					write.setNextStringData(bb.getWbsNo());
					write.setNextNumbericData(bb.getValCOArCur());
					write.setNextNumbericData(bb.getListAmount());
					write.skipCell(1);
					write.setNextNumbericData(bb.getRemainingAmount());
					String replyString = getReplyString(bb,
							splitListingProjects);
					if (replyString.split(",").length == 1
							&& replyString.endsWith("׷��")) {
						write.setNextStringData("׷��");
					} else {
						write.setNextStringData("��ְ���Ԥ��-" + replyString);
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
				write.setNextStringData("�Ƿְ�");
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
				write.setNextStringData("��������װ�ɱ�Ԥ��-" + wbsNo);
				write.setNextStringData(b.getProjectName());
			}

			if (hasRemaining.size() > 0) {
				for (BackBilling bb : hasRemaining) {
					write.createRow();
					write.setNextStringData(bb.getPr());
					write.setNextStringData(bb.getLocation());
					write.setNextStringData("�Ƿְ�");
					write.setNextStringData(bb.getWbsNo());
					write.setNextNumbericData(bb.getValCOArCur());
					write.skipCell(1);
					write.setNextNumbericData(bb.getListAmount());
					write.setNextNumbericData(bb.getRemainingAmount());
					String replyString = getReplyString(bb,
							notSplitListingProjects);
					if (replyString.split(",").length == 1
							&& replyString.endsWith("׷��")) {
						write.setNextStringData("׷��");
					} else {
						write.setNextStringData("��������װ�ɱ�Ԥ��-" + replyString);
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
								.getRemainingAmount()) + "׷��");
						break;
					}
				}
			} else {
				sb.append("׷��");
			}
		}
		return sb.toString();
	}

	private void generateReplyTitle() {
		write.createRow();
		write.setNextStringData("��������");
		write.setNextStringData("����");
		write.setNextStringData("�Ƿ�ְ�");
		write.setNextStringData("WBS Ԫ��");
		write.setNextStringData("ValCOArCur");
		write.setNextStringData("�ְ���Ԥ�����");
		write.setNextStringData("�����ɱ�Ԥ�����");
		write.setNextStringData("������");
		write.setNextStringData("��δ���");
		write.setNextStringData("��Ŀ����");
	}

	private void generateReplayDetail(BackBilling b) {
		if (NumbericUtils.isBigDecimalEqual0(b.getValCOArCur())) {
			return;
		}
		write.createRow();
		write.setNextStringData(b.getPr());
		write.setNextStringData(b.getLocation());
		boolean isSplitPackage = b.isSplitPackage();
		write.setNextStringData(isSplitPackage ? "�ְ�" : "�Ƿְ�");
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
		fileName += "/׷������-" + dateFormat.format(new Date()) + ".xls";
		try {
			write = new POIWriter(fileName);

			MessageProvider.getInstance().publicMessage(Message.DEBUG,
					"��ʼ����׷�����˱�...");

			int index = 0;
			BackBilling backBilling = null;
			BackBilling otherBackBilling = null;
			SimpleDateFormat format = new SimpleDateFormat("yy");
			String year = format.format(new Date());
			String docHeadText = "";
			String debitNo = "";
			String creditNo = "";
			String setupCostDocHeadText = year
					+ StringUtils.padLeft(this.month, 2, "0") + "##������װ�ɱ�׷���ͷ�";
			String splitPackageDocHeadText = year
					+ StringUtils.padLeft(this.month, 2, "0") + "##�ְ���׷���ͷ�";
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
				write.createNewSheet("׷������-" + pr);
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
							// ѭ������һ����Ŀ�ŵ�wbsNo
							for (String wbsNo : wbsNoMap.keySet()) {
								// ȡ��wbsNo�Ļظ�����
								backBilling = wbsNoMap.get(wbsNo);
								if (backBilling.getValCOArCur().doubleValue() < 0) {
									continue;
								}
								// ���ûظ��������Ŀ����Ϊ��ǰ����Ŀ����
								backBilling.setReferenceWbsNoMap(wbsNoMap);
								listingMap = null;
								String handleText = backBilling.getHandleText()
										.trim();
								if (handleText.equals("")
										|| handleText.equals("0")) {
									continue;
								}
								// �ָ��δ�������д��wbsNo
								handleText = handleText.replaceAll("��", "-")
										.replaceAll("��", ",");

								String[] textArr = handleText.split("-");
								// �������δ�����д���ǡ���������װ�ɱ�Ԥ�ᡱ
								if (textArr[0].indexOf("��������װ�ɱ�Ԥ��") > -1) {
									// ���ûظ����������Ϊ����ͨ��װ�ɱ���
									backBilling
											.setType(BackBilling.TYPE_SETUP_COST_NORMAL);
									debitNo = "2193000";
									creditNo = "4292401";

									// ȡ�ûظ�������Ҫ�����Ŀ
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

									// ȡ����ǰ�ظ�������嵥����
									listingMap = listings2193000Map
											.get(backBilling.getWbsNo()
													.substring(0, 5));
									// ����ظ������ǡ��ְ���
									if (backBilling.isSplitPackage()) {
										try {
											// �ǡ��ְ����Ļظ�������Ҫʹ��������װ�ɱ���Ӧ����Ŀ�壬
											// ����Ҫ�ѵ�ǰ�ظ��������Ŀ��������Ϊ��������װ�ɱ�һ����Ŀ�ŵ���Ŀ����
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
															"�ְ���"
																	+ backBilling
																			.getWbsNo()
																	+ "��Ҫ��������װ�ɱ������ڷǷְ����Ҳ�����Ӧ����Ŀ��");
											continue;
										}
									}
								} else if (textArr[0].indexOf("��ְ���Ԥ��") > -1) {// �������δ����ǳ�ְ���Ԥ��
									// ���ûظ����������Ϊ��ͨ�ְ���Ԥ��
									backBilling
											.setType(BackBilling.TYPE_SPLIT_BILLING_NORMAL);
									debitNo = "2192041";
									creditNo = "4411010";
									// ����ظ������ValCOArCurС�ڻظ�����ķְ���Ԥ��������ͨ�������ȡValCOArCur������ȡ�ְ���Ԥ�����
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

									// ȡ��ǰ�ظ�������嵥����
									listingMap = listings2192041Map
											.get(backBilling.getWbsNo()
													.substring(0, 5));
								} else if (textArr[0].indexOf("׷��") > -1) { // �����׷������ʲô������
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
															+ "�е�"
															+ backBilling
																	.getWbsNo()
															+ "�ظ��ı������Ϲ淶��");
									continue;
								}

								// ���ݻظ������ǲ��Ƿְ�������docHeadText
								if (!backBilling.isSplitPackage()) {
									docHeadText = setupCostDocHeadText.replace(
											"##", backBilling.getLocation());
								} else {
									docHeadText = splitPackageDocHeadText
											.replace("##",
													backBilling.getLocation());
								}

								String[] wbsArr = null;
								// ����ָ����δ����ȴ���1��˵������д��������wbsNo
								if (textArr.length > 1) {
									// �ָ���Ҫ��������wbsNo
									wbsArr = textArr[1].split(",");
									if (wbsArr.length > 1
											|| (!wbsArr[0].equals(wbsNo) && wbsArr[0]
													.indexOf("׷��") < 0)) {
										// ������wbsNo�������õ��ظ�����
										backBilling.setOtherCostWbsNo(wbsArr);
										// ����ظ��������������ͨ��װ�ɱ�
										if (backBilling.getType() == BackBilling.TYPE_SETUP_COST_NORMAL) {
											// ���ûظ����������Ϊ����������װ�ɱ���
											backBilling
													.setType(BackBilling.TYPE_SETUP_COST_OTHER_COST);
											// ����ظ������Ƿְ��ģ�����Ҫ��������װ�ɱ����򲻳��������
											if (backBilling.isSplitPackage()) {
												// ��������ظ������������
												backBilling
														.setRemainingAmount(backBilling
																.getNotEnoughRemaining());
											} else {
												// ����ظ������ǷǷְ��ģ�����Ҫ��������װ�ɱ������������ɱ�Ԥ������У����ȳ������ɱ�Ԥ�����
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

													// ��������ظ������������
													backBilling
															.setRemainingAmount(backBilling
																	.getNotEnoughRemaining());
													// ���±���Ļظ�����wbsNo��Ӧ���嵥�����Ա�������wbsNo��ʱ��������
													this.subtractListing(
															listingMap,
															backBilling
																	.getWbsNo(),
															backBilling
																	.getOtherRemaining()
																	.abs());
													// ��Ϊ�Ѿ�������Լ��������Կ��԰��Լ��ĺŴӻظ���ɾ����
													backBilling
															.getOtherCostWbsNo()[0] = "";

												} else {
													backBilling
															.setRemainingAmount(backBilling
																	.getValCOArCur());
												}
											}

										} else if (backBilling.getType() == BackBilling.TYPE_SPLIT_BILLING_NORMAL) {
											// ����ظ�������������ͨ�ְ���Ԥ�ᣬ�����ûظ���������Ϊ�������ְ���Ԥ�ᡱ
											backBilling
													.setType(BackBilling.TYPE_SPLIT_OTHER_BILLING);

											// �������ظ�����ķְ���Ԥ������У��ȳ���ְ���Ԥ�����
											if (NumbericUtils
													.isBigDecimalLessThan0(backBilling
															.getSplitRemaining())) {
												// ��ְ���Ԥ�����
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
												// ��������ظ������������
												backBilling
														.setRemainingAmount(backBilling
																.getNotEnoughRemaining());
												// �����嵥�ļ������
												this.subtractListing(
														listingMap,
														backBilling.getWbsNo(),
														backBilling
																.getSplitRemaining()
																.abs());
												// ��Ϊ�Ѿ�������Լ��������Կ��԰��Լ��ĺŴӻظ���ɾ����
												backBilling.getOtherCostWbsNo()[0] = "";
											} else {
												backBilling
														.setRemainingAmount(backBilling
																.getValCOArCur());
											}
										}

										// ������Ļظ�������뵽����ظ������б���
										specialBackBillingList.add(backBilling);
										continue;
									}
								}

								// ����ͨ�Ļظ���������ƾ֤
								this.writeReplayVoucher(index, docHeadText,
										backBilling.getLocation(),
										normalRemainingValue,
										backBilling.getWbsNo(),
										backBilling.getProjectName(), debitNo,
										creditNo);
								// ������ͨ�����Ӧ���嵥�ļ����Ӧ��ʣ��������Ϊ�����Ѿ����ˣ��������Ҫ�õ����壬ֻ�������塣
								this.subtractListing(listingMap,
										backBilling.getWbsNo(),
										normalRemainingValue);
								// ���ûظ������������Ϊ�ظ������ValCOArCur����������������
								backBilling
										.setRemainingAmount(normalRemainingValue
												.add(backBilling
														.getOtherRemaining())
												.add(backBilling
														.getSplitRemaining()));
								// ����ͨ�Ļظ�������뵽��ͨ�ظ������б���
								normalBackBillingList.add(backBilling);
							}
							// ��������ظ������б�
							for (BackBilling special : specialBackBillingList) {
								// �������ظ������Ѿ�û����Ҫ�������ˣ�������
								if (NumbericUtils.isBigDecimalLessThan0(special
										.getRemainingAmount())) {
									continue;
								}
								// �������ظ���������������ⰲװ�ɱ�
								if (special.getType() == BackBilling.TYPE_SETUP_COST_OTHER_COST) {
									// ȡ������ظ������Ӧ���嵥�б�
									listingMap = listings2193000Map.get(special
											.getWbsNo().substring(0, 5));
									// ����ظ������Ƿְ��ģ�����Ҫ��������װ�ɱ����򲻳��������
									// if (special.isSplitPackage()) {
									//
									// } else {
									// //
									// ����ظ������ǷǷְ��ģ�����Ҫ��������װ�ɱ������������ɱ�Ԥ������У����ȳ������ɱ�Ԥ�����
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
									// ���±���Ļظ�����wbsNo��Ӧ���嵥�����Ա�������wbsNo��ʱ��������
									// this.subtractListing(listingMap,
									// special.getWbsNo(),
									// special.getOtherRemaining()
									// .abs());
									//
									// }
									// }
								} else if (special.getType() == BackBilling.TYPE_SPLIT_OTHER_BILLING) {
									// �������ظ������ǳ�ְ���Ԥ��
									// ȡ������ظ�������嵥�б�
									listingMap = listings2192041Map
											.get(backBilling.getWbsNo()
													.substring(0, 5));
									// �������ظ�����ķְ���Ԥ������У��ȳ���ְ���Ԥ�����
									// if
									// (special.getSplitRemaining().intValue() <
									// 0) {
									// // ��ְ���Ԥ�����
									// this.writeReplayVoucher(index,
									// docHeadText,
									// special.getLocation(),
									// special.getSplitRemaining(),
									// special.getWbsNo(),
									// special.getProjectName(),
									// debitNo, creditNo);
									// // �����嵥�ļ������
									// this.subtractListing(listingMap,
									// special.getWbsNo(), special
									// .getSplitRemaining()
									// .abs());
									// }
									// // ��������ظ������������
									// special.setRemainingAmount(special
									// .getNotEnoughRemaining());
								}

								boolean isZhuiBu = false;
								// ��������δ�������д��wbsNo
								for (String otherCostWbsNo : special
										.getOtherCostWbsNo()) {
									// �������""��˵���Լ�����֮ǰ�Ѿ�������
									if (otherCostWbsNo.equals("")) {
										continue;
									}
									// ������Լ���wbsNo��������
									// if
									// (otherCostWbsNo.equalsIgnoreCase(special
									// .getWbsNo())) {
									// continue;
									// }
									// �������ظ������Ѿ�û�����ɳ��ˣ�������
									if (special.getRemainingAmount()
											.doubleValue() < 0.0001) {
										break;
									}
									if (otherCostWbsNo.indexOf("׷��") > -1) {
										isZhuiBu = true;
										continue;
									}
									// �������δ�����д��wbsNo�뵱ǰ����ظ������wbsNo����ͬ
									// һ����Ŀ�ţ�����ʾ
									if (!otherCostWbsNo.substring(0, 5).equals(
											special.getWbsNo().substring(0, 5))) {
										MessageProvider
												.getInstance()
												.publicMessage(
														Message.WARNING,
														"\"��δ���\"��д��WBS������Ŀ����Ӧ:"
																+ otherCostWbsNo);
										continue;
									}
									// ��ʼ������ظ�����
									otherBackBilling = null;
									// ������д��wbsNo������ظ��������Ŀ������ȡ�������ظ�����
									if (special.getReferenceWbsNoMap() != null) {
										otherBackBilling = special
												.getReferenceWbsNoMap().get(
														otherCostWbsNo);
									}
									// ���ȡ������˵���ڻظ������Ҳ������wbsNo������Ҫ���嵥�ļ�����
									if (otherBackBilling == null) {
										MessageProvider
												.getInstance()
												.publicMessage(
														new Message(
																Message.INFO,
																otherCostWbsNo
																		+ "�ڻظ������Ҳ�������Ҫ���嵥������-----��ʼ",
																true));
										// ��������ȡ�嵥�б�
										if (special.getType() == BackBilling.TYPE_SETUP_COST_OTHER_COST) {
											listingMap = listings2193000Map
													.get(otherCostWbsNo
															.substring(0, 5));
										} else if (special.getType() == BackBilling.TYPE_SPLIT_OTHER_BILLING) {
											listingMap = listings2192041Map
													.get(otherCostWbsNo
															.substring(0, 5));
										}
										// ���嵥�б����������ظ�����
										boolean isOtherBackBillingFound = false;
										if (listingMap != null) {
											// �ҵ��嵥
											listing = listingMap
													.get(otherCostWbsNo);
											// ����嵥�����Ѿ����ܳ��ˣ���������һ��wbsNo��
											if (listing != null) {
												if (listing.getAmount()
														.doubleValue() >= 0) {
													continue;
												}
												isOtherBackBillingFound = true;
												// �����嵥����һ���ظ�����
												otherBackBilling = new BackBilling(
														listing.getWbsNo());
												// �����½��Ļظ���������Ϊ�嵥����
												otherBackBilling
														.setRemainingAmount(listing
																.getAmount());
												// �����½��Ļظ����������ĸ��嵥
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
																				+ "�ڻظ������Ҳ�������Ҫ���嵥������-----�ҵ�"
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
																		+ "�ڻظ������Ҳ�������Ҫ���嵥������-----����",
																true));

										// ������嵥�б����Ҳ����嵥��������ʾ
										if (!isOtherBackBillingFound) {
											MessageProvider
													.getInstance()
													.publicMessage(
															Message.ERROR,
															"����\"��δ���\"��д��WBS�����嵥�ļ����Ҳ�����Ӧ�ļ�¼:"
																	+ otherCostWbsNo);
											continue;
										}
									} else {
										// �ӻظ���������ҵ�����λظ�����д��wbsNo��Ӧ�Ļظ�����
										// ����ҵ��Ļظ�����ɳ����Ϊ�գ�˵���ҵ��Ļظ��������Լ�����Ҫ�壬����û�г壬
										// ���԰ѿɳ��������ΪValCOArCur�������������ĺ�
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
									// �������ظ������Ѿ�û�пɳ���˵�����wbsNo�Լ������û����ˣ���Ҫ��������һ����д��wbsNo
									if (NumbericUtils
											.isBigDecimalEqual0(otherBackBilling
													.getRemainingAmount())) {
										continue;
									}
									// �������ظ�����Ҫ��������ҵ��Ļظ�����ɳ������˵������ظ���������Ժ󣬻��ǲ�����
									if (special.getRemainingAmount()
											.doubleValue() > otherBackBilling
											.getRemainingAmount().abs()
											.doubleValue()) {
										// ������γ����Ϊ�ҵ��Ļظ�����
										specialRemainingValue = otherBackBilling
												.getRemainingAmount();
										// ������ظ������������Ϊ��ȥ�ҵ��ظ�����ɳ����
										special.setRemainingAmount(special
												.getRemainingAmount()
												.add(otherBackBilling
														.getRemainingAmount()));
										// ���ҵ��Ļظ�����ɳ�������Ϊ0
										otherBackBilling
												.setRemainingAmount(new BigDecimal(
														0));
									} else {
										// �������ظ�����Ҫ��������ҵ��Ļظ�����Ҫ�����С��˵������ظ����󹻳���
										// ������γ����Ϊ�������Ҫ�����
										specialRemainingValue = special
												.getRemainingAmount();
										// �����ҵ��Ļظ�����ɳ����Ϊ��ȥ�������Ҫ�����
										otherBackBilling
												.setRemainingAmount(special
														.getRemainingAmount()
														.add(otherBackBilling
																.getRemainingAmount()));
										// ��������ظ�����Ҫ�����Ϊ0
										special.setRemainingAmount(new BigDecimal(
												0));
									}
									// ����ҵ��Ļظ������Ǵ��嵥�б����ҵ��ģ�����Ҫ�����嵥�б����嵥�Ŀɳ������Ϊ�ҵ��Ļظ�����ɳ����
									if (otherBackBilling.getReferenceListing() != null) {
										otherBackBilling
												.getReferenceListing()
												.setAmount(
														otherBackBilling
																.getRemainingAmount());
									}
									// �������ҵ�����
									this.writeReplayVoucher(index, docHeadText,
											special.getLocation(),
											specialRemainingValue,
											otherBackBilling.getWbsNo(),
											special.getProjectName(), debitNo,
											creditNo);
								}
								// ���ݡ���λظ�������д��wbsNo���������ظ����������Ҫ��
								if (special.getRemainingAmount().doubleValue() > 0) {
									MessageProvider
											.getInstance()
											.publicMessage(
													new Message(
															Message.INFO,
															special.getWbsNo()
																	+ "�ûظ���ָ����wbs�Ų����壬��Ҫ���嵥�ļ�����������wbs�ų�=====��ʼ",
															true));
									// ѭ����ֱ������ظ�����û�����Ҫ��
									while (special.getRemainingAmount()
											.doubleValue() > 0) {
										// ���嵥�б����ѯ��������嵥������װ�ɻظ�����
										otherBackBilling = this
												.getLessestBackBilling(special,
														normalBackBillingList,
														specialBackBillingList);
										// ������ҵ��ɳ�Ļظ�����
										if (otherBackBilling != null) {
											// �������ظ�����Ҫ����������ҵ��Ļظ�����ɳ����
											if (special.getRemainingAmount()
													.doubleValue() > otherBackBilling
													.getRemainingAmount().abs()
													.doubleValue()) {
												// ����γ��������Ϊ�ҵ��Ļظ��������
												specialRemainingValue = otherBackBilling
														.getRemainingAmount();
												// ��������ظ�����ʣ��Ҫ�����Ϊ��ȥ�ҵ��Ļظ��������
												special.setRemainingAmount(special
														.getRemainingAmount()
														.add(otherBackBilling
																.getRemainingAmount()));
												// �������õ��嵥����Ϊ0
												otherBackBilling
														.getReferenceListing()
														.setAmount(
																new BigDecimal(
																		0));
											} else {
												// �������ظ�����Ҫ�����С�ڵ����ҵ��Ļظ�����ɳ������
												// �����Ҫ���������Ϊ����ظ�����Ҫ�����
												specialRemainingValue = special
														.getRemainingAmount();
												// �����ҵ��Ļظ�����ʣ��ɳ�Ϊ��ȥ����ظ��������
												otherBackBilling
														.getReferenceListing()
														.setAmount(
																special.getRemainingAmount()
																		.add(otherBackBilling
																				.getRemainingAmount()));
												// ������ظ�����Ҫ�����Ϊ0
												special.setRemainingAmount(new BigDecimal(
														0));
											}
											// �����ε���
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
																			+ "�ûظ���ָ����wbs�Ų����壬��Ҫ���嵥�ļ�����������wbs�ų�=====�ҵ��ˣ�"
																			+ otherBackBilling
																					.getWbsNo(),
																	true));
										} else {
											MessageProvider
													.getInstance()
													.publicMessage(
															Message.ERROR,
															"����û�г�����������Ѿ��Ҳ��������ڳ���"
																	+ special
																			.getWbsNo()
																	+ "("
																	+ (isZhuiBu ? "׷��"
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
																	+ "�ûظ���ָ����wbs�Ų����壬��Ҫ���嵥�ļ�����������wbs�ų�=====����",
															true));
								}
								// ������ظ��������õĶ���Ϊnull
								special.setReferenceListing(null);
								special.setReferenceWbsNoMap(null);
							}
						}
					}
				}
			}
			write.flush();
			MessageProvider.getInstance().publicMessage(Message.INFO,
					"׷�����˱����ɳɹ�!");
			FileUtils.popupFilePath("/" + POIWriter.DEFAULT_FOLDER + fileName);
		} catch (Exception e) {
			e.printStackTrace();
			MessageProvider.getInstance().publicMessage(Message.ERROR,
					"����׷������ʱ���ִ���" + e.getMessage());
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
				"��ʼ����2192041�嵥�ļ�...");
		this.loadReplyAccrualFile(listings2192041Map, list2192041File);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2192041�嵥�ļ��Ѽ�����ɡ�");
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"��ʼ����2193000�嵥�ļ�...");
		this.loadReplyAccrualFile(listings2193000Map, list2193000File);
		MessageProvider.getInstance().publicMessage(Message.DEBUG,
				"2193000�嵥�ļ��Ѽ�����ɡ�");

		reader = new POIReader(replyFile);
		reader.loadSheet(0);
		reader.skipRow(1); // ��������

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
			if (pr == null || pr.equals("") || pr.equals("�ܼ�")) {
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
			isSplitPackage = splitPackage.equals("�ְ�");
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
