package ptp.net.test;

import ptp.net.LocalProxyServer;
import ptp.pac.PacServer;
import ptp.ui.Launcher;

public class LocalProxyServerTry extends Launcher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			PacServer pacServer = new PacServer();
			Thread pacThread = new Thread(pacServer);
			pacThread.start();
			LocalProxyServer lps = new LocalProxyServer();
			lps.startService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
