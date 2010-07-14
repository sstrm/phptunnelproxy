package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpParseException;

public class PostMethodProcesser extends MethodProcesser {

	private static Logger log = Logger.getLogger(PostMethodProcesser.class);

	private InputStream inFromBrowser;
	private OutputStream outToBrowser;

	PostMethodProcesser(InputStream inFromBrowser, OutputStream outToBrowser) {
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
	}

	@Override
	public void process() throws ProxyException {
		String destHost = reqLine.getDestHost();
		int destPort = reqLine.getDestPort();
		String destUrl = reqLine.getAbsDestResource();

		process(destUrl, destHost, destPort, false);

		log.info("post method process done!");
	}

	public void process(String destUrl, String destHost, int destPort, boolean isSSL)
			throws ProxyException {
		byte[] reqLineData = null;
		try {
			reqLineData = reqLine.getNormalizedIns().getBytes();
		} catch (HttpParseException e) {
			throw new ProxyException(e);
		}

		reqHH.removeHeader("Proxy-Connection");
		reqHH.removeHeader("Keep-Alive");
		reqHH.setHeader("Connection", "close");

		byte[] reqHeadData = reqHH.getBytes();

		int postContentLength = Integer.parseInt(reqHH.getHeader(
				"Content-Length").get(0));
		byte[] reqBodyData = new byte[postContentLength];
		int postContentReadCount = 0;
		while (postContentReadCount < postContentLength) {
			try {
				postContentReadCount += inFromBrowser.read(reqBodyData,
						postContentReadCount, postContentLength
								- postContentReadCount);

				log.debug("postContentReadCount: " + postContentReadCount);
			} catch (IOException e) {
				throw new ProxyException(e);
			}
		}

		requestRemote(reqLineData, reqHeadData, reqBodyData,
				destUrl, destHost, destPort, isSSL,
				outToBrowser);
	}

}
