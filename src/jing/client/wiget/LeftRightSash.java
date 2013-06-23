package jing.client.wiget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

public class LeftRightSash {
	private Composite parentComp;
	private Composite wrapper;
	private Composite left;
	private Composite right;

	public LeftRightSash(Composite c) {
		this.parentComp = c;
		this.init();
	}

	private void init() {
		this.wrapper = new Composite(this.parentComp, SWT.NONE);
		final FormLayout form = new FormLayout();
		this.wrapper.setLayout(form);

		left = new Composite(wrapper, SWT.BORDER);
		left.setBackground(new Color(left.getDisplay(), 240, 240, 240));
		right = new Composite(wrapper, SWT.BORDER);
		right.setBackground(right.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		left.setLayout(new FillLayout());
		right.setLayout(new FillLayout());

		final Sash sash = new Sash(wrapper, SWT.VERTICAL);
		final FormLayout layout = new FormLayout();
		wrapper.setLayout(layout);
		FormData leftFormData = new FormData();
		leftFormData.left = new FormAttachment(0, 0);
		leftFormData.right = new FormAttachment(sash, 0);
		leftFormData.top = new FormAttachment(0, 0);
		leftFormData.bottom = new FormAttachment(100, 0);
		left.setLayoutData(leftFormData);

		final int limit = 20, percent = 10;
		final FormData sashData = new FormData();
		sashData.left = new FormAttachment(percent, 0);
		sashData.top = new FormAttachment(0, 0);
		sashData.bottom = new FormAttachment(100, 0);
		sash.setLayoutData(sashData);
		sash.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				Rectangle sashRect = sash.getBounds();
				Rectangle shellRect = wrapper.getClientArea();
				int right = shellRect.width - sashRect.width - limit;
				e.x = Math.max(Math.min(e.x, right), limit);
				if (e.x != sashRect.x) {
					sashData.left = new FormAttachment(0, e.x);
					wrapper.layout();
				}
			}
		});

		FormData rightFormData = new FormData();
		rightFormData.left = new FormAttachment(sash, 0);
		rightFormData.right = new FormAttachment(100, 0);
		rightFormData.top = new FormAttachment(0, 0);
		rightFormData.bottom = new FormAttachment(100, 0);
		right.setLayoutData(rightFormData);
	}

	public Composite getParentComp() {
		return parentComp;
	}

	public void setParentComp(Composite parentComp) {
		this.parentComp = parentComp;
	}

	public Composite getLeft() {
		return left;
	}

	public void setLeft(Composite left) {
		this.left = left;
	}

	public Composite getRight() {
		return right;
	}

	public void setRight(Composite right) {
		this.right = right;
	}

	public Composite getWrapper() {
		return wrapper;
	}

	public void setWrapper(Composite wrapper) {
		this.wrapper = wrapper;
	}

}
