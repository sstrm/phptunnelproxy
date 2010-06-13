package ptp.net.mp;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.ProxyException;
import ptp.util.ByteArrayUtil;

public class HttpHead {
	private static Logger log = Logger.getLogger(HttpHead.class);
	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	private static final Pattern reqLinePattern = Pattern
			.compile("^(\\w+)\\s(.+)\\s([\\w|\\d|/|\\.]+)$");
	private static final Pattern reqResourcePattern = Pattern
			.compile("\\shttp://[\\w|\\.|:|\\d]+(/.*)\\s");
	private static final Pattern resLinePattern = Pattern
			.compile("^([\\w|/|\\.]+)\\s(\\d+)\\s\\w*$");

	private String destHost;
	private int destPort;
	private String destResource;
	private URL destURL;
	private int httpCode = -1;
	private String httpVersion;
	private String methodName;

	private String line;
	private Map<String, String> headers = new HashMap<String, String>();

	private int readHttpHead(byte[] buff, InputStream in, byte key)
			throws ProxyException {
		int index = 0;
		int findEnd = 0;
		while (findEnd < 4) {
			byte b = 0;
			try {
				b = (byte) ((byte) in.read() - key);
			} catch (IOException e) {
				throw new ProxyException(e);
			}
			try {
				buff[index++] = b;
			} catch (ArrayIndexOutOfBoundsException e) {
				log.debug(ByteArrayUtil.toString(buff, 0, index - 1), e);
				log.error(e.getMessage(), e);
				throw new ProxyException(e);
			}
			if (b == '\r' || b == '\n') {
				findEnd++;
			} else {
				findEnd = 0;
			}
		}

		return index;
	}

	public HttpHead(InputStream in, byte key) throws ProxyException {
		byte[] buff = new byte[buff_size];
		int headLength = 0;
		headLength = readHttpHead(buff, in, key);

		String headString = ByteArrayUtil.toString(buff, 0, headLength);
		String[] headArray = headString.split("\\r\\n");

		line = headArray[0];

		for (int i = 1; i < headArray.length; i++) {
			String[] tokens = headArray[i].split(":\\s");

			if (tokens.length == 2) {
				headers.put(tokens[0], tokens[1]);
			} else if (tokens.length == 1) {
				headers.put(tokens[0], "");
			} else {
				// what a fucking header?!
				throw new ProxyException("Get a wrong http header: "
						+ headArray[i]);
			}
		}

		if (line.startsWith("HTTP")) {
			Matcher m = resLinePattern.matcher(line);
			if (m.matches()) {
				httpVersion = m.group(1);
				httpCode = Integer.parseInt(m.group(2));
			}
		} else {
			Matcher m1 = reqLinePattern.matcher(line);
			if (m1.matches()) {
				methodName = m1.group(1);
				try {
					destURL = new URL(m1.group(2));
				} catch (MalformedURLException e) {
					log.error(e.getMessage(), e);
				}
				destHost = Config.getIns().getIp(destURL.getHost());
				destPort = destURL.getPort() != -1 ? getDestURL().getPort()
						: 80;
				httpVersion = m1.group(3);
			}

			Matcher m2 = reqResourcePattern.matcher(line);
			if (m2.find()) {
				destResource = m2.group(1);
			}
		}
	}

	public void setHeader(String headerName, String headerValue) {
		headers.put(headerName, headerValue);
	}

	public void removeHeader(String headerName) {
		headers.remove(headerName);
	}

	public String getHeader(String headerName) {
		return headers.get(headerName);
	}

	public String getMethodName() {
		return methodName;
	}

	public URL getDestURL() {
		return this.destURL;
	}

	public String getDestHost() {
		return this.destHost;
	}

	public int getDestPort() {
		return this.destPort;
	}

	public String getDestResource() {
		return this.destResource;
	}

	public String getHttpVersion() {
		return this.httpVersion;
	}

	public void normalizeRequestLine() {
		if (line.startsWith("HTTP")) {
			return;
		} else {
			line = getMethodName() + " " + getDestResource() + " "
					+ getHttpVersion();
		}

	}

	public int getHttpCode() {
		return this.httpCode;
	}

	public byte[] getHeadBytes() {
		final String CRLF = "\r\n";
		StringBuilder sb = new StringBuilder();
		sb.append(line).append(CRLF);
		for (String headerName : headers.keySet()) {
			String headerValue = headers.get(headerName);
			sb.append(headerName).append(": ").append(headerValue).append(CRLF);
		}
		sb.append(CRLF);

		return ByteArrayUtil.getBytesFromString(sb.toString());
	}
}
