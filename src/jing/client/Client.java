package jing.client;

import jing.client.wiget.MainFrame;

public class Client {
	public void open() {
		try {
			new MainFrame("���񱨱�����ϵͳ");
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
