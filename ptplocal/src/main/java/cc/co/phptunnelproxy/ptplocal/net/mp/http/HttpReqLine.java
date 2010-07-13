package cc.co.phptunnelproxy.ptplocal.net.mp.http;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.co.phptunnelproxy.ptplocal.Config;

public class HttpReqLine extends HttpStartLine {

	private static final Pattern reqLinePattern = Pattern
			.compile("^(\\w+)\\s(.+)\\s([\\w|\\d|/|\\.]+)$");
	private static final Pattern reqResourcePattern = Pattern
			.compile("\\shttp://.+?(/.*)\\s");

	private String methodName = null;
	private String destHost = null;
	private int destPort = -1;
	private String destResource = null;
	private String httpVersion = null;
	private URL destURL = null;

	private void analize() throws HttpParseException {
		Matcher m1 = reqLinePattern.matcher(this.line);
		if (!m1.matches()) {
			throw new HttpParseException("Invalid Http Request Startline: "
					+ line);
		} else {
			methodName = m1.group(1);
			if (methodName.equals("CONNECT")) {
				String connectDest = m1.group(2);
				destHost = connectDest.split(":")[0];
				destPort = Integer.parseInt(connectDest.split(":")[1]);
			} else {
				try {
					destURL = new URL(m1.group(2));
				} catch (MalformedURLException e) {
					this.destResource = m1.group(2);
				}
				if (destURL != null) {
					destHost = Config.getIns().getIp(destURL.getHost());
					destPort = destURL.getPort() != -1 ? destURL.getPort()
							: destURL.getDefaultPort();
				}

			}

			httpVersion = m1.group(3);

			Matcher m2 = reqResourcePattern.matcher(line);
			if (m2.find()) {
				destResource = m2.group(1);
			}
		}
	}

	public HttpReqLine(String line) throws HttpParseException {
		super(line);
		analize();
	}

	public HttpReqLine(InputStream inFromBrowser) throws HttpParseException {
		super(inFromBrowser);
		analize();
	}

	public String getMethodName() {
		return this.methodName;
	}

	public String getHttpVersion() {
		return this.httpVersion;
	}

	public String getAbsDestResource() {
		return this.destURL.toString();
	}

	public String getDestResource() {
		return this.destResource;
	}

	public String getDestHost() {
		return this.destHost;
	}

	public int getDestPort() {
		return this.destPort;
	}

	public HttpReqLine getNormalizedIns() throws HttpParseException {
		String normalizedLine = getMethodName() + " " + getDestResource() + " "
				+ getHttpVersion();
		return new HttpReqLine(normalizedLine);
	}

}
