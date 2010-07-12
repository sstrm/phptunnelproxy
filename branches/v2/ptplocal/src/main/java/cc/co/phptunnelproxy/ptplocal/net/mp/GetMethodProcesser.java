package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;

public class GetMethodProcesser extends MethodProcesser {
	private static Logger log = Logger.getLogger(GetMethodProcesser.class);

	protected InputStream inFromBrowser;
	protected OutputStream outToBrowser;

	GetMethodProcesser(InputStream inFromBrowser, OutputStream outToBrowser) {
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
	}

	@Override
	public void process() throws ProxyException {
		request(reqLine.getDestURL(), reqHH.getBytes(), new byte[0], outToBrowser);
		log.info("get method process done!");
	}
}
