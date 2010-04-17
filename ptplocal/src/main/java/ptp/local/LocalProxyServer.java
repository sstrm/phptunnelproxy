package ptp.local;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.ByteArrayUtil;

public class LocalProxyServer implements Runnable {
	private static Logger log = Logger.getLogger(LocalProxyServer.class);

	private boolean isStopped = false;

	public synchronized void stopServer() {
		isStopped = true;
	}

	@Override
	public void run() {
		ServerSocket sSocket = null;
		try {
			int localProxyPort = Integer.parseInt(Config.getIns().getValue(
					"ptp.local.proxy.port", "8888"));
			sSocket = new ServerSocket(localProxyPort);
			sSocket.setSoTimeout(1000);
			log.info("local proxy server started on port: " + localProxyPort);

			while (!isStopped) {
				Socket browserSocket = null;
				try {
					browserSocket = sSocket.accept();
				} catch (SocketTimeoutException ste) {
					continue;
				}
				if (browserSocket != null) {
					log.info("visit from browser: "
							+ browserSocket.getInetAddress().getHostAddress()
							+ " " + browserSocket.getPort());
					Thread localProxyProcessThread = new Thread(
							new LocalProxyProcessThread(browserSocket));
					localProxyProcessThread.start();
					log.info("thread count: " + Thread.activeCount());
				}
			}
			sSocket.close();
			log.info("stop local proxy server");
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
				InputStream sslIn = sslSocket.getInputStream();
				OutputStream sslOut = sslSocket.getOutputStream();

				outToBrowser.write("HTTP/1.1 200 Connection established\r\n"
						.getBytes("US-ASCII"));
				outToBrowser.write("Proxy-agent: Mozilla/1.1\r\n"
						.getBytes("US-ASCII"));
				outToBrowser.write("Proxy-Connection: Keep-Alive\r\n"
						.getBytes("US-ASCII"));
				outToBrowser.write("\r\n".getBytes("US-ASCII"));

				outToBrowser.flush();

				Thread pipeThreadFromBrowserToSSLServer = new Thread(
						new PipeThread(inFromBrowser, sslOut, "1"));
				Thread pipeThreadFromSSLServerToBrowser = new Thread(
						new PipeThread(sslIn, outToBrowser, "2"));
				pipeThreadFromBrowserToSSLServer.start();
				pipeThreadFromSSLServerToBrowser.start();
				try {
					pipeThreadFromBrowserToSSLServer.join();
					pipeThreadFromSSLServerToBrowser.join();
				} catch (InterruptedException e) {
					log.error(e.getMessage(), e);
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

class PipeThread implements Runnable {
	private static Logger log = Logger.getLogger(PipeThread.class);
	InputStream in;
	OutputStream out;
	String title;

	PipeThread(InputStream in, OutputStream out, String title) {
		this.in = in;
		this.out = out;
		this.title = title;
	}

	@Override
	public void run() {
		int rc = 0;
		int buffSize = Integer.parseInt(Config.getIns().getValue(
				"ptp.buff.size", "1024"));
		byte[] buff = new byte[buffSize];
		try {
			while (true) {
				rc = in.read(buff);
				if (rc == -1) {
					break;
				}
				if (rc > 0) {
					out.write(buff, 0, rc);
					out.flush();
				}
			}
		} catch (final Exception e) {
			log.error(title + e.getMessage(), e);

		} finally {
			try {
				in.close();
				out.close();
			} catch (final Exception e) {
			}
		}

	}

}