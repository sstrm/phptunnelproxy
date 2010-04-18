package ptp.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.ByteArrayUtil;

public class SSLForwardServer implements Runnable {
	private static Logger log = Logger.getLogger(SSLForwardServer.class);
	
	private boolean isStopped = false;
	
	public synchronized void stopServer() {
		isStopped = true;
	}

	@Override
	public void run() {

		String ksName = "/etc/ptp.jks";
		char ksPass[] = "phptunnelproxykspass".toCharArray();
		char ctPass[] = "phptunnelproxyctpass".toCharArray();
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(SSLForwardServer.class.getResourceAsStream(ksName), ksPass);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ctPass);
			SSLContext sc = SSLContext.getInstance("SSLv3");
			sc.init(kmf.getKeyManagers(), null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			int localSslPort = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.ssl.port", "8889"));
			SSLServerSocket sslServerSocket = (SSLServerSocket) ssf
					.createServerSocket(localSslPort);
			sslServerSocket.setSoTimeout(1000);
			log.info("local ssl server started on port: " + localSslPort);
			
			while (!isStopped) {
				SSLSocket sslSocket = null;
				try {
					sslSocket = (SSLSocket) sslServerSocket.accept();
				} catch(SocketTimeoutException ste) {
					continue;
				}
				if(sslSocket != null) {
					Thread sslForwardServerProcessThread = new Thread(
							new SSLForwardServerProcessThread(sslSocket));
					sslForwardServerProcessThread.start();
				}
			}
			sslServerSocket.close();
			log.info("stop ssl server");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
}

class SSLForwardServerProcessThread implements Runnable {
	private static Logger log = Logger
			.getLogger(SSLForwardServerProcessThread.class);

	SSLSocket sslSocket;

	SSLForwardServerProcessThread(SSLSocket sslSocket) {
		this.sslSocket = sslSocket;
	}

	@Override
	public void run() {
		try {
			DataInputStream in = new DataInputStream(
					sslSocket.getInputStream());

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

			String destHost = null;
			for (String header : requestHeaders) {
				if (header.toUpperCase().startsWith("HOST: ".toUpperCase())) {
					destHost = header.split(":\\s")[1];
				}
				
				if (header.toUpperCase().startsWith("Content-Length:".toUpperCase())) {
					int bodyLen = Integer.parseInt(header.split(":\\s")[1]);
					if(requestBody.length<bodyLen) {
						byte[] requestBodyTmp = new byte[bodyLen];
						ByteArrayUtil.copy(requestBody, 0, requestBodyTmp, 0, requestBody.length);
						in.read(requestBodyTmp, requestBody.length, bodyLen);
						requestBody = requestBodyTmp;
					}
				}
			}
			log.info("destHost: " + destHost);
			destHost = Config.getIns().getIp(destHost);

			int destPort = 443;
			if (Boolean.parseBoolean(Config.getIns().getValue(
					"ptp.local.https.ashttp", "false"))) {
				destPort = 80;
				log.info("https used as http in remote");
			}
			log.info("destPort: " + destPort);

			log.debug("ssl headers: " + requestHeaderString);
			log.debug("ssl body: " + ByteArrayUtil.toString(requestBody, 0, requestBody.length));
			HttpProxy httpProxy = new HttpProxy(destHost, destPort, in, out);
			httpProxy.proces(requestHeaders, requestBody);
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				log.fatal(e.getMessage(), e);
			}
			
			in.close();
			out.close();
			sslSocket.close();
			log.info("ssl forward end!");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

}