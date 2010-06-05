package ptp.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.mp.MethodProcesser;
import ptp.util.HttpUtil;

public class LocalProxyServer {

	private static Logger log = Logger.getLogger(LocalProxyServer.class);

	int localProxyPort;
	int localProxyTimeOut;

	boolean isStopped = false;

	ServerSocket localProxyServerSocket;

	public LocalProxyServer() {
		localProxyPort = Integer.parseInt(Config.getIns().getValue(
				"ptp.local.proxy.port", "8887"));
		localProxyTimeOut = 1000;
	}

	public void startService() throws Exception {
		try {
			localProxyServerSocket = new ServerSocket(localProxyPort);
		} catch (IOException e) {
			log.error("create local proxy server socket failed", e);
			throw e;
		}
		log.info("local proxy server started on port: " + localProxyPort);

		localProxyServerSocket.setSoTimeout(1000);

		while (!isStopped) {
			Socket browserSocket = null;
			try {
				browserSocket = localProxyServerSocket.accept();
			} catch (SocketTimeoutException ste) {
				continue;
			}
			if (browserSocket != null) {
				log.info("visit from browser: "
						+ browserSocket.getInetAddress().getHostAddress() + " "
						+ browserSocket.getPort());
				Thread localProxyProcessThread = new Thread(
						new LocalProxyProcessThread(browserSocket));
				localProxyProcessThread.start();
				log.debug("thread count: " + Thread.activeCount());
			}
		}
		localProxyServerSocket.close();
		log.info("stop local proxy server");
	}

	public void stopService() {
		isStopped = true;
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
		process();
	}

	private void process() {
		InputStream inFromBrowser = null;
		OutputStream outToBrowser = null;
		try {
			inFromBrowser = browserSocket.getInputStream();
			outToBrowser = browserSocket.getOutputStream();
		} catch (IOException e) {
			log.error("failed to open stream on browser socket", e);
		}

		byte[] browserRequestHeadBuff = new byte[1024*50];

		int retry = 0;
		int processTimes = 0;
		while (browserSocket.isConnected()) {
			int availableBytes = 0;
			try {
				availableBytes = inFromBrowser.available();
			} catch (IOException e) {
				log.error(e.getMessage());
				break;
			}
			if (availableBytes > 0) {
				// try to read proxy request head
				int proxyReqHeadLen = 0;
				try {
					proxyReqHeadLen = HttpUtil
							.readHttpHead(browserRequestHeadBuff, inFromBrowser);
				} catch (IOException e) {
					log.error("failed to read browser http head", e);
					HttpUtil.writeErrorResponse(outToBrowser,
							"failed to read browser http head");
				}

				MethodProcesser mp = MethodProcesser.getIns(browserRequestHeadBuff,
						proxyReqHeadLen, inFromBrowser, outToBrowser);
				mp.process();
				processTimes++;
			} else {
				if (retry > 0) {
					break;
				}
				try {
					log.info("local proxy thread wait");
					Thread.sleep(150);
					retry++;
				} catch (InterruptedException e) {
					log.error(e.getMessage());
					break;
				}
			}

		}

		try {
			inFromBrowser.close();
			outToBrowser.close();
			browserSocket.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		log.info("local proxy thread end, it process " + processTimes + " requests from browser");
	}

}
