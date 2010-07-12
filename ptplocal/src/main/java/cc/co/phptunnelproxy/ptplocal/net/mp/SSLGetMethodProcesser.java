package cc.co.phptunnelproxy.ptplocal.net.mp;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
		try {
			URL destURL = new URL("https://"+destHost+":"+destPort+reqLine.getDestResource());
			log.info("https://"+destHost+":"+destPort+reqLine.getDestResource());
			request(destURL, reqHH.getBytes(), new byte[0], outToBrowser);
		} catch (MalformedURLException e) {
			throw new ProxyException(e);
		}
		log.info("ssl get method process done!");
	}

}
