package cc.co.phptunnelproxy.ptplocal.net.mp.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;
import cc.co.phptunnelproxy.ptplocal.util.ByteArrayUtil;
import cc.co.phptunnelproxy.ptplocal.util.DumpUtil;

public class HttpHead {
	private static Logger log = Logger.getLogger(HttpHead.class);
	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	private List<HeaderNameValue> headers = new ArrayList<HeaderNameValue>();

	public HttpHead() {

	}

	private int readHttpHead(byte[] buff, InputStream in)
			throws HttpParseException {
		int index = 0;
		int findEnd = 0;
		while (findEnd < 4) {
			byte b = 0;
			try {
				b = (byte) in.read();
			} catch (IOException e) {
				throw new HttpParseException(e);
			}
			try {
				buff[index++] = b;
			} catch (ArrayIndexOutOfBoundsException e) {
				log.debug(DumpUtil.dump(buff, 0, index - 1), e);
				log.error(e.getMessage(), e);
				throw new HttpParseException(e);
			}
			if (b == '\r' || b == '\n') {
				findEnd++;
			} else {
				findEnd = 0;
			}
		}

		return index;
	}

	public HttpHead(InputStream in) throws HttpParseException {
		byte[] buff = new byte[buff_size];
		int headLength = 0;
		headLength = readHttpHead(buff, in);

		String headString = ByteArrayUtil.toString(buff, 0, headLength);
		//log.info(headString);
		String[] headArray = headString.split("\\r\\n");

		for (int i = 0; i < headArray.length; i++) {
			int splitIndex = headArray[i].indexOf(": ");
			if (splitIndex > 0) {
				String headerName = headArray[i].substring(0, splitIndex);
				String headerValue = headArray[i].substring(splitIndex + 2);
				headers.add(new HeaderNameValue(headerName, headerValue));
			} else {
				// what a fucking header?!
				throw new HttpParseException("Get a wrong http header: "
						+ headArray[i]);
			}
		}

	}

	public void setHeader(String headerName, String headerValue) {
		removeHeader(headerName);
		this.headers.add(new HeaderNameValue(headerName, headerValue));
	}

	public boolean removeHeader(String headerName) {
		for (HeaderNameValue hnv : this.headers) {
			if (hnv.getName().equals(headerName)) {
				return this.headers.remove(hnv);
			}
		}
		return false;
	}

	public String getHeader(String headerName) {
		for (HeaderNameValue hnv : this.headers) {
			if (hnv.getName().equals(headerName)) {
				return hnv.getValue();
			}
		}
		return null;
	}

	public byte[] getBytes() {
		final String CRLF = "\r\n";
		StringBuilder sb = new StringBuilder();
		for (HeaderNameValue hnv : this.headers) {
			sb.append(hnv.getName()).append(": ").append(hnv.getValue())
					.append(CRLF);
		}
		sb.append(CRLF);

		return ByteArrayUtil.getBytesFromString(sb.toString());
	}
}

class HeaderNameValue {
	private String headerName;
	private String headerValue;

	HeaderNameValue(String headerName, String headerValue) {
		this.headerName = headerName;
		this.headerValue = headerValue;
	}

	public String getName() {
		return this.headerName;
	}

	public String getValue() {
		return this.headerValue;
	}
}