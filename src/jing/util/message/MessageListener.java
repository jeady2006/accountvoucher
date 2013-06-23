package jing.util.message;

public interface MessageListener {
	void handleError(Message m);
	void handleInfo(Message m);
	void handleWarning(Message m);
	void handleDebug(Message m);
}
