package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;

public class SSLPostMethodProcesser extends PostMethodProcesser {
	private static Logger log = Logger.getLogger(SSLPostMethodProcesser.class);

	private String destHost;
	private int destPort;

	SSLPostMethodProcesser(InputStream inFromBrowser,
			OutputStream outToBrowser, String destHost, int destPort) {
		super(inFromBrowser, outToBrowser);
		this.destHost = destHost;
		this.destPort = destPort;
	}

	@Override
	public void process() throws ProxyException {
		int postContentLength = Integer.parseInt(reqHH.getHeader(
				"Content-Length").get(0));
		byte[] postBodyData = new byte[postContentLength];
		int postContentReadCount = 0;
		while (postContentReadCount < postContentLength) {
			try {
				postContentReadCount += inFromBrowser.read(postBodyData,
						postContentReadCount, postContentLength
								- postContentReadCount);

				log.debug("postContentReadCount: " + postContentReadCount);
			} catch (IOException e) {
				throw new ProxyException(e);
			}
		}
		try {
			URL destURL = new URL("https://" + destHost + ":" + destPort
					+ reqLine.getDestResource());
			log.info("https://" + destHost + ":" + destPort
					+ reqLine.getDestResource());
			request(destURL, reqHH.getBytes(), postBodyData, outToBrowser);
		} catch (MalformedURLException e) {
			throw new ProxyException(e);
		}

		log.info("ssl post method process done!");
	}

}
