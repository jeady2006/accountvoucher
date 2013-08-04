package jing.salary.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jing.model.wip.WIPDevice;
import jing.model.wip.WIPSetup;
import jing.salary.service.WIPGenerator;
import jing.util.excel.POIReader;
import jing.util.excel.POIWriter;
import jing.util.lang.FileUtils;
import jing.util.lang.StringUtils;
import jing.util.message.Message;
import jing.util.message.MessageProvider;

public class WIPGeneratorImpl implements WIPGenerator {
	private static final int WBS_NO_COLUMN_INDEX = 4;
	private String wip3910File;
	private String wip3940File;
	private POIReader reader;
	private POIWriter writer;
	private boolean done = false;
	private List<WIPDevice> wipDevices = Collections
			.synchronizedList(new ArrayList<WIPDevice>());
	private List<WIPDevice> cheapenPrepareList = new ArrayList<WIPDevice>();
	private List<WIPDevice> DPAmountList = new ArrayList<WIPDevice>();
	private List<WIPDevice> warehouseDateList = new ArrayList<WIPDevice>();
	private List<WIPDevice> DPMinusWIPList = new ArrayList<WIPDevice>();
	private List<WIPDevice> v9BillingMinusWIPList = new ArrayList<WIPDevice>();
	private List<WIPDevice> actWIPMinusPlaWIPList = new ArrayList<WIPDevice>();

	private List<WIPSetup> wipSetups = Collections
			.synchronizedList(new ArrayList<WIPSetup>());
	private List<WIPSetup> setupCheapenPrepareList = new ArrayList<WIPSetup>();
	private List<WIPSetup> notBillingAfterGovInpList = new ArrayList<WIPSetup>();
	private List<WIPSetup> wipBetween2and3andAbove = new ArrayList<WIPSetup>();
	private List<WIPSetup> wipBetween1and2 = new ArrayList<WIPSetup>();
	private List<WIPSetup> setupStartButNoDP = new ArrayList<WIPSetup>();
	private List<WIPSetup> setupUnStartButHasWIP_NoDP = new ArrayList<WIPSetup>();
	private List<WIPSetup> startAndWIPBeyondDP = new ArrayList<WIPSetup>();
	private List<WIPSetup> getDPButUnStartSetup = new ArrayList<WIPSetup>();
	private List<WIPSetup> v9Equal0 = new ArrayList<WIPSetup>();

	private String month;

	@Override
	public void loadDevice(final String wip3910File, final String wip3940File) {
		this.wip3910File = wip3910File;
		this.wip3940File = wip3940File;
		wipDevices.clear();
		cheapenPrepareList.clear();
		DPAmountList.clear();
		warehouseDateList.clear();
		DPMinusWIPList.clear();
		v9BillingMinusWIPList.clear();
		actWIPMinusPlaWIPList.clear();
		done = false;

		new Thread() {
			public void run() {
				// load 3910 file
				if (wip3910File != null && !wip3910File.equals("")) {
					try {
						loadWipDeviceFile(wip3910File);
					} catch (Exception e) {
						MessageProvider.getInstance().publicMessage(
								Message.ERROR, "��ȡ3910�ļ�����" + e.getMessage());
					}
				}
				// load 3940 file
				if (wip3940File != null && !wip3940File.equals("")) {
					try {
						loadWipDeviceFile(wip3940File);
					} catch (Exception e) {
						MessageProvider.getInstance().publicMessage(
								Message.ERROR, "��ȡ3940�ļ�����" + e.getMessage());
					}
				}
				done = true;
				MessageProvider.getInstance().publicMessage(Message.DEBUG,
						"WIP�ļ���ȡ���.");
			};
		}.start();
	}

	private void loadWipDeviceFile(String filePath) {
		reader = new POIReader(filePath);
		reader.loadSheet(0);

		// ����ǰ���հ���(3��)��������(1��)�������µ��У�1�У�
		reader.skipRow(5);
		String wbsNo = null;
		WIPDevice wip = null;
		while (reader.hasRow()) {
			reader.nextRow();

			// ������1��2��
			wbsNo = reader.getStringByIndex(WBS_NO_COLUMN_INDEX);
			if (wbsNo == null || wbsNo.equals("")) {
				break;
			}

			reader.skipCell(2);
			wip = new WIPDevice(wbsNo);
			wip.setCustomerNo(reader.getNextStringFormat());
			wip.setCustomerName(reader.getNextStringFormat());
			reader.skipCell(1); // ����
			wip.setContractNo(reader.getNextStringFormat());
			wip.setBusinessType(reader.getNextStringFormat());
			wip.setProductLine(reader.getNextStringFormat());
			wip.setPr(reader.getNextStringFormat());
			wip.setSaleDepartment(reader.getNextStringFormat());
			wip.setProjectDesc(reader.getNextStringFormat());
			wip.setDPMinusWIP(reader.getNextBigDecimalFormat());
			wip.setDPAmount(reader.getNextBigDecimalFormat());
			wip.setPlanTicket(reader.getNextBigDecimalFormat());
			wip.setWIPAmount(reader.getNextBigDecimalFormat());
			wip.setPlanTicketV9(reader.getNextBigDecimalFormat());
			wip.setPlanWIP(reader.getNextBigDecimalFormat());
			wip.setImportStuff(reader.getNextBigDecimalFormat());
			wip.setInternalStuff(reader.getNextBigDecimalFormat());
			wip.setOtherStuff(reader.getNextBigDecimalFormat());
			wip.setImportCustom(reader.getNextBigDecimalFormat());
			wip.setOceanShippingFee(reader.getNextBigDecimalFormat());
			wip.setSubPackageFee(reader.getNextBigDecimalFormat());
			wip.setRebuildSetupCost(reader.getNextBigDecimalFormat());
			wip.setContractCarryDate(reader.getNextStringFormat());
			wip.setOrderDate(reader.getNextStringFormat());
			wip.setWarehouseDate(reader.getNextStringFormat());
			wip.setLastBatchDate(reader.getNextStringFormat());
			wip.setEqual0Month(reader.getNextBigDecimalFormat());
			wip.setLessThan6Month(reader.getNextBigDecimalFormat());
			wip.setBetween6To12Month(reader.getNextBigDecimalFormat());
			wip.setBetween12To24Month(reader.getNextBigDecimalFormat());
			wip.setBetween24To36Month(reader.getNextBigDecimalFormat());
			wip.setGreaterThan36Month(reader.getNextBigDecimalFormat());
			wip.setCheapenPrepare(reader.getNextBigDecimalFormat());
			wip.setV9BillingMinusWIP(reader.getNextBigDecimalFormat());

			synchronized (wipDevices) {
				wipDevices.add(wip);
				wipDevices.notifyAll();
			}
		}

		reader.destroy();
	}

	@Override
	public void generateDevice(final String month) {
		this.month = month;
		new Thread() {
			public void run() {
				String fileName = "";
				try {
					fileName = generateDeviceWip(month);
				} catch (Exception e) {
					MessageProvider.getInstance().publicMessage(Message.ERROR,
							"����WIP�豸����" + e.getMessage());
					return;
				}
				MessageProvider.getInstance().publicMessage("����WIP�豸�ɹ���");
				FileUtils.popupFilePath("/" + POIWriter.DEFAULT_FOLDER
						+ fileName);
			};
		}.start();
	}

	private void generateDeviceTitle(boolean isGenerateAct) {

		writer.createRow();
		// ����
		writer.setNextStringData("�ͻ���");
		writer.setNextStringData("�ͻ�����");
		writer.setNextStringData("WBS���");
		writer.setNextStringData("��ͬ��");
		writer.setNextStringData("ҵ������");
		writer.setNextStringData("��Ʒ��");
		writer.setNextStringData("��������");
		writer.setNextStringData("���۰��´�");
		writer.setNextStringData("��Ŀ����");
		writer.setNextStringData("DP-WIP");
		writer.setNextStringData("DP(����)�ϼ�");
		writer.setNextStringData("�ƻ���ƱV0");
		writer.setNextStringData("WIP�ϼ�");
		writer.setNextStringData("�ƻ���ƱV9");
		writer.setNextStringData("�ƻ�WIP");
		writer.setNextStringData("���ڲ���");
		writer.setNextStringData("���ڲ���");
		writer.setNextStringData("��������");
		writer.setNextStringData("���ڹ�˰");
		writer.setNextStringData("���˷�");
		writer.setNextStringData("�ְ���");
		writer.setNextStringData("���찲װ�ɱ�");
		writer.setNextStringData("��ͬ�н�����");
		writer.setNextStringData("�´ﶩ������");
		writer.setNextStringData("����̨����");
		writer.setNextStringData("���һ������");
		writer.setNextStringData("0months");
		writer.setNextStringData("<6months");
		writer.setNextStringData("6 12months");
		writer.setNextStringData("12 24months");
		writer.setNextStringData("24 36months");
		writer.setNextStringData(">36months");
		writer.setNextStringData("����׼��");
		writer.setNextStringData("V9Billing-WIP");
		if (isGenerateAct) {
			writer.setNextStringData("act wip-pla wip");
		}
	}

	private void generateDeviceDetial(WIPDevice w, boolean isGenerateAct) {
		writer.createRow();
		writer.setNextStringData(w.getCustomerNo());
		writer.setNextStringData(w.getCustomerName());
		writer.setNextStringData(w.getWbsNo());
		writer.setNextStringData(w.getContractNo());
		writer.setNextStringData(w.getBusinessType());
		writer.setNextStringData(w.getProductLine());
		writer.setNextStringData(w.getPr());
		writer.setNextStringData(w.getSaleDepartment());
		writer.setNextStringData(w.getProjectDesc());
		writer.setNextNumbericData(w.getDPMinusWIP());
		writer.setNextNumbericData(w.getDPAmount());
		writer.setNextNumbericData(w.getPlanTicket());
		writer.setNextNumbericData(w.getWIPAmount());
		writer.setNextNumbericData(w.getPlanTicketV9());
		writer.setNextNumbericData(w.getPlanWIP());
		writer.setNextNumbericData(w.getImportStuff());
		writer.setNextNumbericData(w.getInternalStuff());
		writer.setNextNumbericData(w.getOtherStuff());
		writer.setNextNumbericData(w.getImportCustom());
		writer.setNextNumbericData(w.getOceanShippingFee());
		writer.setNextNumbericData(w.getSubPackageFee());
		writer.setNextNumbericData(w.getRebuildSetupCost());
		writer.setNextStringData(w.getContractCarryDate());
		writer.setNextStringData(w.getOrderDate());
		writer.setNextStringData(w.getWarehouseDate());
		writer.setNextStringData(w.getLastBatchDate());
		writer.setNextNumbericData(w.getEqual0Month());
		writer.setNextNumbericData(w.getLessThan6Month());
		writer.setNextNumbericData(w.getBetween6To12Month());
		writer.setNextNumbericData(w.getBetween12To24Month());
		writer.setNextNumbericData(w.getBetween24To36Month());
		writer.setNextNumbericData(w.getGreaterThan36Month());
		writer.setNextNumbericData(w.getCheapenPrepare());
		writer.setNextNumbericData(w.getV9BillingMinusWIP());
		if (isGenerateAct) {
			writer.setNextNumbericData(w.getActWIPMinusPlaWIP());
		}
	}

	private String generateDeviceWip(final String month) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");
		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();
		String f = "WIP�豸-" + fileName.substring(2, 4)
				+ StringUtils.padLeft(month, 2, "0");
		fileName += "/" + f + ".xls";
		try {
			writer = new POIWriter(fileName);
			writer.createNewSheet(f);

			this.generateDeviceTitle(true);
			boolean isDone = false;
			List<WIPDevice> l = new ArrayList<WIPDevice>();
			while (true) {
				if (done) {
					isDone = done;
				}
				l.clear();
				synchronized (wipDevices) {
					wipDevices.wait(100);
					while (wipDevices.size() > 0) {
						l.add(wipDevices.remove(0));
					}
				}
				for (WIPDevice w : l) {
					generateDeviceDetial(w, true);
					if (w.getCheapenPrepare().doubleValue() != 0) {
						cheapenPrepareList.add(w);
					}
					if (w.getDPAmount().doubleValue() == 0
							&& w.getWIPAmount().doubleValue() != 0) {
						DPAmountList.add(w);
					}
					if (w.getWarehouseDate() != null
							&& !w.getWarehouseDate().equals("")) {
						warehouseDateList.add(w);
					}
					if (w.getDPMinusWIP().doubleValue() < 0) {
						DPMinusWIPList.add(w);
					}
					if (w.getV9BillingMinusWIP().doubleValue() > 0) {
						v9BillingMinusWIPList.add(w);
					}
					if (w.getActWIPMinusPlaWIP().doubleValue() > 0) {
						actWIPMinusPlaWIPList.add(w);
					}
				}
				if (isDone)
					break;
			}

			generateCheapenPrepare();
			generateDPAmountSheet();
			generateWarehouseDateSheet();
			generateDPMinusWIPSheet();
			generateV9BillingMinusWIPSheet();
			generateActWIPMinusPlaWIPSheet();

			writer.flush();
			writer.destroy();
			MessageProvider.getInstance().publicMessage("����WIP��ɹ�!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return fileName;
	}

	private void generateCheapenPrepare() {
		writer.createNewSheet("��������WIP����׼��");
		generateDeviceTitle(false);
		if (cheapenPrepareList.size() > 0) {
			for (WIPDevice w : cheapenPrepareList) {
				generateDeviceDetial(w, false);
			}
		}

	}

	private void generateDPAmountSheet() {
		writer.createNewSheet("û�ж����ѷ���WIP");
		generateDeviceTitle(false);
		if (DPAmountList.size() > 0) {
			for (WIPDevice w : DPAmountList) {
				generateDeviceDetial(w, false);
			}
		}

	}

	private void generateWarehouseDateSheet() {
		writer.createNewSheet("�ѳ�̨����δ����");
		generateDeviceTitle(false);
		if (warehouseDateList.size() > 0) {
			for (WIPDevice w : warehouseDateList) {
				generateDeviceDetial(w, false);
			}
		}
	}

	private void generateDPMinusWIPSheet() {
		writer.createNewSheet("ʵ�ն���<WIP");
		generateDeviceTitle(false);
		if (DPMinusWIPList.size() > 0) {
			for (WIPDevice w : DPMinusWIPList) {
				generateDeviceDetial(w, false);
			}
		}
	}

	private void generateV9BillingMinusWIPSheet() {
		writer.createNewSheet("ʵ��WIP>�ƻ�(V9)Billing");
		generateDeviceTitle(false);
		if (v9BillingMinusWIPList.size() > 0) {
			for (WIPDevice w : v9BillingMinusWIPList) {
				generateDeviceDetial(w, false);
			}
		}
	}

	private void generateActWIPMinusPlaWIPSheet() {
		writer.createNewSheet("ʵ��WIP>�ƻ�WIP(V9)");
		generateDeviceTitle(true);
		if (actWIPMinusPlaWIPList.size() > 0) {
			for (WIPDevice w : actWIPMinusPlaWIPList) {
				generateDeviceDetial(w, true);
			}
		}
	}

	@Override
	public void generateSetup(final String month) {
		this.month = month;
		new Thread() {
			public void run() {
				String fileName = "";
				try {
					fileName = generateSetupWip(month);
				} catch (Exception e) {
					MessageProvider.getInstance().publicMessage(Message.ERROR,
							"����WIP��װ�ļ�����" + e.getMessage());
					return;
				}
				MessageProvider.getInstance().publicMessage("����WIP��װ�ɹ���");
				FileUtils.popupFilePath("/" + POIWriter.DEFAULT_FOLDER
						+ fileName);
			}
		}.start();

	}

	private String generateSetupWip(String month) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd_hh-mm-ss");
		String fileName = dateFormat.format(new Date());
		File file = new File(POIWriter.DEFAULT_FOLDER + fileName);
		file.mkdirs();
		String f = "WIP��װ-" + fileName.substring(2, 4)
				+ StringUtils.padLeft(month, 2, "0");
		fileName += "/" + f + ".xls";
		try {
			writer = new POIWriter(fileName);
			writer.createNewSheet(f);

			this.generateSetupTitle();
			boolean isDone = false;
			List<WIPSetup> l = new ArrayList<WIPSetup>();
			while (true) {
				if (done) {
					isDone = done;
				}
				l.clear();
				synchronized (wipSetups) {
					wipSetups.wait(100);
					while (wipSetups.size() > 0) {
						l.add(wipSetups.remove(0));
					}
				}
				for (WIPSetup w : l) {
					generateSetupDetial(w);
					if (w.getCheapenPrepare().doubleValue() != 0) {
						setupCheapenPrepareList.add(w);
					}
					if (w.getGovAcceptDate() != null
							&& !w.getGovAcceptDate().equals("")) {
						notBillingAfterGovInpList.add(w);
					}
					if (w.getGreaterThan36Month().doubleValue() != 0
							|| w.getBetween24To36Month().doubleValue() != 0) {
						wipBetween2and3andAbove.add(w);
					}
					if (w.getBetween12To24Month().doubleValue() != 0) {
						wipBetween1and2.add(w);
					}
					if (w.getComponentSetupStartDate() != null
							&& !w.getComponentSetupStartDate().equals("")
							&& w.getDPAmount().doubleValue() == 0) {
						setupStartButNoDP.add(w);
					}
					if ((w.getComponentSetupStartDate() == null || w
							.getComponentSetupStartDate().equals(""))
							&& w.getWIPAmount().doubleValue() > 0
							&& w.getDPAmount().doubleValue() == 0) {
						setupUnStartButHasWIP_NoDP.add(w);
					}
					if (w.getComponentSetupStartDate() != null
							&& !w.getComponentSetupStartDate().equals("")
							&& w.getDPMinusWIP().doubleValue() < 0) {
						startAndWIPBeyondDP.add(w);
					}
					if ((w.getComponentSetupStartDate() == null || w
							.getComponentSetupStartDate().equals(""))
							&& w.getDPAmount().doubleValue() > 0) {
						getDPButUnStartSetup.add(w);
					}
					if (w.getPlanTicketV9().doubleValue() == 0) {
						v9Equal0.add(w);
					}
				}
				if (isDone)
					break;
			}

			generateSetupCheapenPrepare();
			generateNotBillingAfterGovInp();
			generateWipBetween2and3andAbove();
			generateWipBetween1and2();
			generateSetupStartButNoDP();
			generateSetupUnStartButHasWIP_NoDP();
			generateStartAndWIPBeyondDP();
			generateGetDPButUnStartSetup();
			generateV9Equal0();

			writer.flush();
			writer.destroy();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return fileName;
	};

	private void generateV9Equal0() {
		writer.createNewSheet("V9=0");
		generateSetupTitle();
		if (v9Equal0.size() > 0) {
			for (WIPSetup w : v9Equal0) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateGetDPButUnStartSetup() {
		writer.createNewSheet("���ն�����δ����(δά����������)");
		generateSetupTitle();
		if (getDPButUnStartSetup.size() > 0) {
			for (WIPSetup w : getDPButUnStartSetup) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateStartAndWIPBeyondDP() {
		writer.createNewSheet("�ѿ���WIP������");
		generateSetupTitle();
		if (startAndWIPBeyondDP.size() > 0) {
			for (WIPSetup w : startAndWIPBeyondDP) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateSetupUnStartButHasWIP_NoDP() {
		writer.createNewSheet("δ�����ѷ���WIP(NO DP)");
		generateSetupTitle();
		if (setupUnStartButHasWIP_NoDP.size() > 0) {
			for (WIPSetup w : setupUnStartButHasWIP_NoDP) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateSetupStartButNoDP() {
		writer.createNewSheet("��װ������û�ж���");
		generateSetupTitle();
		if (setupStartButNoDP.size() > 0) {
			for (WIPSetup w : setupStartButNoDP) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateWipBetween1and2() {
		writer.createNewSheet("WIP(1-2��)");
		generateSetupTitle();
		if (wipBetween1and2.size() > 0) {
			for (WIPSetup w : wipBetween1and2) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateWipBetween2and3andAbove() {
		writer.createNewSheet("WIP(2-3�꼰3������)");
		generateSetupTitle();
		if (wipBetween2and3andAbove.size() > 0) {
			for (WIPSetup w : wipBetween2and3andAbove) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateNotBillingAfterGovInp() {
		writer.createNewSheet("Not billing after Gov Inp.");
		generateSetupTitle();
		if (notBillingAfterGovInpList.size() > 0) {
			for (WIPSetup w : notBillingAfterGovInpList) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateSetupCheapenPrepare() {
		writer.createNewSheet("��������WIP����׼��");
		generateSetupTitle();
		if (setupCheapenPrepareList.size() > 0) {
			for (WIPSetup w : setupCheapenPrepareList) {
				generateSetupDetial(w);
			}
		}
	}

	private void generateSetupDetial(WIPSetup w) {
		writer.createRow();
		writer.setNextStringData(w.getCustomerNo());
		writer.setNextStringData(w.getCustomerName());
		writer.setNextStringData(w.getWbsNo());
		writer.setNextStringData(w.getContractNo());
		writer.setNextStringData(w.getBusinessType());
		writer.setNextStringData(w.getProductLine());
		writer.setNextStringData(w.getProjectDesc());
		writer.setNextStringData(w.getPr());
		writer.setNextStringData(w.getSaleDepartment());
		writer.setNextNumbericData(w.getDPMinusWIP());
		writer.setNextNumbericData(w.getDPAmount());
		writer.setNextNumbericData(w.getPlanTicket());
		writer.setNextNumbericData(w.getPlanTicketV9());
		writer.setNextNumbericData(w.getPlanWIP());
		writer.setNextNumbericData(w.getWIPAmount());
		writer.setNextNumbericData(w.getComponentSetupSalary());
		writer.setNextNumbericData(w.getComponentSetupTripFee());
		writer.setNextNumbericData(w.getProjectManageSalaryCost());
		writer.setNextNumbericData(w.getProjectManageTripCost());
		writer.setNextNumbericData(w.getDebugSalaryCost());
		writer.setNextNumbericData(w.getDebugTripCost());
		writer.setNextNumbericData(w.getCheckSalaryCost());
		writer.setNextNumbericData(w.getCheckTripCost());
		writer.setNextNumbericData(w.getCheckFee());
		writer.setNextNumbericData(w.getSplitFee());
		writer.setNextNumbericData(w.getUnsureCost());
		writer.setNextNumbericData(w.getInnerSettleCost());
		writer.setNextNumbericData(w.getOtherCost());
		writer.setNextStringData(w.getSetupPreCollectDate());
		writer.setNextStringData(w.getComponentSetupStartDate());
		writer.setNextStringData(w.getInnerCheckEndDate());
		writer.setNextStringData(w.getDeviceTicketDate());
		writer.setNextStringData(w.getGovAcceptDate());
		writer.setNextNumbericData(w.getEqual0Month());
		writer.setNextNumbericData(w.getLessThan6Month());
		writer.setNextNumbericData(w.getBetween6To12Month());
		writer.setNextNumbericData(w.getBetween12To24Month());
		writer.setNextNumbericData(w.getBetween24To36Month());
		writer.setNextNumbericData(w.getGreaterThan36Month());
		writer.setNextNumbericData(w.getCheapenPrepare());
		writer.setNextNumbericData(w.getV9BillingMinusWIP());
	}

	private void generateSetupTitle() {
		writer.createRow();
		// ����
		writer.setNextStringData("�ͻ���");
		writer.setNextStringData("�ͻ�����");
		writer.setNextStringData("WBS���");
		writer.setNextStringData("��ͬ��");
		writer.setNextStringData("ҵ������");
		writer.setNextStringData("��Ʒ��");
		writer.setNextStringData("��Ŀ����");
		writer.setNextStringData("��������");
		writer.setNextStringData("���۰��´�");
		writer.setNextStringData("DP-WIP");
		writer.setNextStringData("DP(����)�ϼ�");
		writer.setNextStringData("�ƻ���ƱV0");
		writer.setNextStringData("�ƻ���ƱV9");
		writer.setNextStringData("�ƻ�WIP");
		writer.setNextStringData("WIP�ϼ�");
		writer.setNextStringData("������װ����");
		writer.setNextStringData("������װ���÷�");
		writer.setNextStringData("��Ŀ�����ʳɱ�");
		writer.setNextStringData("��Ŀ������óɱ�");
		writer.setNextStringData("���Թ��ʳɱ�");
		writer.setNextStringData("���Բ��óɱ�");
		writer.setNextStringData("���鹤�ʳɱ�");
		writer.setNextStringData("������óɱ�");
		writer.setNextStringData("�����");
		writer.setNextStringData("�ְ���");
		writer.setNextStringData("�Ᵽ�ɱ�");
		writer.setNextStringData("�ڲ�����ɱ�");
		writer.setNextStringData("�����ɱ�");
		writer.setNextStringData("��װԤ�ն�������");
		writer.setNextStringData("������װ��ʼ����");
		writer.setNextStringData("�ڼ��������");
		writer.setNextStringData("�豸��Ʊ����");
		writer.setNextStringData("������������");
		writer.setNextStringData("0months");
		writer.setNextStringData("<6months");
		writer.setNextStringData("6 12months");
		writer.setNextStringData("12 24months");
		writer.setNextStringData("24 36months");
		writer.setNextStringData(">36months");
		writer.setNextStringData("����׼��");
		writer.setNextStringData("V9Billing-WIP");
	}

	@Override
	public void loadSetup(final String wip3910File, final String wip3940File) {
		this.wip3910File = wip3910File;
		this.wip3940File = wip3940File;
		wipSetups.clear();
		setupCheapenPrepareList.clear();
		notBillingAfterGovInpList.clear();
		wipBetween2and3andAbove.clear();
		wipBetween1and2.clear();
		setupStartButNoDP.clear();
		setupUnStartButHasWIP_NoDP.clear();
		startAndWIPBeyondDP.clear();
		done = false;

		new Thread() {
			public void run() {
				// load 3910 file
				if (wip3910File != null && !wip3910File.equals("")) {
					try {
						loadWipSetupFile(wip3910File);
					} catch (Exception e) {
						MessageProvider.getInstance().publicMessage(
								Message.ERROR, "��ȡ3910�ļ�����" + e.getMessage());
					}
				}
				// load 3940 file
				if (wip3940File != null && !wip3940File.equals("")) {
					try {
						loadWipSetupFile(wip3940File);
					} catch (Exception e) {
						MessageProvider.getInstance().publicMessage(
								Message.ERROR, "��ȡ3940�ļ�����" + e.getMessage());
					}
				}
				done = true;
				System.out.println("Done!");
			}
		}.start();

	}

	private void loadWipSetupFile(final String wipFile) {
		reader = new POIReader(wipFile);
		reader.loadSheet(0);

		// ����ǰ���հ���(3��)��������(1��)�������µ��У�1�У�
		reader.skipRow(5);
		String wbsNo = null;
		WIPSetup wip = null;
		while (reader.hasRow()) {
			reader.nextRow();

			// ������1��2��
			wbsNo = reader.getStringByIndex(WBS_NO_COLUMN_INDEX);
			if (wbsNo == null || wbsNo.equals("")) {
				break;
			}

			reader.skipCell(2);
			wip = new WIPSetup(wbsNo);
			wip.setCustomerNo(reader.getNextStringFormat());
			wip.setCustomerName(reader.getNextStringFormat());
			reader.skipCell(1); // ����
			wip.setContractNo(reader.getNextStringFormat());
			wip.setBusinessType(reader.getNextStringFormat());
			wip.setProductLine(reader.getNextStringFormat());
			wip.setProjectDesc(reader.getNextStringFormat());
			wip.setPr(reader.getNextStringFormat());
			wip.setSaleDepartment(reader.getNextStringFormat());
			wip.setDPMinusWIP(reader.getNextBigDecimalFormat());
			wip.setDPAmount(reader.getNextBigDecimalFormat());
			wip.setPlanTicket(reader.getNextBigDecimalFormat());
			wip.setPlanTicketV9(reader.getNextBigDecimalFormat());
			wip.setPlanWIP(reader.getNextBigDecimalFormat());
			wip.setWIPAmount(reader.getNextBigDecimalFormat());
			wip.setComponentSetupSalary(reader.getNextBigDecimalFormat());
			wip.setComponentSetupTripFee(reader.getNextBigDecimalFormat());
			wip.setProjectManageSalaryCost(reader.getNextBigDecimalFormat());
			wip.setProjectManageTripCost(reader.getNextBigDecimalFormat());
			wip.setDebugSalaryCost(reader.getNextBigDecimalFormat());
			wip.setDebugTripCost(reader.getNextBigDecimalFormat());
			wip.setCheckSalaryCost(reader.getNextBigDecimalFormat());
			wip.setCheckTripCost(reader.getNextBigDecimalFormat());
			wip.setCheckFee(reader.getNextBigDecimalFormat());
			wip.setSplitFee(reader.getNextBigDecimalFormat());
			wip.setInnerSettleCost(reader.getNextBigDecimalFormat());
			wip.setUnsureCost(reader.getNextBigDecimalFormat());
			wip.setOtherCost(reader.getNextBigDecimalFormat());
			wip.setSetupPreCollectDate(reader.getNextStringFormat());
			wip.setComponentSetupStartDate(reader.getNextStringFormat());
			wip.setInnerCheckEndDate(reader.getNextStringFormat());
			wip.setDeviceTicketDate(reader.getNextStringFormat());
			wip.setGovAcceptDate(reader.getNextStringFormat());
			wip.setEqual0Month(reader.getNextBigDecimalFormat());
			wip.setLessThan6Month(reader.getNextBigDecimalFormat());
			wip.setBetween6To12Month(reader.getNextBigDecimalFormat());
			wip.setBetween12To24Month(reader.getNextBigDecimalFormat());
			wip.setBetween24To36Month(reader.getNextBigDecimalFormat());
			wip.setGreaterThan36Month(reader.getNextBigDecimalFormat());
			wip.setCheapenPrepare(reader.getNextBigDecimalFormat());
			wip.setV9BillingMinusWIP(reader.getNextBigDecimalFormat());

			synchronized (wipSetups) {
				wipSetups.add(wip);
				wipSetups.notifyAll();
			}
		}

		reader.destroy();

	}

	public String getWip3910File() {
		return wip3910File;
	}

	public void setWip3910File(String wip3910File) {
		this.wip3910File = wip3910File;
	}

	public String getWip3940File() {
		return wip3940File;
	}

	public void setWip3940File(String wip3940File) {
		this.wip3940File = wip3940File;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	};

}
