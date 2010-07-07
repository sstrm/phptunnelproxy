package cc.co.phptunnelproxy.ptplocal.net.mp.http;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResLine extends HttpStartLine {
	private static final Pattern resLinePattern = Pattern
			.compile("^([\\w|/|\\.]+)\\s(\\d+)\\s\\w*$");

	private String httpVersion = null;
	private int statusCode;
	
	private void analize() throws HttpParseException {
		if (!line.startsWith("HTTP")) {
			throw new HttpParseException("Invalid Http Response Startline: "
					+ line);
		} else {
			Matcher m = resLinePattern.matcher(line);
			if (m.matches()) {
				httpVersion = m.group(1);
				statusCode = Integer.parseInt(m.group(2));
			}
		}
	}

	public HttpResLine(String line) throws HttpParseException {
		super(line);
		analize();
	}

	public HttpResLine(InputStream inFromPhp) throws HttpParseException {
		super(inFromPhp);
		analize();
	}

	public String getHttpVersion() {
		return this.httpVersion;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

}
