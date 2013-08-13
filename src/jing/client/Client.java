package jing.client;

import jing.client.wiget.MainFrame;

public class Client {
	public void open() {
		try {
			new MainFrame("财务报表生成系统");
			// LeftRightSash sash = new LeftRightSash(mainFrame.getWrapper());

			// mainFrame.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client().open();
		
	}
}
