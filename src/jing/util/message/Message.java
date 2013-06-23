package jing.util.message;

public class Message {
	public final static int DEBUG = 0;
	public final static int INFO = 1;
	public final static int WARNING = 2;
	public final static int ERROR = 3;

	private int type;
	private String message;
	boolean isHighLight;

	public Message(String message) {
		this.type = Message.INFO;
		this.message = message;
	}

	public Message(int type, String message) {
		this.type = type;
		this.message = message;
	}

	public Message(int type, String message, boolean isHighLight) {
		this.type = type;
		this.message = message;
		this.isHighLight = isHighLight;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isHighLight() {
		return isHighLight;
	}

	public void setHighLight(boolean isHighLight) {
		this.isHighLight = isHighLight;
	}
}
