package jing.client.wiget;

import jing.util.cache.ProcessCache;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class SimpleProgressBar implements Runnable {
	private Composite parent;
	private String cacheName;
	private ProgressBar pb = null;

	public SimpleProgressBar(Composite parent) {
		this(parent, SimpleProgressBar.class.getName());
	}

	public SimpleProgressBar(Composite parent, String cacheName) {
		this.parent = parent;
		this.cacheName = cacheName;
		pb = new ProgressBar(this.parent, SWT.NONE);
		// pb.setVisible(false);
	}

	public void show() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		ProcessCache.setCacheValue(cacheName, 0);
		final int[] i = new int[1];
		try {
			while (true) {
				i[0] = ProcessCache.getCacheValue(cacheName);
				pb.getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						pb.setSelection(i[0]);
					}
				});
				if (i[0] == 100) {
					this.wait(2000);
					break;
				} else {
					Thread.sleep(100);
				}
			}
		} catch (InterruptedException e) {
			// MYTAG Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ProgressBar getPb() {
		return pb;
	}

}
