package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.util.ByteArrayUtil;

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
		String destHost = reqHH.getDestHost();
		int destPort = reqHH.getDestPort();

		process(destHost, destPort, false);
		
		log.info("post method process done!");
	}

	public void process(String destHost, int destPort, boolean isSSL)
			throws ProxyException {

		reqHH.removeHeader("Proxy-Connection");
		reqHH.removeHeader("Keep-Alive");
		reqHH.setHeader("Connection", "close");

		reqHH.normalizeRequestLine();

		byte[] newRequestHeaderData = reqHH.getHeadBytes();

		int postContentLength = Integer.parseInt(reqHH
				.getHeader("Content-Length"));
		byte[] newRequestBodyData = new byte[postContentLength];
		int postContentReadCount = 0;
		while (postContentReadCount < postContentLength) {
			try {
				postContentReadCount += inFromBrowser.read(newRequestBodyData,
						postContentReadCount, postContentLength
								- postContentReadCount);

				log.debug("postContentReadCount: " + postContentReadCount);
			} catch (IOException e) {
				throw new ProxyException(e);
			}
		}

		byte[] newRequestData = new byte[newRequestHeaderData.length
				+ newRequestBodyData.length];
		ByteArrayUtil.copy(newRequestHeaderData, 0, newRequestData, 0,
				newRequestHeaderData.length);
		ByteArrayUtil.copy(newRequestBodyData, 0, newRequestData,
				newRequestHeaderData.length, newRequestBodyData.length);

		requestRemote(newRequestData, destHost, destPort, isSSL, outToBrowser);
	}

}
