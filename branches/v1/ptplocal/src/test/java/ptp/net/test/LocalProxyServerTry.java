package ptp.net.test;

import ptp.net.LocalProxyServer;
import ptp.ui.Launcher;

public class LocalProxyServerTry extends Launcher{

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		LocalProxyServer lps = new LocalProxyServer();
		try {
			lps.startService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
