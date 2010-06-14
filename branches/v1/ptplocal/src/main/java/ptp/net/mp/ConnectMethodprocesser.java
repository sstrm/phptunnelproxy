package ptp.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.ProxyException;
import ptp.net.ssl.PipeThread;
import ptp.net.ssl.SSLForwardServer;

public class ConnectMethodprocesser extends MethodProcesser {

	private static Logger log = Logger.getLogger(ConnectMethodprocesser.class);

	private InputStream inFromBrowser;
	private OutputStream outToBrowser;

	ConnectMethodprocesser(InputStream inFromBrowser, OutputStream outToBrowser) {
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
	}

	@Override
	public void process() throws ProxyException {
		String destHost = reqHH.getDestHost();
		int destPort = reqHH.getDestPort();

		HttpHead connResHH = new HttpHead("HTTP/1.1 200 Connection established");
		connResHH.setHeader("Proxy-agent", Config.getIns().getUserAgent());
		connResHH.setHeader("Proxy-Connection", "Keep-Alive");

		try {
			outToBrowser.write(connResHH.getHeadBytes());
			outToBrowser.flush();

			SSLForwardServer sss = new SSLForwardServer(destHost, destPort);
			int sslServerPort = sss.startService();

			Socket sslSocket = new Socket("127.0.0.1", sslServerPort);
			InputStream sslIn = sslSocket.getInputStream();
			OutputStream sslOut = sslSocket.getOutputStream();

			Thread pipeThreadFromBrowserToSSLServer = new PipeThread(
					inFromBrowser, sslOut, "Pipe from browser to ssl");
			Thread pipeThreadFromSSLServerToBrowser = new PipeThread(sslIn,
					outToBrowser, "Pipe from ssl to browser");
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

		} catch (IOException e) {
			throw new ProxyException(e);
		}

	}

}
