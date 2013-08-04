package jing.client;

import java.text.DateFormat;
import java.util.Date;

import jing.util.message.Message;
import jing.util.message.MessageListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ClientMessage implements MessageListener {
	private Table table;

	public ClientMessage(final Table table) {
		this.table = table;
		TableColumn typeTitle = new TableColumn(this.table, SWT.NONE
				| SWT.CENTER);
		typeTitle.setText("类型");
		typeTitle.setWidth(50);
		TableColumn dateTitle = new TableColumn(this.table, SWT.NONE
				| SWT.CENTER);
		dateTitle.setText("时间");
		dateTitle.setWidth(150);
		TableColumn contextTitle = new TableColumn(this.table, SWT.CENTER);
		contextTitle
				.setText("                                                                                     消息");
		contextTitle.setWidth(800);
		contextTitle.setAlignment(SWT.LEFT);
	}

	@Override
	public void handleError(Message m) {
		this.addMessage(Message.ERROR, m);
	}

	@Override
	public void handleInfo(final Message m) {
		this.addMessage(Message.INFO, m);
	}

	@Override
	public void handleDebug(final Message m) {
		this.addMessage(Message.DEBUG, m);
	}

	@Override
	public void handleWarning(Message m) {
		this.addMessage(Message.WARNING, m);
	}

	private void addMessage(final int type, final Message m) {
		final String typeLabel = type == Message.ERROR ? "错误"
				: (type == Message.INFO ? "信息"
						: (type == Message.WARNING ? "警告" : "调试"));
		final int color = type == Message.ERROR ? SWT.COLOR_RED
				: (type == Message.INFO ? SWT.COLOR_BLACK
						: (type == Message.WARNING ? SWT.COLOR_YELLOW
								: SWT.COLOR_GRAY));
		this.table.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				TableItem item = new TableItem(table, SWT.LEFT);
				item.setText(0, typeLabel);
				item.setText(1, DateFormat.getInstance().format(new Date()));
				item.setText(2, m.getMessage());
				item.setForeground(item.getDisplay().getSystemColor(color));
				if (m.isHighLight()) {
					item.setBackground(item.getDisplay().getSystemColor(
							SWT.COLOR_CYAN));
				}
			}
		});
	}

}
