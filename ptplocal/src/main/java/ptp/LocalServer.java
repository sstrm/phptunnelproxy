package ptp;

import org.apache.log4j.PropertyConfigurator;

import ptp.local.SSLForwardServer;

public class LocalServer {
	public static void main(String[] args) {
		PropertyConfigurator.configure(LocalServer.class
				.getResource("/etc/log4j.properties"));
		Thread sslForwarderThread = new Thread(new SSLForwardServer());
		sslForwarderThread.start();
		Thread localProxyThread = new Thread(new LocalProxy());
		localProxyThread.start();
	}

}
