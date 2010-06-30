package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.InputStream;
import java.io.OutputStream;

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
		process(destHost, destPort, true);
		log.info("ssl post method process done!");
	}

}
