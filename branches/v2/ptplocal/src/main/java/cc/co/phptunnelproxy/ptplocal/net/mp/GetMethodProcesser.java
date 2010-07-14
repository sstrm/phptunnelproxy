package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpParseException;

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
		String destHost = reqLine.getDestHost();
		int destPort = reqLine.getDestPort();
		String destUrl = reqLine.getAbsDestResource();

		process(destUrl, destHost, destPort, false);

		log.info("get method process done!");
	}

	protected void process(String destUrl, String destHost, int destPort, boolean isSSL)
			throws ProxyException {
		reqHH.removeHeader("Proxy-Connection");
		reqHH.removeHeader("Keep-Alive");
		// reqHH.removeHeader("Accept-Encoding");
		reqHH.setHeader("Connection", "close");

		byte[] reqLineData = null;
		try {
			reqLineData = reqLine.getNormalizedIns().getBytes();
		} catch (HttpParseException e) {
			throw new ProxyException(e);
		}
		byte[] reqHeadData = reqHH.getBytes();
		log.info("get method begin request remote!");
		requestRemote(reqLineData, reqHeadData, new byte[0],
				destUrl, destHost, destPort, isSSL,
				outToBrowser);
	}

}
