package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;
import cc.co.phptunnelproxy.ptplocal.net.mp.http.HttpParseException;
import cc.co.phptunnelproxy.ptplocal.util.ByteArrayUtil;

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

		process(destHost, destPort, false);
		//process_url();
		log.info("get method process done!");
	}

	protected void process(String destHost, int destPort, boolean isSSL)
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
		byte[] reqData = new byte[reqLineData.length + reqHeadData.length];
		ByteArrayUtil.copy(reqLineData, 0, reqData, 0, reqLineData.length);
		ByteArrayUtil.copy(reqHeadData, 0, reqData, reqLineData.length,
				reqHeadData.length);
		requestRemote(reqData, destHost, destPort, isSSL, outToBrowser);
	}

	protected void process_url()
			throws ProxyException {
		reqHH.removeHeader("Proxy-Connection");
		reqHH.removeHeader("Keep-Alive");
		//reqHH.removeHeader("Accept-Encoding");
		reqHH.setHeader("Connection", "close");

		this.request(reqLine.getDestURL(), reqHH.getBytes(), new byte[0], outToBrowser);
	}
}
