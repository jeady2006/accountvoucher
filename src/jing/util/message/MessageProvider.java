package jing.util.message;

import java.util.ArrayList;
import java.util.List;

public class MessageProvider {
	private static MessageProvider instance;
	private List<Message> messages = new ArrayList<Message>();
	private List<MessageListener> handlers = new ArrayList<MessageListener>();

	public MessageProvider() {
		instance = this;
		new Thread() {
			public void run() {
				handleMessage();
			};
		}.start();
	}

	public static MessageProvider getInstance() {
		if (instance == null) {
			new MessageProvider();
		}
		return instance;
	}

	public void publicMessage(String m) {
		this.publicMessage(new Message(m));
	}

	public void publicMessage(int type, String m) {
		this.publicMessage(new Message(type, m));
	}

	public void publicMessage(Message m) {
		synchronized (messages) {
			messages.add(m);
			messages.notifyAll();
		}
	}

	public void handleMessage() {
		Message mess = null;
		List<Message> l = new ArrayList<Message>();
		while (true) {
			synchronized (messages) {
				if (messages.size() > 0) {
					while (messages.size() > 0) {
						l.add(messages.remove(0));
					}
				} else {
					try {
						messages.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (l.size() > 0) {
				while (l.size() > 0) {
					mess = l.remove(0);
					for (MessageListener listener : handlers) {
						if (mess.getType() == Message.DEBUG) {
							listener.handleDebug(mess);
						} else if (mess.getType() == Message.INFO) {
							listener.handleInfo(mess);
						} else if (mess.getType() == Message.ERROR) {
							listener.handleError(mess);
						} else if (mess.getType() == Message.WARNING){
							listener.handleWarning(mess);
						}
					}
				}

			}
		}
	}

	public void addHandler(MessageListener l) {
		synchronized (handlers) {
			this.handlers.add(l);
		}
	}
}
