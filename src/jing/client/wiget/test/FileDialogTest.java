package jing.client.wiget.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class FileDialogTest {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// MYTAG Auto-generated method stubDisplay display = new Display ();
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.open();
		FileDialog dialog = new FileDialog(shell);
		String[] filterNames = new String[] { "Image Files", "All Files (*)" };
		String[] filterExtensions = new String[] {
				"*.gif;*.png;*.xpm;*.jpg;*.jpeg;*.tiff", "*" };
		String filterPath = "/";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterNames = new String[] { "Image Files", "All Files (*.*)" };
			filterExtensions = new String[] {
					"*.gif;*.png;*.bmp;*.jpg;*.jpeg;*.tiff", "*.*" };
			filterPath = "c:\\";
		}
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFilterPath(filterPath);
		dialog.setFileName("myfile");
		System.out.println("Save to: " + dialog.open());
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
