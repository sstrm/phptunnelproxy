package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;
import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpHead;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpParseException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpResLine;
import cc.co.phptunnelproxy.ptplocal.net.ssl.PipeThread;
import cc.co.phptunnelproxy.ptplocal.net.ssl.SSLForwardServer;

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
		String destHost = reqLine.getDestHost();
		int destPort = reqLine.getDestPort();

		HttpResLine connResLine = null;
		try {
			connResLine = new HttpResLine("HTTP/1.1 200 Connection established");
		} catch (HttpParseException e) {
			throw new ProxyException(e);
		}
		HttpHead connResHH = new HttpHead();
		connResHH.setHeader("Proxy-agent", Config.getIns().getUserAgent());
		connResHH.setHeader("Proxy-Connection", "Keep-Alive");

		try {
			outToBrowser.write(connResLine.getBytes());
			outToBrowser.write(connResHH.getBytes());
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
