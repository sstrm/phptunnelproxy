package ptp.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509KeyManager;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.ByteArrayUtil;

public class SSLForwardServer {
	private static Logger log = Logger.getLogger(SSLForwardServer.class);

	private String destHost;
	private int destPort;

	public SSLForwardServer(String destHost, int destPort) {
		this.destHost = destHost;
		this.destPort = destPort;
	}

	public int start() {

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
					new SSLForwardServerProcessThread(sslServerSocket, destHost, destPort));
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

	SSLForwardServerProcessThread(SSLServerSocket sslServerSocket, String destHost, int destPort) {
		this.sslServerSocket = sslServerSocket;
		this.destHost = destHost;
		this.destPort = destPort;
	}

	@Override
	public void run() {
		try {

			SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

			DataInputStream in = new DataInputStream(sslSocket.getInputStream());

			DataOutputStream out = new DataOutputStream(sslSocket
					.getOutputStream());

			int buffSize = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.request.size", "10240"));
			byte[] buff = new byte[buffSize];

			int readCount = in.read(buff, 0, buffSize);

			int headerLen = ByteArrayUtil.firstIndexof("\r\n\r\n".getBytes(),
					buff, 0, readCount) + 2;
			String requestHeaderString = ByteArrayUtil.toString(buff, 0,
					headerLen);

			String[] requestHeaders = requestHeaderString.split("\\r\\n");
			byte[] requestBody = ByteArrayUtil.trim(buff, headerLen + 2,
					readCount - headerLen - 2);

			//String destHost = null;
			for (String header : requestHeaders) {

				if (header.toUpperCase().startsWith(
						"Content-Length:".toUpperCase())) {
					int bodyLen = Integer.parseInt(header.split(":\\s")[1]);
					if (requestBody.length < bodyLen) {
						byte[] requestBodyTmp = new byte[bodyLen];
						ByteArrayUtil.copy(requestBody, 0, requestBodyTmp, 0,
								requestBody.length);
						in.read(requestBodyTmp, requestBody.length, bodyLen);
						requestBody = requestBodyTmp;
					}
				}
			}
			log.info("destHost: " + destHost);
			destHost = Config.getIns().getIp(destHost);
			log.info("destPort: " + destPort);

			log.debug("ssl headers: " + requestHeaderString);
			log.debug("ssl body: "
					+ ByteArrayUtil
							.toString(requestBody, 0, requestBody.length));
			HttpProxy httpProxy = new HttpProxy(destHost, destPort, in, out);
			httpProxy.proces(requestHeaders, requestBody);

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