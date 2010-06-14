package ptp.net.mp;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ptp.net.ProxyException;

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
		process(destHost, destPort, true);
		log.info("ssl get method process done!");
	}

}
