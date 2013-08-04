package jing.client.wiget.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MessageQueue {
	public static void main(String[] args) {
		final Display d = Display.getDefault();
		Shell s = new Shell(d);
		s.setBounds(100, 100, 300, 400);
		s.setLayout(new FillLayout());

		final Text t = new Text(s, SWT.NONE);
		t.setBackground(d.getSystemColor(SWT.COLOR_WHITE));
		s.open();

		final List<String> l = new ArrayList<String>();
		new Thread() {
			public void run() {
				try {
					for (int i = 0; i < 1000; i++) {
						synchronized (l) {
							l.add(i + "");
						}
						sleep(200);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
		new Thread() {
			public void run() {
				while (true) {
					if (l.size() > 0) {
						synchronized (l) {
							while (l.size() > 0) {
								final String[] s = new String[1];
								s[0] = l.remove(0);
								System.out.println(s);
								d.asyncExec(new Runnable() {

									@Override
									public void run() {
										t.setText(t.getText() + " " + s[0]);

									}
								});
							}
						}
					} else {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							// MYTAG Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
		while (!s.isDisposed()) {
			if (!d.readAndDispatch()) {
				d.sleep();
			}
		}
	}
}
