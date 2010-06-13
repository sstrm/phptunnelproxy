package ptp.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ptp.net.ProxyException;
import ptp.util.ByteArrayUtil;
import ptp.util.HttpUtil;

public class PostMethodProcesser extends MethodProcesser {

	private static Logger log = Logger.getLogger(PostMethodProcesser.class);

	private InputStream inFromBrowser;
	private OutputStream outToBrowser;

	PostMethodProcesser(InputStream inFromBrowser,
			OutputStream outToBrowser) {
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
	}

	@Override
	public void process() throws ProxyException {
		super.process();
		byte[] newRequestHeaderData = HttpUtil.getHeadBytes(reqLine, reqHeaders);

		int postContentLength = Integer.parseInt(reqHeaders.get("Content-Length"));
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

		requestRemote(newRequestData, destHost, destPort, false, outToBrowser);
		log.info("post method process done!");
	}

}
