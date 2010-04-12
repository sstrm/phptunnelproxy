package ptp.ui;

import ptp.local.LocalProxyServer;
import ptp.local.SSLForwardServer;

public class CLILauncher extends Launcher {
	public static void main(String[] args) {
		Thread sslForwarderThread = new Thread(new SSLForwardServer());
		sslForwarderThread.start();
		Thread localProxyThread = new Thread(new LocalProxyServer());
		localProxyThread.start();
	}

}
