package ptp.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

	int buffSize;

	public LocalProxyProcessThread(Socket browserSocket) {
		this.browserSocket = browserSocket;
		buffSize = Integer.parseInt(Config.getIns().getValue(
				"ptp.local.request.size", "10240"));
	}

	@Override
	public void run() {
		process();
		log.info("local proxy server thread end");
	}

	private void process() {
		DataInputStream inFromBrowser = null;
		DataOutputStream outToBrowser = null;
		try {
			inFromBrowser = new DataInputStream(browserSocket.getInputStream());
			outToBrowser = new DataOutputStream(browserSocket.getOutputStream());
		} catch (IOException e) {
			log.error("failed to open stream on browser socket", e);
		}

		byte[] buff = new byte[buffSize];

		// try to read proxy request head
		int proxyReqHeadLen = 0;
		try {
			proxyReqHeadLen = HttpUtil.readHttpHead(buff, inFromBrowser);
		} catch (IOException e) {
			log.error("failed to read browser http head", e);
			HttpUtil.writeErrorResponse(outToBrowser,
					"failed to read browser http head");
		}

		MethodProcesser mp = MethodProcesser.getIns(buff, proxyReqHeadLen,
				inFromBrowser, outToBrowser);
		mp.process();
	}

}
