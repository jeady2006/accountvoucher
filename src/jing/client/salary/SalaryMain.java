package jing.client.salary;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import jing.client.ClientMessage;
import jing.model.data.RuntimeData;
import jing.salary.service.AccrualGenerator;
import jing.salary.service.BackBillingGenerator;
import jing.salary.service.SalaryGenerator;
import jing.salary.service.WIPGenerator;
import jing.salary.service.impl.AccrualGeneratorImpl;
import jing.salary.service.impl.BackBillingGeneratorImpl;
import jing.salary.service.impl.SalaryGeneratorImpl;
import jing.salary.service.impl.WIPGeneratorImpl;
import jing.salary.service.listener.BackBillingReplyListener;
import jing.salary.service.listener.CallListener;
import jing.util.excel.POIWriter;
import jing.util.fields.ExcelFields;
import jing.util.lang.FileUtils;
import jing.util.lang.StringUtils;
import jing.util.message.Message;
import jing.util.message.MessageProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class SalaryMain implements BackBillingReplyListener {
	private Composite parent;
	private String detailFileName;
	private Button backBillingReplyButton;

	public SalaryMain(Composite parent) {
		this.parent = parent;
		GridLayout grid = new GridLayout(1, false);
		grid.marginHeight = 2;
		grid.marginWidth = 2;
		this.parent.setLayout(grid);
		init();
	}

	private void init() {
		final Composite salaryMenu = new Composite(this.parent, SWT.NONE);
		GridData salaryMenuData = new GridData(GridData.FILL_HORIZONTAL);
		salaryMenu.setLayoutData(salaryMenuData);
		// salaryMenuData.heightHint = 50;
		// salaryMenu.setBackground(new Color(this.parent.getDisplay(), 191,
		// 219,
		// 255));

		GridLayout salaryMenuLayout = new GridLayout();
		salaryMenuLayout.numColumns = 6;
		salaryMenuLayout.marginHeight = 2;
		salaryMenuLayout.marginWidth = 2;
		salaryMenu.setLayout(salaryMenuLayout);
		ToolBar toolBar = new ToolBar(salaryMenu, SWT.NONE);
		ToolItem salaryToolItem = new ToolItem(toolBar, SWT.PUSH);
		salaryToolItem.setText("工资表");
		salaryToolItem.setImage(new Image(this.parent.getDisplay(),
				"icon/salary.png"));
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem accrualToolItem = new ToolItem(toolBar, SWT.PUSH);
		accrualToolItem.setText("Arrcual表");
		accrualToolItem.setImage(new Image(this.parent.getDisplay(),
				"icon/accrual.png"));
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem wipToolItem = new ToolItem(toolBar, SWT.PUSH);
		wipToolItem.setText("WIP表");
		wipToolItem.setImage(new Image(this.parent.getDisplay(),
				"icon/backbill.png"));
		accrualToolItem.setImage(new Image(this.parent.getDisplay(),
				"icon/accrual.png"));
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem backBillingItem = new ToolItem(toolBar, SWT.PUSH);
		backBillingItem.setText("追补表");
		backBillingItem.setImage(new Image(this.parent.getDisplay(),
				"icon/wip.png"));
		new ToolItem(toolBar, SWT.SEPARATOR);
		ToolItem releaseBillItem = new ToolItem(toolBar, SWT.PUSH);
		releaseBillItem.setText("释放表");
		releaseBillItem.setImage(new Image(this.parent.getDisplay(),
				"icon/release.png"));

		// final Composite subSalaryComp = new Composite(salaryMenu, SWT.NONE);
		// subSalaryComp.setBackground(new Color(this.parent.getDisplay(), 229,
		// 239, 252));
		// subSalaryComp.setLayout(new FillLayout(SWT.NONE));
		// Image saralyIcon = new Image(this.parent.getDisplay(),
		// "icon/salary.png");
		// subSalaryComp.setBackgroundImage(saralyIcon);

		final Composite formComposite = new Composite(this.parent, SWT.NONE);
		final StackLayout formLayout = new StackLayout();
		formComposite.setLayout(formLayout);
		GridData formCompositeData = new GridData(GridData.FILL_HORIZONTAL);
		formComposite.setLayoutData(formCompositeData);

		final Group salaryGroup = new Group(formComposite, SWT.NONE);
		salaryGroup.setText("工资表");

		GridLayout wrapperLayout = new GridLayout();
		wrapperLayout.numColumns = 6;
		wrapperLayout.verticalSpacing = 10;
		salaryGroup.setLayout(wrapperLayout);
		Color whiteColor = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		salaryGroup.setBackground(whiteColor);
		// this.wrapper.setBounds(10, 10, 1000, 1000);

		Label monthLabel = new Label(salaryGroup, SWT.NONE);
		monthLabel.setText("请选择月份：");
		monthLabel.setBackground(whiteColor);

		final Combo monthCombo = new Combo(salaryGroup, SWT.NONE| SWT.READ_ONLY);
		for (int i = 1; i <= 12; i++) {
			monthCombo.add(i + "月");
		}
		GridData monthComboData = new GridData();
		monthComboData.widthHint = 100;
		monthCombo.setLayoutData(monthComboData);
		monthCombo.select(Calendar.getInstance().get(Calendar.MONTH));

		Label prLabel = new Label(salaryGroup, SWT.NONE);
		prLabel.setText("请选择生成哪个利润中心:");
		prLabel.setBackground(whiteColor);

		final Combo prCombo = new Combo(salaryGroup, SWT.NONE| SWT.READ_ONLY);
		Set<String> prSet = RuntimeData.getInstance().getPrSet();
		Iterator<String> it = prSet.iterator();
		while (it.hasNext()) {
			prCombo.add(it.next());
		}
		prCombo.select(0);

		final Label postingLabel = new Label(salaryGroup, SWT.NONE);
		postingLabel.setText("请选择posting日期：");
		postingLabel.setBackground(whiteColor);
		// final Button openCalendar = new Button(salaryGroup, SWT.PUSH);
		// openCalendar.setText("点击选择Posting日期");
		// openCalendar.addSelectionListener(new SelectionAdapter() {
		// public void widgetSelected(SelectionEvent e) {
		// final Shell dialog = new Shell(openCalendar.getShell(),
		// SWT.DIALOG_TRIM);
		// dialog.setLayout(new FillLayout());
		//
		// final DateTime calendar = new DateTime(dialog, SWT.CALENDAR
		// | SWT.BORDER);
		// calendar.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// int m = calendar.getMonth() + 1;
		// String month = m > 10 ? m + "" : "0" + m;
		// String day = calendar.getDay() > 10 ? calendar.getDay()
		// + "" : "0" + calendar.getDay();
		// postingLabel.setData("Date", calendar.getYear() + month
		// + day);
		// postingLabel.setText("当前Posting日期："
		// + postingLabel.getData("Date"));
		// dialog.close();
		// }
		// });
		// dialog.pack();
		// dialog.open();
		// }
		// });
		final DateTime salaryPostDate = new DateTime(salaryGroup, SWT.DATE
				| SWT.BORDER);
		// GridData replyPostDateData = new GridData();
		// replyPostDateData.horizontalSpan = 5;
		// replyPostDate.setLayoutData(replyPostDateData);

		Label fileLabel = new Label(salaryGroup, SWT.NONE);
		fileLabel.setText("请选择明细文件：");
		fileLabel.setBackground(whiteColor);

		final Text detailFile = new Text(salaryGroup, SWT.BORDER);
		detailFile.setEditable(false);
		GridData detailFileData = new GridData();
		detailFileData.widthHint = 450;
		detailFileData.horizontalSpan = 4;
		detailFile.setLayoutData(detailFileData);
		detailFile.setBackground(whiteColor);

		Button chooseFile = new Button(salaryGroup, SWT.NONE);
		chooseFile.setText("选择文件");
		chooseFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				FileDialog fileDialog = new FileDialog(salaryGroup.getShell());
				fileDialog.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				fileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = fileDialog.open();
				if (detailFileName != null) {
					detailFile.setText(detailFileName);
				}
			}
		});

		Label salaryWbsLabel = new Label(salaryGroup, SWT.NONE);
		salaryWbsLabel.setBackground(whiteColor);
		salaryWbsLabel.setText("请选择WBS资源文件：");
		final Text salaryWbsFile = new Text(salaryGroup, SWT.BORDER);
		salaryWbsFile.setEditable(false);
		GridData salaryWbsFileData = new GridData();
		salaryWbsFileData.widthHint = 550;
		salaryWbsFileData.horizontalSpan = 4;
		salaryWbsFile.setLayoutData(salaryWbsFileData);
		salaryWbsFile.setBackground(whiteColor);

		Button salaryWbsFileChoose = new Button(salaryGroup, SWT.NONE);
		salaryWbsFileChoose.setText("选择文件");
		salaryWbsFileChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog wbsfileDialog = new FileDialog(salaryGroup
						.getShell());
				wbsfileDialog.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				wbsfileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = wbsfileDialog.open();
				if (detailFileName != null) {
					salaryWbsFile.setText(detailFileName);
				}
			}
		});

		Button generateButton = new Button(salaryGroup, SWT.NONE);
		generateButton.setText("生成");
		formLayout.topControl = salaryGroup;

		final Group accrualGroup = new Group(formComposite, SWT.NONE);
		accrualGroup.setText("Accrual");
		GridData accrualGroupData = new GridData(GridData.FILL_HORIZONTAL);
		accrualGroup.setLayoutData(accrualGroupData);
		GridLayout accrualGroupLayout = new GridLayout();
		accrualGroupLayout.numColumns = 6;
		accrualGroupLayout.verticalSpacing = 10;
		accrualGroup.setLayout(accrualGroupLayout);
		accrualGroup.setBackground(whiteColor);

		Label wbsLabel = new Label(accrualGroup, SWT.NONE);
		wbsLabel.setBackground(whiteColor);
		wbsLabel.setText("请选择WBS资源文件：");
		final Text wbsFile = new Text(accrualGroup, SWT.BORDER);
		wbsFile.setEditable(false);
		GridData wbsFileData = new GridData();
		wbsFileData.widthHint = 550;
		wbsFileData.horizontalSpan = 4;
		wbsFile.setLayoutData(wbsFileData);
		wbsFile.setBackground(whiteColor);

		Button wbsFileChoose = new Button(accrualGroup, SWT.NONE);
		wbsFileChoose.setText("选择文件");
		wbsFileChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog wbsfileDialog = new FileDialog(accrualGroup
						.getShell());
				wbsfileDialog.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				wbsfileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = wbsfileDialog.open();
				if (detailFileName != null) {
					wbsFile.setText(detailFileName);
				}
			}
		});

		Label listingLabel = new Label(accrualGroup, SWT.NONE);
		listingLabel.setBackground(whiteColor);
		listingLabel.setText("请选择清单文件：");
		final Text listingFile = new Text(accrualGroup, SWT.BORDER);
		listingFile.setEditable(false);
		GridData listingFileData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		listingFileData.widthHint = 550;
		listingFile.setLayoutData(listingFileData);
		listingFile.setBackground(whiteColor);

		Button listingFileChoose = new Button(accrualGroup, SWT.NONE);
		listingFileChoose.setText("选择文件");
		listingFileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog listfileDialog = new FileDialog(accrualGroup
						.getShell());
				listfileDialog.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				listfileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = listfileDialog.open();
				if (detailFileName != null) {
					listingFile.setText(detailFileName);
				}
			};
		});
		Label listingWarning = new Label(accrualGroup, SWT.NONE);
		listingWarning.setText("清单文件名的格式如：2193000清单.xls");
		listingWarning.setBackground(whiteColor);
		listingWarning.setForeground(parent.getDisplay().getSystemColor(
				SWT.COLOR_BLUE));
		GridData listingWarningData = new GridData();
		listingWarningData.horizontalSpan = 3;
		listingWarning.setLayoutData(listingWarningData);

		Label accrualMonthLabel = new Label(accrualGroup, SWT.NONE);
		accrualMonthLabel.setText("请选择月份：");
		accrualMonthLabel.setBackground(whiteColor);

		final Combo accrualMonthCombo = new Combo(accrualGroup, SWT.NONE| SWT.READ_ONLY);
		for (int i = 1; i <= 12; i++) {
			accrualMonthCombo.add(i + "月");
		}
		GridData accrualMonthComboData = new GridData();
		accrualMonthComboData.widthHint = 100;
		accrualMonthComboData.horizontalSpan = 5;
		accrualMonthCombo.setLayoutData(accrualMonthComboData);
		accrualMonthCombo.select(Calendar.getInstance().get(Calendar.MONTH));

		Button accrualButton = new Button(accrualGroup, SWT.NONE);
		accrualButton.setText("生成");

		// WIP=====================================
		final Group wipGroup = new Group(formComposite, SWT.NONE);
		wipGroup.setText("WIP");
		GridData wipGroupData = new GridData(GridData.FILL_HORIZONTAL);
		wipGroup.setLayoutData(wipGroupData);
		GridLayout wipGroupLayout = new GridLayout();
		wipGroupLayout.numColumns = 6;
		wipGroupLayout.verticalSpacing = 10;
		wipGroup.setLayout(wipGroupLayout);
		wipGroup.setBackground(whiteColor);

		final Label wipTypeMonth = new Label(wipGroup, SWT.NONE);
		wipTypeMonth.setText("请选择WIP类型：");
		wipTypeMonth.setBackground(whiteColor);

		final Combo wipType = new Combo(wipGroup, SWT.NONE | SWT.READ_ONLY);
		wipType.add("设备", 0);
		wipType.add("安装", 1);
		GridData wipTypeData = new GridData();
		wipTypeData.widthHint = 100;
		wipTypeData.horizontalSpan = 5;
		wipType.setLayoutData(wipTypeData);
		wipType.select(0);

		Label wipMonthLabel = new Label(wipGroup, SWT.NONE);
		wipMonthLabel.setText("请选择月份：");
		wipMonthLabel.setBackground(whiteColor);

		final Combo wipMonthCombo = new Combo(wipGroup, SWT.NONE
				| SWT.READ_ONLY);
		for (int i = 1; i <= 12; i++) {
			wipMonthCombo.add(i + "月");
		}
		GridData wipMonthComboData = new GridData();
		wipMonthComboData.widthHint = 100;
		wipMonthComboData.horizontalSpan = 5;
		wipMonthCombo.setLayoutData(wipMonthComboData);
		wipMonthCombo.select(Calendar.getInstance().get(Calendar.MONTH));

		Label wip3910Label = new Label(wipGroup, SWT.NONE);
		wip3910Label.setBackground(whiteColor);
		wip3910Label.setText("请选择WIP3910文件：");
		final Text wip3910File = new Text(wipGroup, SWT.BORDER);
		wip3910File.setEditable(false);
		GridData wip3910FileData = new GridData();
		wip3910FileData.widthHint = 550;
		wip3910FileData.horizontalSpan = 4;
		wip3910File.setLayoutData(wip3910FileData);
		wip3910File.setBackground(whiteColor);

		Button wip3910FileChoose = new Button(wipGroup, SWT.NONE);
		wip3910FileChoose.setText("选择文件");
		wip3910FileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog wip3910fileDialog = new FileDialog(accrualGroup
						.getShell());
				wip3910fileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				wip3910fileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = wip3910fileDialog.open();
				if (detailFileName != null) {
					wip3910File.setText(detailFileName);
				}
			};
		});

		Label wip3940Label = new Label(wipGroup, SWT.NONE);
		wip3940Label.setBackground(whiteColor);
		wip3940Label.setText("请选择WIP3940文件：");
		final Text wip3940File = new Text(wipGroup, SWT.BORDER);
		wip3940File.setEditable(false);
		GridData wip3940FileData = new GridData();
		wip3940FileData.widthHint = 550;
		wip3940FileData.horizontalSpan = 4;
		wip3940File.setLayoutData(wip3940FileData);
		wip3940File.setBackground(whiteColor);

		Button wip3940FileChoose = new Button(wipGroup, SWT.NONE);
		wip3940FileChoose.setText("选择文件");
		wip3940FileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog wip3940fileDialog = new FileDialog(accrualGroup
						.getShell());
				wip3940fileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				wip3940fileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = wip3940fileDialog.open();
				if (detailFileName != null) {
					wip3940File.setText(detailFileName);
				}
			};
		});

		Button wipButton = new Button(wipGroup, SWT.NONE);
		wipButton.setText("生成");

		final Group backBillingGroup = new Group(formComposite, SWT.NONE);
		backBillingGroup.setText("追补释放表");
		GridData backBillingData = new GridData(GridData.FILL_HORIZONTAL);
		backBillingData.horizontalSpan = 6;
		backBillingGroup.setLayoutData(backBillingData);
		GridLayout backBillingLayout = new GridLayout();
		backBillingLayout.numColumns = 6;
		backBillingLayout.verticalSpacing = 10;
		backBillingGroup.setLayout(backBillingLayout);
		backBillingGroup.setBackground(whiteColor);

		final Label backBillingWbsLabel = new Label(backBillingGroup, SWT.NONE);
		backBillingWbsLabel.setText("请选择WBS表：");
		backBillingWbsLabel.setBackground(whiteColor);
		final Text backBillingWbsFile = new Text(backBillingGroup, SWT.BORDER);
		backBillingWbsFile.setEditable(false);
		GridData backBillingWbsFileData = new GridData();
		backBillingWbsFileData.widthHint = 550;
		backBillingWbsFileData.horizontalSpan = 4;
		backBillingWbsFile.setLayoutData(backBillingWbsFileData);
		backBillingWbsFile.setBackground(whiteColor);

		Button backBillingWbsFileChoose = new Button(backBillingGroup, SWT.NONE);
		backBillingWbsFileChoose.setText("选择文件");
		backBillingWbsFileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog backBillingWbsFileDialog = new FileDialog(
						accrualGroup.getShell());
				backBillingWbsFileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				backBillingWbsFileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = backBillingWbsFileDialog.open();
				if (detailFileName != null) {
					backBillingWbsFile.setText(detailFileName);
				}
			};
		});

		final Label list2192041Label = new Label(backBillingGroup, SWT.NONE);
		list2192041Label.setText("请选择2192041清单表：");
		list2192041Label.setBackground(whiteColor);
		final Text list2192041File = new Text(backBillingGroup, SWT.BORDER);
		list2192041File.setEditable(false);
		GridData list2192041FileData = new GridData();
		list2192041FileData.widthHint = 550;
		list2192041FileData.horizontalSpan = 4;
		list2192041File.setLayoutData(list2192041FileData);
		list2192041File.setBackground(whiteColor);

		Button list2192041FileChoose = new Button(backBillingGroup, SWT.NONE);
		list2192041FileChoose.setText("选择文件");
		list2192041FileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog list2192041FileDialog = new FileDialog(accrualGroup
						.getShell());
				list2192041FileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				list2192041FileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = list2192041FileDialog.open();
				if (detailFileName != null) {
					list2192041File.setText(detailFileName);
				}
			};
		});

		final Label list2193000Label = new Label(backBillingGroup, SWT.NONE);
		list2193000Label.setText("请选择2193000清单表：");
		list2193000Label.setBackground(whiteColor);
		final Text list2193000File = new Text(backBillingGroup, SWT.BORDER);
		list2193000File.setEditable(false);
		GridData list2193000FileData = new GridData();
		list2193000FileData.widthHint = 550;
		list2193000FileData.horizontalSpan = 4;
		list2193000File.setLayoutData(list2193000FileData);
		list2193000File.setBackground(whiteColor);

		Button list2193000FileChoose = new Button(backBillingGroup, SWT.NONE);
		list2193000FileChoose.setText("选择文件");
		list2193000FileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog list2193000FileDialog = new FileDialog(accrualGroup
						.getShell());
				list2193000FileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				list2193000FileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = list2193000FileDialog.open();
				if (detailFileName != null) {
					list2193000File.setText(detailFileName);
				}
			};
		});

		final Label backBillingListLabel = new Label(backBillingGroup, SWT.NONE);
		backBillingListLabel.setText("请选择追补清单表：");
		backBillingListLabel.setBackground(whiteColor);
		final Text backBillingListFile = new Text(backBillingGroup, SWT.BORDER);
		backBillingListFile.setEditable(false);
		GridData backBillingListFileData = new GridData();
		backBillingListFileData.widthHint = 550;
		backBillingListFileData.horizontalSpan = 4;
		backBillingListFile.setLayoutData(backBillingListFileData);
		backBillingListFile.setBackground(whiteColor);

		Button backBillingListFileChoose = new Button(backBillingGroup,
				SWT.NONE);
		backBillingListFileChoose.setText("选择文件");
		backBillingListFileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog backBillingListFileDialog = new FileDialog(
						accrualGroup.getShell());
				backBillingListFileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				backBillingListFileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = backBillingListFileDialog.open();
				if (detailFileName != null) {
					backBillingListFile.setText(detailFileName);
				}
			};
		});

		Label backBillingMonthLabel = new Label(backBillingGroup, SWT.NONE);
		backBillingMonthLabel.setText("请选择月份：");
		backBillingMonthLabel.setBackground(whiteColor);

		final Combo backBillingMonthCombo = new Combo(backBillingGroup,
				SWT.NONE | SWT.READ_ONLY);
		for (int i = 1; i <= 12; i++) {
			backBillingMonthCombo.add(i + "月");
		}
		GridData backBillingComboData = new GridData();
		backBillingComboData.widthHint = 100;
		backBillingComboData.horizontalSpan = 5;
		backBillingMonthCombo.setLayoutData(backBillingComboData);
		backBillingMonthCombo
				.select(Calendar.getInstance().get(Calendar.MONTH));

		Button backBillingButton = new Button(backBillingGroup, SWT.NONE);
		GridData backBillingButtonData = new GridData();
		backBillingButtonData.horizontalSpan = 6;
		backBillingButton.setLayoutData(backBillingButtonData);
		backBillingButton.setText("生成");

		final Group backBillingReplyGroup = new Group(formComposite, SWT.NONE);
		backBillingReplyGroup.setText("释放表");
		GridData backBillingReplyData = new GridData(GridData.FILL_HORIZONTAL);
		backBillingReplyData.horizontalSpan = 6;
		backBillingReplyGroup.setLayoutData(backBillingReplyData);
		GridLayout backBillingReplyLayout = new GridLayout();
		backBillingReplyLayout.numColumns = 6;
		backBillingReplyLayout.verticalSpacing = 10;
		backBillingReplyGroup.setLayout(backBillingReplyLayout);
		backBillingReplyGroup.setBackground(whiteColor);
		final Label list2192041ReplyLabel = new Label(backBillingReplyGroup,
				SWT.NONE);
		list2192041ReplyLabel.setText("请选择2192041清单表：");
		list2192041ReplyLabel.setBackground(whiteColor);
		final Text list2192041ReplyFile = new Text(backBillingReplyGroup,
				SWT.BORDER);
		list2192041ReplyFile.setEditable(false);
		GridData list2192041ReplyFileData = new GridData();
		list2192041ReplyFileData.widthHint = 550;
		list2192041ReplyFileData.horizontalSpan = 4;
		list2192041ReplyFile.setLayoutData(list2192041ReplyFileData);
		list2192041ReplyFile.setBackground(whiteColor);

		Button list2192041ReplyFileChoose = new Button(backBillingReplyGroup,
				SWT.NONE);
		list2192041ReplyFileChoose.setText("选择文件");
		list2192041ReplyFileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog list2192041ReplyFileDialog = new FileDialog(
						accrualGroup.getShell());
				list2192041ReplyFileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				list2192041ReplyFileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = list2192041ReplyFileDialog.open();
				if (detailFileName != null) {
					list2192041ReplyFile.setText(detailFileName);
				}
			};
		});

		final Label list2193000ReplyLabel = new Label(backBillingReplyGroup,
				SWT.NONE);
		list2193000ReplyLabel.setText("请选择2193000清单表：");
		list2193000ReplyLabel.setBackground(whiteColor);
		final Text list2193000ReplyFile = new Text(backBillingReplyGroup,
				SWT.BORDER);
		list2193000ReplyFile.setEditable(false);
		GridData list2193000ReplyFileData = new GridData();
		list2193000ReplyFileData.widthHint = 550;
		list2193000ReplyFileData.horizontalSpan = 4;
		list2193000ReplyFile.setLayoutData(list2193000ReplyFileData);
		list2193000ReplyFile.setBackground(whiteColor);

		Button list2193000ReplyFileChoose = new Button(backBillingReplyGroup,
				SWT.NONE);
		list2193000ReplyFileChoose.setText("选择文件");
		list2193000ReplyFileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog list2193000ReplyFileDialog = new FileDialog(
						accrualGroup.getShell());
				list2193000ReplyFileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				list2193000ReplyFileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = list2193000ReplyFileDialog.open();
				if (detailFileName != null) {
					list2193000ReplyFile.setText(detailFileName);
				}
			};
		});

		final Label backBillingReplyLabel = new Label(backBillingReplyGroup,
				SWT.NONE);
		backBillingReplyLabel.setText("请选择回复表：");
		backBillingReplyLabel.setBackground(whiteColor);
		final Text backBillingReplyFile = new Text(backBillingReplyGroup,
				SWT.BORDER);
		backBillingReplyFile.setEditable(false);
		GridData backBillingReplyFileData = new GridData();
		backBillingReplyFileData.widthHint = 550;
		backBillingReplyFileData.horizontalSpan = 4;
		backBillingReplyFile.setLayoutData(backBillingReplyFileData);
		backBillingReplyFile.setBackground(whiteColor);

		Button backBillingReplyFileChoose = new Button(backBillingReplyGroup,
				SWT.NONE);
		backBillingReplyFileChoose.setText("选择文件");
		backBillingReplyFileChoose.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog backBillingReplyFileDialog = new FileDialog(
						accrualGroup.getShell());
				backBillingReplyFileDialog
						.setFilterNames(ExcelFields.EXCEL_FILTER_NAMES);
				backBillingReplyFileDialog
						.setFilterExtensions(ExcelFields.EXCEL_FILTER_EXTENSIONS);
				detailFileName = backBillingReplyFileDialog.open();
				if (detailFileName != null) {
					backBillingReplyFile.setText(detailFileName);
				}
			};
		});

		Label backBillingReplayMonthLabel = new Label(backBillingReplyGroup,
				SWT.NONE);
		backBillingReplayMonthLabel.setText("请选择月份：");
		backBillingReplayMonthLabel.setBackground(whiteColor);

		final Combo backBillingReplyMonthCombo = new Combo(
				backBillingReplyGroup, SWT.NONE | SWT.READ_ONLY);
		for (int i = 1; i <= 12; i++) {
			backBillingReplyMonthCombo.add(i + "月");
		}
		GridData backBillingReplyComboData = new GridData();
		backBillingReplyComboData.widthHint = 100;
		backBillingReplyComboData.horizontalSpan = 5;
		backBillingReplyMonthCombo.setLayoutData(backBillingReplyComboData);
		backBillingReplyMonthCombo.select(Calendar.getInstance().get(
				Calendar.MONTH));

		Label replayPostingDateLabel = new Label(backBillingReplyGroup,
				SWT.NONE);
		replayPostingDateLabel.setText("请选择Posting日期：");
		replayPostingDateLabel.setBackground(whiteColor);

		final DateTime replyPostDate = new DateTime(backBillingReplyGroup,
				SWT.DATE | SWT.BORDER);
		GridData replyPostDateData = new GridData();
		replyPostDateData.horizontalSpan = 5;
		replyPostDate.setLayoutData(replyPostDateData);

		backBillingReplyButton = new Button(backBillingReplyGroup, SWT.NONE);
		backBillingReplyButton.setText("生成");

		final Composite errorGroup = new Composite(this.parent, SWT.NONE);
		GridData errorGroupData = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.FILL_VERTICAL);
		errorGroup.setLayoutData(errorGroupData);
		errorGroup.setLayout(new FillLayout());
		errorGroup.setBackground(errorGroup.getDisplay().getSystemColor(
				SWT.COLOR_BLUE));

		final Table messageTable = new Table(errorGroup, SWT.FULL_SELECTION
				| SWT.BORDER);
		messageTable.setLinesVisible(true);
		messageTable.setHeaderVisible(true);
		MessageProvider.getInstance().addHandler(
				new ClientMessage(messageTable));

		generateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (detailFileName != null) {
						// progressBar.getPb().setVisible(true);
						// progressBar.show();
						final String wbsFileName = salaryWbsFile.getText();
						final String detailFileName = detailFile.getText();
						final String postingDate = salaryPostDate.getYear()
								+ StringUtils.padLeft(
										(salaryPostDate.getMonth() + 1) + "",
										2, "0")
								+ StringUtils.padLeft(salaryPostDate.getDay()
										+ "", 2, "0");
						final String pr = prCombo.getText();
						final int month = monthCombo.getSelectionIndex() + 1;
						new Thread() {
							@Override
							public void run() {
								SalaryGenerator generator = new SalaryGeneratorImpl();
								generator.load(wbsFileName, detailFileName);
								String generatedFile = generator.generate(pr,
										month, postingDate);
								String exportFile = (FileUtils.getSystemPath()
										+ "/" + POIWriter.DEFAULT_FOLDER + generatedFile
										.substring(0,
												generatedFile.lastIndexOf("/")))
										.replace("/", "\\");
								MessageProvider.getInstance().publicMessage(
										"生成工资表成功!文件保存在：" + exportFile);
								try {
									Runtime.getRuntime().exec(
											"explorer " + exportFile);
								} catch (IOException e) {
									// MYTAG Auto-generated catch block
									e.printStackTrace();
								}
							}
						}.start();

					} else {
						System.out.println("Detial File is not selected.");
					}
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
		});

		accrualButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AccrualGenerator generator = new AccrualGeneratorImpl();
				try {
					if (listingFile.getText().indexOf("清") < 0) {
						MessageProvider.getInstance().publicMessage(
								Message.ERROR, "清单文件名必须包含‘清单’两个字！");
					}
					generator.load(wbsFile.getText(), listingFile.getText());
					int month = accrualMonthCombo.getSelectionIndex() + 1;
					String generatedFile = generator.generate(month + "");
					Runtime.getRuntime().exec(
							("explorer " + FileUtils.getSystemPath() + "/"
									+ POIWriter.DEFAULT_FOLDER + generatedFile
									.substring(0,
											generatedFile.lastIndexOf("/")))
									.replace("/", "\\"));
				} catch (IOException e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
				}
			}
		});

		wipButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final WIPGenerator generator = new WIPGeneratorImpl();
				final String wip3910FileName = wip3910File.getText();
				final String wip3940FileName = wip3940File.getText();
				final int month = wipMonthCombo.getSelectionIndex() + 1;
				try {
					if (wipType.getSelectionIndex() == 0) {
						// 生成设备文件
						generator.loadDevice(wip3910FileName, wip3940FileName);
						generator.generateDevice(month + "");
					} else {
						// 生成安装文件
						generator.loadSetup(wip3910FileName, wip3940FileName);
						generator.generateSetup(month + "");
					}

					// String generatedFile = generator.generate(month + "");
					// Runtime.getRuntime().exec(
					// ("explorer " + FileUtils.getSystemPath() + "/"
					// + POIWriter.DEFAULT_FOLDER + generatedFile
					// .substring(0,
					// generatedFile.lastIndexOf("/")))
					// .replace("/", "\\"));
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
				}
			}
		});

		backBillingButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final BackBillingGenerator generator = new BackBillingGeneratorImpl();
				final String wbsFileName = backBillingWbsFile.getText();
				final String list2192041FileName = list2192041File.getText();
				final String list2193000FileName = list2193000File.getText();
				final String backBillingFileName = backBillingListFile
						.getText();
				final int month = backBillingMonthCombo.getSelectionIndex() + 1;
				try {
					new Thread() {
						@Override
						public void run() {
							generator.loadBackBilling(wbsFileName,
									list2192041FileName, list2193000FileName,
									backBillingFileName);
							String generatedFile = generator
									.generateBackBilling(month + "");
							FileUtils.popupFilePath("/"
									+ POIWriter.DEFAULT_FOLDER
									+ generatedFile.substring(0,
											generatedFile.lastIndexOf("/")));
							MessageProvider.getInstance().publicMessage(
									"生成追补释放表成功!文件保存在：" + generatedFile);
						}
					}.start();
				} catch (Exception e1) {
					System.out.println(e1.getMessage());
					e1.printStackTrace();
				}
			}
		});

		backBillingReplyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final BackBillingGenerator generator = new BackBillingGeneratorImpl();
				final String replyFileName = backBillingReplyFile.getText();
				final String list2192041FileName = list2192041ReplyFile
						.getText();
				final String list2193000FileName = list2193000ReplyFile
						.getText();
				final int month = backBillingReplyMonthCombo
						.getSelectionIndex() + 1;
				final String postDate = replyPostDate.getYear()
						+ StringUtils.padLeft((replyPostDate.getMonth() + 1)
								+ "", 2, "0")
						+ StringUtils.padLeft(replyPostDate.getDay() + "", 2,
								"0");
				new Thread() {
					public void run() {
						generator.loadReply(replyFileName, list2192041FileName,
								list2193000FileName);
						generator.generateReplyVoucher(month + "", postDate);
					};
				}.start();
			}
		});

		salaryToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				formLayout.topControl = salaryGroup;
				formComposite.layout();
			}
		});

		accrualToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				formLayout.topControl = accrualGroup;
				formComposite.layout();
			}
		});

		wipToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				formLayout.topControl = wipGroup;
				formComposite.layout();
			}
		});

		backBillingItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				formLayout.topControl = backBillingGroup;
				formComposite.layout();
			}
		});

		releaseBillItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				formLayout.topControl = backBillingReplyGroup;
				formComposite.layout();
			}
		});

		CallListener.getInstance().listenAllEvent(this);
	}

	@Override
	public void backBillingStart() {
		backBillingReplyButton.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				backBillingReplyButton.setVisible(false);
			}
		});
	}

	@Override
	public void backBillingFinish() {
		backBillingReplyButton.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				backBillingReplyButton.setVisible(true);
			}
		});
	}
}
