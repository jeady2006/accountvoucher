package jing.client.wiget;

import jing.client.salary.SalaryMain;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MainFrame {
	private String title;
	private Display display;
	private Shell shell;
	private Composite wrapper;

	public MainFrame(String title) {
		this.title = title;
		this.init();
	}

	public void init() {
		display = Display.getDefault();
		shell = new Shell(display);
		shell.setMaximized(true);
		shell.setText(this.title);
		shell.setImage(new Image(display, "icon/title.png"));
		shell.setLayout(new FillLayout(SWT.NONE));
		wrapper = new Composite(shell, SWT.NONE);
		wrapper.setBackground(new Color(display, 240, 240, 240));
		// if(this.layout == null){
		// this.layout = new GridLayout();
		// }
		// wrapper.setLayout(new FillLayout(SWT.NONE));

		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				System.exit(0);
			}
		});

		new SalaryMain(wrapper);

		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void show() {
		// shell.pack();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {

			}
		});
	}

	public Composite getWrapper() {
		return wrapper;
	}

	public Shell getShell() {
		return shell;
	}

}
