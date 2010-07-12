package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;

public class PostMethodProcesser extends MethodProcesser {

	private static Logger log = Logger.getLogger(PostMethodProcesser.class);

	protected InputStream inFromBrowser;
	protected OutputStream outToBrowser;

	PostMethodProcesser(InputStream inFromBrowser, OutputStream outToBrowser) {
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
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
		request(reqLine.getDestURL(), reqHH.getBytes(), postBodyData, outToBrowser);
		log.info("post method process done!");
	}

}
