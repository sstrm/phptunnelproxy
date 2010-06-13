package ptp.net.mp;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ptp.net.ProxyException;

final class GetMethodProcesser extends MethodProcesser {
	private static Logger log = Logger.getLogger(GetMethodProcesser.class);

	@SuppressWarnings("unused")
	private InputStream inFromBrowser;
	private OutputStream outToBrowser;

	GetMethodProcesser(InputStream inFromBrowser, OutputStream outToBrowser) {
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
	}

	@Override
	public void process() throws ProxyException {
		
		super.process();

		requestRemote(ptp.ui.HttpUtil
				.getHeadBytes(reqLine, reqHeaders), destHost, destPort,
				false, outToBrowser);
		log.info("get method process done!");
	}

}
