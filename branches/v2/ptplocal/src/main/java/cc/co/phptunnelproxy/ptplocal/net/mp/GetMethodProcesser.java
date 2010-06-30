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
		String destHost = reqHH.getDestHost();
		int destPort = reqHH.getDestPort();

		process(destHost, destPort, false);

		log.info("get method process done!");
	}

	protected void process(String destHost, int destPort, boolean isSSL)
			throws ProxyException {
		reqHH.removeHeader("Proxy-Connection");
		reqHH.removeHeader("Keep-Alive");
		reqHH.setHeader("Connection", "close");

		reqHH.normalizeRequestLine();
		requestRemote(reqHH.getHeadBytes(), destHost, destPort, isSSL,
				outToBrowser);
	}

}
