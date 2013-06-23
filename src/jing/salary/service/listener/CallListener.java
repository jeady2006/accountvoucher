package jing.salary.service.listener;

import java.util.ArrayList;
import java.util.List;

public class CallListener {
	private static CallListener instance;
	private static List<BackBillingReplyListener> backBillingListeners = new ArrayList<BackBillingReplyListener>();

	private CallListener() {
		instance = this;
	}

	public static CallListener getInstance() {
		if (instance == null) {
			new CallListener();
		}
		return instance;
	}

	public void addBackBillingListener(BackBillingReplyListener listener) {
		if (backBillingListeners.contains(listener)) {
			return;
		}
		backBillingListeners.add(listener);
	}

	public void removeBackBillingListener(BackBillingReplyListener listener) {
		if (!backBillingListeners.contains(listener)) {
			return;
		}
		backBillingListeners.remove(listener);
	}

	public void notifyBackBillingStart() {
		for (BackBillingReplyListener l : backBillingListeners) {
			if (l != null) {
				l.backBillingStart();
			}
		}
	}

	public void notifyBackBillingFinish() {
		for (BackBillingReplyListener l : backBillingListeners) {
			if (l != null) {
				l.backBillingFinish();
			}
		}
	}

	public void listenAllEvent(GeneratorListener listener) {
		addBackBillingListener((BackBillingReplyListener) listener);
	}
}
