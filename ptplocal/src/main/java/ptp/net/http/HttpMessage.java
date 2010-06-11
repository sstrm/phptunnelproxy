package ptp.net.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ptp.Config;
import ptp.net.ProxyException;
import ptp.net.util.HttpUtil;
import ptp.util.ByteArrayUtil;

public abstract class HttpMessage {
	private static Logger log = Logger.getLogger(HttpMessage.class);
	private static int buff_size = Integer.parseInt(Config.getIns().getValue(
			"ptp.local.buff.size", "102400"));

	protected String firstLine;

	protected String version;

	protected Map<String, String> headers;

	protected File bodyDataFile;

	protected HttpMessage() {
		headers = new HashMap<String, String>();
	}

	public void read(InputStream in) throws ProxyException {
		this.readHttpHeaders(in);
		this.readHttpBody(in);
	}

	protected abstract void readHttpHeaders(InputStream in)
			throws ProxyException;

	protected void readHttpHeaders(InputStream in, byte key)
			throws ProxyException {
		byte[] buff = new byte[buff_size];
		int headerLength = 0;
		headerLength = HttpUtil.readHttpHead(buff, in, key);

		String headString = ByteArrayUtil.toString(buff, 0, headerLength);
		String[] headArray = headString.split("\\r\\n");
		this.firstLine = headArray[0];

		for (int i = 1; i < headArray.length; i++) {
			String[] tokens = headArray[i].split(":\\s");

			if (tokens.length == 2) {
				this.headers.put(tokens[0], tokens[1]);
			} else if (tokens.length == 1) {
				this.headers.put(tokens[0], "");
			} else {
				// what a fucking header?!
				throw new ProxyException("Get a wrong http header: "
						+ headArray[i]);
			}

		}
	}

	protected abstract void readHttpBody(InputStream in) throws ProxyException;

	protected FileOutputStream getBodyDataFileOutputStream()
			throws ProxyException {
		FileOutputStream bodyDataTmpFOS = null;
		try {
			if (this.bodyDataFile == null) {
				File tmpDirFile = new File("tmp");
				if (!(tmpDirFile.exists() && tmpDirFile.isDirectory())) {
					if (tmpDirFile.exists()) {
						tmpDirFile.delete();
					}
					tmpDirFile.mkdir();
				}
				this.bodyDataFile = File.createTempFile(Thread.currentThread()
						.getName()
						+ new Date().getTime(), ".ptp", tmpDirFile);
				log.debug("create body data file: "
						+ this.bodyDataFile.getAbsolutePath());
				bodyDataTmpFOS = new FileOutputStream(this.bodyDataFile);
			}
		} catch (IOException e) {
			throw new ProxyException(e);
		}
		return bodyDataTmpFOS;
	}

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

	public byte[] getBytes() throws ProxyException {
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
				throw new ProxyException(e);
			}
			return bytes;
		} else {
			return headerByte;
		}

	}

	public void clear() {
		boolean wantDel = Boolean.valueOf(Config.getIns().getValue(
				"ptp.local.deltmp", "true"));

		if (wantDel && this.bodyDataFile != null) {
			log.debug("delete: " + this.bodyDataFile.getPath());
			this.bodyDataFile.delete();
		}

		this.bodyDataFile = null;
	}

}
