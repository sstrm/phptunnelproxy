package ptp.net.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.ProxyException;
import ptp.net.mp.MethodProcesser;

public class SSLForwardServer {
	private static Logger log = Logger.getLogger(SSLForwardServer.class);

	private String destHost;
	private int destPort;

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

			Thread sslForwardServerProcessThread = new Thread(
					new SSLForwardServerProcessThread(sslServerSocket,
							destHost, destPort));
			sslForwardServerProcessThread.start();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return localSslPort;
	}
}

class SSLForwardServerProcessThread implements Runnable {
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
				writeErrorResponse(out, e);
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

	private void writeErrorResponse(OutputStream outToBrowser,
			ProxyException proxyException) {
		log.error("wirite error page for: " + proxyException.getMessage());
		PrintWriter w = new PrintWriter(new OutputStreamWriter(outToBrowser));
		w.write("HTTP/1.1 500 Internal Server Error\r\n");
		w.write("Content-Type: text/html; charset=utf-8\r\n");
		w.write("Connection: close\r\n");
		w.write("\r\n");

		w.write("<html>");
		w.write("<head><title>HTTP 500 Internal Server Error</title><head>");
		w.write("<body><pre>");
		proxyException.printStackTrace(w);
		w.write("</pre></body>");
		w.write("</html>");
		w.flush();
		w.close();
	}

}