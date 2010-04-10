package ptp;

import ptp.local.SSLForwardServer;

public class LocalServer {
	public static void main(String[] args) {
		Thread sslForwarderThread = new Thread(new SSLForwardServer());
		sslForwarderThread.start();
		Thread localProxyThread = new Thread(new LocalProxy());
		localProxyThread.start();
	}

}
