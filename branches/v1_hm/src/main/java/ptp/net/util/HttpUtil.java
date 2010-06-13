package ptp.net.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import ptp.net.ProxyException;
import ptp.util.ByteArrayUtil;

public class HttpUtil {
	private static Logger log = Logger.getLogger(HttpUtil.class);

	public static int readHttpHead(byte[] buff, InputStream in)
			throws ProxyException {
		return readHttpHead(buff, in, (byte) 0);
	}

	public static int readHttpHead(byte[] buff, InputStream in, byte key)
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

	public static void writeErrorResponse(OutputStream outToBrowser,
			ProxyException proxyException) {
		log.info("wirite error page for: " + proxyException.getMessage());
		PrintWriter w = new PrintWriter(new OutputStreamWriter(outToBrowser));
		w.write("HTTP/1.1 500 Internal Server Error\r\n");
		w.write("Content-Type: text/html; charset=utf-8\r\n");
		w.write("Connection: close\r\n");
		w.write("\r\n");

		w.write("<html>");
		w.write("<head><title>HTTP 500 Internal Server Error</title><head>");
		w.write("<body><pre>");
		proxyException.printStackTrace(w);
		w.write("</pre></body>");
		w.write("</html>");
		w.flush();
		w.close();
	}

}
