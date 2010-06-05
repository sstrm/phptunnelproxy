package ptp.net.mp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.HttpUtil;
import ptp.util.URLUtil;

final class GetMethodProcesser extends MethodProcesser {
	private static Logger log = Logger.getLogger(GetMethodProcesser.class);

	private String[] headers;
	@SuppressWarnings("unused")
	private InputStream inFromBrowser;
	private OutputStream outToBrowser;

	GetMethodProcesser(String[] headers, InputStream inFromBrowser,
			OutputStream outToBrowser) {
		this.headers = headers;
		this.inFromBrowser = inFromBrowser;
		this.outToBrowser = outToBrowser;
	}

	@Override
	public void process() {
		StringBuilder newRequestHeaderString = new StringBuilder();

		URL requestURL;
		try {
			requestURL = new URL(headers[0].split("\\s")[1]);
		} catch (MalformedURLException e1) {
			HttpUtil.writeErrorResponse(outToBrowser,
					"browser send malformed url");
			log.error("browser send malformed url", e1);
			return;
		}

		String destHost = requestURL.getHost();
		log.info("destHost: " + destHost);
		destHost = Config.getIns().getIp(destHost);

		int destPort = requestURL.getPort() != -1 ? requestURL.getPort() : 80;
		log.info("destPort: " + destPort);

		String method = headers[0].split("\\s")[0];
		String resourc = URLUtil.getResource(headers[0].split("\\s")[1]);
		log.info("destPath: " + resourc);
		String version = headers[0].split("\\s")[2];
		newRequestHeaderString.append(method).append(" ").append(resourc)
				.append(" ").append(version).append("\r\n");

		for (int i = 0; i < headers.length; i++) {
			if (i == 0) {
				continue;
			} else if (headers[i].startsWith("Proxy-Connection")) {
				continue;
			} else if (headers[i].startsWith("Connection")) {
				continue;
			} else if (headers[i].startsWith("Keep-Alive")) {
				continue;
			} else {
				newRequestHeaderString.append(headers[i]).append("\r\n");
			}
		}
		newRequestHeaderString.append("Connection: close").append("\r\n");
		newRequestHeaderString.append("\r\n");

		log.info("Request Headers: ");
		log.debug("\n" + newRequestHeaderString);

		byte[] newRequestHeaderData = null;
		try {
			newRequestHeaderData = newRequestHeaderString.toString().getBytes(
					"US-ASCII");
		} catch (UnsupportedEncodingException e) {

		}

		requestRemote(newRequestHeaderData, destHost, destPort, false,
				outToBrowser);
		log.info("http proxy done!");
	}

}
