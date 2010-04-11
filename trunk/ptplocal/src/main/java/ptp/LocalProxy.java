package ptp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.apache.log4j.Logger;

import ptp.local.HttpProxy;
import ptp.util.ByteArrayUtil;

public class LocalProxy implements Runnable {
	private static Logger log = Logger.getLogger(LocalProxy.class);

	@Override
	public void run() {
		ServerSocket sSocket = null;
		try {
			int localProxyPort = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.proxy.port", "8888"));
			sSocket = new ServerSocket(localProxyPort);
			log.info("local proxy server started on port: " + localProxyPort);

			while (true) {
				Socket browserSocket = sSocket.accept();
				log.info("visit from browser: "
						+ browserSocket.getInetAddress().getHostAddress() + " "
						+ browserSocket.getPort());
				Thread localProxyProcessThread = new Thread(
						new LocalProxyProcessThread(browserSocket));
				localProxyProcessThread.start();
				log.info("thread count: " + Thread.activeCount());
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

}

class LocalProxyProcessThread implements Runnable {
	private static Logger log = Logger.getLogger(LocalProxyProcessThread.class);

	Socket browserSocket;

	public LocalProxyProcessThread(Socket browserSocket) {
		this.browserSocket = browserSocket;
	}

	@Override
	public void run() {
		// dispatch to httproxy or ssl forwarder
		try {
			DataInputStream inFromBrowser = new DataInputStream(
					new BufferedInputStream(browserSocket.getInputStream()));

			DataOutputStream outToBrowser = new DataOutputStream(browserSocket
					.getOutputStream());

			int buffSize = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.request.size", "10240"));
			byte[] buff = new byte[buffSize];

			int readCount = inFromBrowser.read(buff, 0, buffSize);

			if (ByteArrayUtil.toString(buff, 0, 7).equalsIgnoreCase("CONNECT")) {
				// https proxy
				log.debug("connect read cout: " + readCount);
				log.debug("connect request: "
						+ ByteArrayUtil.toString(buff, 0, readCount));

				Socket sslSocket = new Socket("127.0.0.1", 8889);
				DataInputStream sslIn = new DataInputStream(
						new BufferedInputStream(sslSocket.getInputStream()));
				DataOutputStream sslOut = new DataOutputStream(sslSocket
						.getOutputStream());

				outToBrowser.write("HTTP/1.1 200 Connection established\r\n"
						.getBytes("US-ASCII"));
				outToBrowser.write("Proxy-agent: Netscape-Proxy/1.1\r\n"
						.getBytes("US-ASCII"));
				outToBrowser.write("Proxy-Connection: close\r\n"
						.getBytes("US-ASCII"));
				outToBrowser.write("\r\n".getBytes("US-ASCII"));

				outToBrowser.flush();

				while (true) {
					if (browserSocket.isClosed()) {
						break;
					}
					if (inFromBrowser.available() > 0
							&& ((readCount = inFromBrowser.read(buff)) > 0)) {
						sslOut.write(buff, 0, readCount);
						sslOut.flush();
					} else if (readCount < 0) {
						break;
					}

					try {
						sslSocket.sendUrgentData(0);
					} catch (IOException e) {
						break;
					}
					if (sslIn.available() > 0
							&& ((readCount = sslIn.read(buff)) > 0)) {
						outToBrowser.write(buff, 0, readCount);
						outToBrowser.flush();
					} else if (readCount < 0) {
						break;
					}

				}

				sslOut.close();
				sslIn.close();
				sslSocket.close();

			} else {
				int headerLen = ByteArrayUtil.firstIndexof("\r\n\r\n"
						.getBytes(), buff, 0, readCount) + 2;
				String requestHeaderString = ByteArrayUtil.toString(buff, 0,
						headerLen);

				String[] requestHeaders = requestHeaderString.split("\\r\\n");
				byte[] requestBody = ByteArrayUtil.trim(buff, headerLen + 2,
						readCount - headerLen - 2);

				URL requestURL = new URL(requestHeaders[0].split("\\s")[1]);

				String destHost = requestURL.getHost();
				log.info("destHost: " + destHost);
				destHost = Config.getIns().getIp(destHost);

				int destPort = requestURL.getPort() != -1 ? requestURL
						.getPort() : 80;
				log.info("destPort: " + destPort);

				HttpProxy httpProxy = new HttpProxy(destHost, destPort,
						inFromBrowser, outToBrowser);
				httpProxy.proces(requestHeaders, requestBody);
			}

			inFromBrowser.close();
			outToBrowser.close();
			browserSocket.close();
			log.info("local proxy end!");
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				browserSocket.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}

	}

}