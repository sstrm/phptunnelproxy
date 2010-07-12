package cc.co.phptunnelproxy.ptplocal.net.mp.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import cc.co.phptunnelproxy.ptplocal.Config;
import cc.co.phptunnelproxy.ptplocal.util.ByteArrayUtil;
import cc.co.phptunnelproxy.ptplocal.util.DumpUtil;

public abstract class HttpStartLine {
	private static Logger log = Logger.getLogger(HttpStartLine.class);

	protected String line;

	public HttpStartLine(String line) {
		this.line = line;
	}

	public HttpStartLine(InputStream in) throws HttpParseException {
		int index = 0;
		int findEnd = 0;
		int buff_size = Integer.parseInt(Config.getIns().getValue(
				"ptp.local.buff.size", "102400"));
		byte[] buff = new byte[buff_size];
		while (findEnd < 2) {
			byte b = 0;
			try {
				b = (byte) in.read();
			} catch (IOException e) {
				throw new HttpParseException(e);
			}
			try {
				buff[index++] = b;
			} catch (ArrayIndexOutOfBoundsException e) {
				log.info(DumpUtil.dump(buff, 0, index - 1), e);
				log.error(e.getMessage(), e);
				throw new HttpParseException(e);
			}
			if (b == '\r' || b == '\n') {
				findEnd++;
			} else {
				findEnd = 0;
			}
		}

		this.line = ByteArrayUtil.toString(buff, 0, index);
		this.line = this.line.trim();
	}

	public byte[] getBytes() {
		final String CRLF = "\r\n";
		return ByteArrayUtil.getBytesFromString(line + CRLF);
	}

	@Override
	public String toString() {
		return this.line;
	}

}
