package ptp.net.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.util.ByteArrayUtil;
import ptp.util.HttpUtil;

public abstract class HttpMessage {
	private static Logger log = Logger.getLogger(HttpMessage.class);
	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.buff.size", "102400"));

	protected String firstLine;

	protected String version;

	protected Map<String, String> headers;

	protected File bodyDataFile;

	protected HttpMessage() {
		headers = new HashMap<String, String>();
	}

	public void read(InputStream in) {
		this.readHttpHeaders(in);
		this.readHttpBody(in);
	}

	protected abstract void readHttpHeaders(InputStream in);

	protected void readHttpHeaders(InputStream in, byte key) {
		byte[] buff = new byte[buff_size];
		int headerLength = 0;
		try {
			headerLength = HttpUtil.readHttpHead(buff, in, key);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		String headString = ByteArrayUtil.toString(buff, 0, headerLength);
		String[] headArray = headString.split("\\r\\n");
		firstLine = headArray[0];

		for (int i = 1; i < headArray.length; i++) {
			String[] tokens = headArray[i].split(":\\s");

			if (tokens.length == 2) {
				this.headers.put(tokens[0], tokens[1]);
			} else if (tokens.length == 1) {
				this.headers.put(tokens[0], "");
			} else {
				// TODO what a fucking header?!
			}

		}
	}

	protected abstract void readHttpBody(InputStream in);

	public String getHeader(String headerName) {
		return this.headers.get(headerName);
	}

	public void setHeader(String headerName, String value) {
		this.headers.put(headerName, value);
	}

	public void removeHeader(String headerName) {
		this.headers.remove(headerName);
	}

	public File getBodyDataFile() {
		return this.bodyDataFile;
	}

	public byte[] getBytes() {
		final String CRLF = "\r\n";
		StringBuilder sb = new StringBuilder();
		sb.append(this.firstLine).append(CRLF);
		for (String headerName : this.headers.keySet()) {
			String headerValue = this.headers.get(headerName);
			sb.append(headerName).append(": ").append(headerValue).append(CRLF);
		}
		sb.append(CRLF);

		byte[] headerByte = ByteArrayUtil.getBytesFromString(sb.toString());
		int headerSize = headerByte.length;
		if (this.bodyDataFile != null) {
			int bodySize = (int) this.bodyDataFile.length();
			byte[] bytes = new byte[headerSize + bodySize];
			ByteArrayUtil.copy(headerByte, 0, bytes, 0, headerSize);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(this.bodyDataFile);
				fis.read(bytes, headerSize, bodySize);
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bytes;
		} else {
			return headerByte;
		}

	}

	public void destry() {
		if (this.bodyDataFile != null) {
			log.info("delete: "+this.bodyDataFile.getPath());
			this.bodyDataFile.delete();
		}
	}

}
