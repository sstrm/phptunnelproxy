package ptp.ui;

import org.apache.log4j.PropertyConfigurator;

import ptp.local.LocalProxyServer;
import ptp.local.SSLForwardServer;

public class CLILauncher {
	public static void main(String[] args) {
		PropertyConfigurator.configure(CLILauncher.class
				.getResource("/etc/log4j.properties"));
		Thread sslForwarderThread = new Thread(new SSLForwardServer());
		sslForwarderThread.start();
		Thread localProxyThread = new Thread(new LocalProxyServer());
		localProxyThread.start();
	}

}
