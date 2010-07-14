package cc.co.phptunnelproxy.ptplocal.net.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;
import cc.co.phptunnelproxy.ptplocal.net.AbstractServer;
import cc.co.phptunnelproxy.ptplocal.net.AbstractServerProcessThread;
import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.net.ThreadPoolService;
import cc.co.phptunnelproxy.ptplocal.net.mp.MethodProcesser;

public class HttpProxyServer extends AbstractServer {
	private static Logger log = Logger.getLogger(HttpProxyServer.class);

	HttpProxyServerThread httpProxyServerThread = null;

	public HttpProxyServer() {
		httpProxyServerThread = new HttpProxyServerThread();
	}

	public int startService() {
		httpProxyServerThread.start();
		return httpProxyServerThread.httpProxyPort;
	}

	public void stopService() {
		httpProxyServerThread.shutdown();
		try {
			httpProxyServerThread.join();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public boolean isServerOn() {
		return httpProxyServerThread.isAlive();
	}
}

class HttpProxyServerThread extends Thread {

	private static Logger log = Logger.getLogger(HttpProxyServer.class);

	int httpProxyPort;
	int httpProxyTimeOut;

	boolean isStopped = false;

	ServerSocket localProxyServerSocket;

	public HttpProxyServerThread() {
		httpProxyPort = Integer.parseInt(Config.getIns().getValue(
				"ptp.local.proxy.port", "8887"));
		httpProxyTimeOut = 1000;
	}

	public void shutdown() {
		isStopped = true;
	}

	public void run() {
		try {
			localProxyServerSocket = new ServerSocket(httpProxyPort);
		} catch (IOException e) {
			log.error("create local proxy server socket failed", e);
			return;
		}
		log.info("local proxy server started on port: " + httpProxyPort);

		try {
			localProxyServerSocket.setSoTimeout(1000);
		} catch (SocketException e) {
			log.error(e.getMessage(), e);
		}

		while (!isStopped) {
			Socket browserSocket = null;
			try {
				browserSocket = localProxyServerSocket.accept();
			} catch (SocketTimeoutException ste) {
				continue;
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				break;
			}
			if (browserSocket != null) {
				log.info("visit from browser: "
						+ browserSocket.getInetAddress().getHostAddress() + " "
						+ browserSocket.getPort());
				ThreadPoolService.submit(
						new HttpProxyProcessThread(browserSocket));
				log.info("current thread count: " + Thread.activeCount());
			}
		}
		try {
			localProxyServerSocket.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		log.info("stop local proxy server");
	}

}

class HttpProxyProcessThread extends AbstractServerProcessThread {
	private static Logger log = Logger.getLogger(HttpProxyProcessThread.class);

	Socket browserSocket;

	public HttpProxyProcessThread(Socket browserSocket) {
		this.browserSocket = browserSocket;
	}

	@Override
	public void run() {
		process();
		try {
			browserSocket.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void process() {
		InputStream inFromBrowser = null;
		OutputStream outToBrowser = null;
		try {
			inFromBrowser = browserSocket.getInputStream();
			outToBrowser = browserSocket.getOutputStream();
		} catch (IOException e) {
			log.error("failed to open stream on browser socket", e);
			return;
		}

		int retry = 0;
		int processTimes = 0;
		while (browserSocket.isConnected()) {
			int availableBytes = 0;
			try {
				availableBytes = inFromBrowser.available();
			} catch (IOException e) {
				log.info(e.getMessage());
				break;
			}
			if (availableBytes > 0) {
				MethodProcesser mp;
				try {
					mp = MethodProcesser.getIns(inFromBrowser, outToBrowser);
					mp.process();
				} catch (ProxyException e) {
					writeErrorResponse(outToBrowser, e, this.getClass());
				}

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
					log.info(e.getMessage());
					break;
				}
			}
		}

		try {
			inFromBrowser.close();
			outToBrowser.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		log.info(Thread.currentThread().getName() + " end, it proceed "
				+ processTimes + " requests from browser");
	}

}
