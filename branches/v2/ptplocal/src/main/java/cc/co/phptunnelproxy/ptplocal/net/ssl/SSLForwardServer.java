package cc.co.phptunnelproxy.ptplocal.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;
import cc.co.phptunnelproxy.ptplocal.net.AbstractServer;
import cc.co.phptunnelproxy.ptplocal.net.AbstractServerProcessThread;
import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.net.mp.MethodProcesser;

public class SSLForwardServer extends AbstractServer {
	private static Logger log = Logger.getLogger(SSLForwardServer.class);

	private String destHost;
	private int destPort;

	private Thread sslForwardServerProcessThread;

	public SSLForwardServer(String destHost, int destPort) {
		this.destHost = destHost;
		this.destPort = destPort;
	}

	public int startService() {

		String ksName = "/etc/ptp.jks";
		char ksPass[] = "phptunnelproxykspass".toCharArray();
		char ctPass[] = "phptunnelproxyctpass".toCharArray();
		int localSslPort = Integer.parseInt(Config.getIns().getValue(
				"ptp.local.ssl.port", "8889"));
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(SSLForwardServer.class.getResourceAsStream(ksName), ksPass);
			SSLContext sc = SSLContext.getInstance("SSLv3");
			sc.init(new X509KeyManager[] { new AliasKeyManager(ks, ctPass,
					this.destHost) }, null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();

			SSLServerSocket sslServerSocket = null;
			while (true) {
				try {
					sslServerSocket = (SSLServerSocket) ssf
							.createServerSocket(localSslPort);
					break;
				} catch (java.net.BindException be) {
					localSslPort++;
				}
			}
			sslServerSocket.setSoTimeout(10000);
			log.info("local ssl server started on port: " + localSslPort);

			sslForwardServerProcessThread = new Thread(
					new SSLForwardServerProcessThread(sslServerSocket,
							destHost, destPort));
			sslForwardServerProcessThread.start();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return localSslPort;
	}

	@Override
	public boolean isServerOn() {
		return sslForwardServerProcessThread.isAlive();
	}

	@Override
	public void stopService() {
		// no need
	}

}

class SSLForwardServerProcessThread extends AbstractServerProcessThread {
	private static Logger log = Logger
			.getLogger(SSLForwardServerProcessThread.class);

	SSLServerSocket sslServerSocket;
	private String destHost;
	private int destPort;

	SSLForwardServerProcessThread(SSLServerSocket sslServerSocket,
			String destHost, int destPort) {
		this.sslServerSocket = sslServerSocket;
		this.destHost = destHost;
		this.destPort = destPort;
	}

	@Override
	public void run() {
		try {

			SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

			InputStream in = sslSocket.getInputStream();

			OutputStream out = sslSocket.getOutputStream();

			MethodProcesser mp;
			try {
				mp = MethodProcesser.getSSLIns(in, out, destHost, destPort);
				mp.process();
			} catch (ProxyException e) {
				writeErrorResponse(out, e, this.getClass());
			}

			in.close();
			out.close();
			sslSocket.close();
			sslServerSocket.close();
			log.info("ssl forward end!");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

}