package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.net.ProxyException;

public class SSLGetMethodProcesser extends GetMethodProcesser {

	private static Logger log = Logger.getLogger(GetMethodProcesser.class);

	private String destHost;
	private int destPort;

	public SSLGetMethodProcesser(InputStream inFromBrowser,
			OutputStream outToBrowser, String destHost, int destPort) {
		super(inFromBrowser, outToBrowser);
		this.destHost = destHost;
		this.destPort = destPort;
	}

	@Override
	public void process() throws ProxyException {
		log.info("ssl get method process begin!");
		String destUrl = "https://" + destHost + ":" + destPort
				+ reqLine.getDestResource();
		process(destUrl, destHost, destPort, true);
		log.info("ssl get method process done!");
	}

}
