package ptp.ui;

import ptp.local.LocalProxyServer;
import ptp.local.SSLForwardServer;
import ptp.pac.PacServer;

public class CLILauncher extends Launcher {
	public static void main(String[] args) {
		Thread sslForwarderThread = new Thread(new SSLForwardServer());
		sslForwarderThread.start();
		Thread localProxyThread = new Thread(new LocalProxyServer());
		localProxyThread.start();
		Thread pacThread = new Thread(new PacServer());
		pacThread.start();
	}

}
